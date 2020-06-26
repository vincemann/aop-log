package com.github.vincemann.aoplog;

import com.github.vincemann.aoplog.api.LogInteraction;
import com.github.vincemann.aoplog.api.LogAllInteractions;
import com.github.vincemann.aoplog.api.LogException;
import com.github.vincemann.aoplog.api.UltimateTargetClassAware;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.lang.Nullable;
import org.springframework.test.util.AopTestUtils;
import org.springframework.util.ReflectionUtils;


import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentHashMap;


/**
 * To make it work with JDK Runtime Proxies implement {@link UltimateTargetClassAware}.
 *
 * @see UltimateTargetClassAware
 */
@Aspect
@NoArgsConstructor
public class ProxyAwareAopLogger implements InitializingBean {

    // private static final Log LOGGER = LogFactory.getLog(AOPLogger.class);
    private LogAdapter logAdapter;
    private Map<Severity, LogStrategy> logStrategies;
    private final LocalVariableTableParameterNameDiscoverer localVariableNameDiscoverer = new LocalVariableTableParameterNameDiscoverer();
    private final ExceptionResolver exceptionResolver = new ExceptionResolver();
    private final ConcurrentMap<Method, MethodDescriptor> cache = new ConcurrentHashMap<Method, MethodDescriptor>();
    private AnnotationParser annotationParser;
    //I add this impl bc otherwise the ignoreGetters and ignoreSetters in @LogConfig, that is handled by this component, would be ignored,
    // which would be unexpected behavior
    private final List<MethodFilter> methodFilters = Lists.newArrayList(new LogConfigMethodFilter());

    @Override
    public void afterPropertiesSet(){
        logStrategies = new EnumMap<Severity, LogStrategy>(Severity.class);
        logStrategies.put(Severity.FATAL, new LogStrategy.FatalLogStrategy(logAdapter));
        logStrategies.put(Severity.ERROR, new LogStrategy.ErrorLogStrategy(logAdapter));
        logStrategies.put(Severity.WARN, new LogStrategy.WarnLogStrategy(logAdapter));
        logStrategies.put(Severity.INFO, new LogStrategy.InfoLogStrategy(logAdapter));
        logStrategies.put(Severity.DEBUG, new LogStrategy.DebugLogStrategy(logAdapter));
        logStrategies.put(Severity.TRACE, new LogStrategy.TraceLogStrategy(logAdapter));
    }

    public void setLogAdapter(LogAdapter log) {
        this.logAdapter = log;
    }

    @Autowired
    public ProxyAwareAopLogger(AnnotationParser annotationParser,MethodFilter... methodFilters) {
        this.annotationParser = annotationParser;
        this.methodFilters.addAll(Lists.newArrayList(methodFilters));
    }

    @Around("this(com.github.vincemann.aoplog.api.AopLoggable)")
    public Object log(ProceedingJoinPoint joinPoint) throws Throwable {
        LoggedMethodCall loggedCall = new LoggedMethodCall(joinPoint,findTargetClass(joinPoint));

        //method filter only restricts interaction logging, logException is independent subsystem
        for (MethodFilter methodFilter : methodFilters) {
            if (!methodFilter.wanted(loggedCall.methodDescriptor)){
                return proceedWithExceptionLogging(loggedCall);
            }
        }

        if (loggedCall.isInteractionLoggingOn()) {
            loggedCall.logInvocation();
        }

        proceedWithExceptionLogging(loggedCall);


        if (loggedCall.isInteractionLoggingOn()) {
            loggedCall.logResult();
        }
        return loggedCall.getResult();
    }

    protected Object proceedWithExceptionLogging(LoggedMethodCall loggedCall) throws Throwable {
        if (!loggedCall.isExceptionLoggingOn()) {
            loggedCall.proceed();
        } else {
            try {
                loggedCall.proceed();
            } catch (Exception e) {
                loggedCall.logException(e);
                throw e;
            }
        }
        return loggedCall.getResult();
    }

    protected Class<?> findTargetClass(ProceedingJoinPoint joinPoint){
        Object target = joinPoint.getTarget();
        if (target instanceof UltimateTargetClassAware){
            return ((UltimateTargetClassAware) target).getTargetClass();
        }
        return AopTestUtils.getUltimateTargetObject(joinPoint.getTarget()).getClass();
    }

    @Getter
    @AllArgsConstructor
    public class LoggedMethodCall{
        Method method;
        Class<?> targetClass;
        Object[] args;
        LogInteraction methodLog;
        LogAllInteractions classLog;

        MethodDescriptor methodDescriptor;
        InvocationDescriptor invocationDescriptor;
        ArgumentDescriptor argumentDescriptor;
        ExceptionDescriptor exceptionDescriptor;
        ProceedingJoinPoint joinPoint;
        org.apache.commons.logging.Log logger;

        Object result;

        LoggedMethodCall(ProceedingJoinPoint joinPoint,Class<?> targetClass) throws NoSuchMethodException {
            this.joinPoint = joinPoint;
            this.method = extractMethod(joinPoint);
            this.targetClass = targetClass;
            this.methodLog = annotationParser.fromMethod(method, LogInteraction.class);
            this.classLog = annotationParser.fromClass(targetClass, LogAllInteractions.class);
            AnnotationInfo<LogException>logExceptionInfo = annotationParser.fromMethodOrClass(method,LogException.class);
            this.methodDescriptor = createMethodDescriptor(method,methodLog,classLog,logExceptionInfo);
            this.invocationDescriptor = methodDescriptor.getInvocationDescriptor();
            //exception descriptor will be null if no LogException was found
            if (logExceptionInfo!=null)
                this.exceptionDescriptor = createExceptionDescriptor(methodDescriptor);
            this.args = joinPoint.getArgs();
            this.logger = logAdapter.getLog(targetClass);
            this.argumentDescriptor= createArgumentDescriptor(methodDescriptor,method,args.length);
        }

        Object proceed() throws Throwable {
            result = joinPoint.proceed(args);
            return result;
        }

        void logException(Exception e){
            Class<? extends Exception> resolved = exceptionResolver.resolve(exceptionDescriptor, e);
            if (resolved != null) {
                ExceptionSeverity excSeverity = exceptionDescriptor.getExceptionSeverity(resolved);
                if (isLoggingOn(excSeverity.getSeverity())) {
                    logStrategies.get(excSeverity.getSeverity()).logException(logger, method.getName(), args.length, e, excSeverity.getStackTrace());
                }
            }
        }

        void logInvocation(){
            logStrategies.get(invocationDescriptor.getSeverity())
                    .logBefore(logger, method.getName(), args, argumentDescriptor);
        }

        void logResult(){
            Object loggedResult = (method.getReturnType() == Void.TYPE) ? Void.TYPE : result;
            logStrategies.get(invocationDescriptor.getSeverity()).logAfter(logger, method.getName(), args.length, loggedResult);
        }

        boolean isExceptionLoggingOn(){
            if (exceptionDescriptor==null){
                return false;
            }
            AnnotationInfo<LogException> logExceptionInfo = invocationDescriptor.getExceptionAnnotation();
            if (logExceptionInfo==null){
                return false;
            }
            return (logExceptionInfo.getAnnotation().value().length>0 && isLoggingOn(Severity.ERROR))||
                    (logExceptionInfo.getAnnotation().fatal().length>0 && isLoggingOn(Severity.FATAL)) ||
                    (logExceptionInfo.getAnnotation().trace().length>0 && isLoggingOn(Severity.TRACE))||
                    (logExceptionInfo.getAnnotation().debug().length>0 && isLoggingOn(Severity.DEBUG))||
                    (logExceptionInfo.getAnnotation().info().length>0 && isLoggingOn(Severity.INFO))||
                    (logExceptionInfo.getAnnotation().warn().length>0 && isLoggingOn(Severity.WARN));
        }

        boolean isInteractionLoggingOn(){
            return isLoggingOn(invocationDescriptor.getSeverity());
        }

        private boolean isLoggingOn(Severity severity) {
            return severity != null && logStrategies.get(severity).isLogEnabled(logger);
        }
    }



    private MethodDescriptor createMethodDescriptor(Method method, LogInteraction methodLog, LogAllInteractions classLog, @Nullable AnnotationInfo<LogException> logExceptionInfo) {
        MethodDescriptor cached = cache.get(method);
        if (cached != null) {
            return cached;
        }
        cached = new MethodDescriptor(new InvocationDescriptor.Builder(methodLog,classLog,logExceptionInfo).build(), method);
        MethodDescriptor prev = cache.putIfAbsent(method, cached);
        return prev == null ? cached : prev;
    }

    private ArgumentDescriptor createArgumentDescriptor(MethodDescriptor descriptor, Method method, int argumentCount) {
        if (descriptor.getArgumentDescriptor() != null) {
            return descriptor.getArgumentDescriptor();
        }
        ArgumentDescriptor argumentDescriptor = new ArgumentDescriptor.Builder(method, argumentCount, localVariableNameDiscoverer).build();
        descriptor.setArgumentDescriptor(argumentDescriptor);
        return argumentDescriptor;
    }

    private ExceptionDescriptor createExceptionDescriptor(MethodDescriptor descriptor) {
        if (descriptor.getExceptionDescriptor() != null) {
            return descriptor.getExceptionDescriptor();
        }
        ExceptionDescriptor exceptionDescriptor = new ExceptionDescriptor.Builder(descriptor.getInvocationDescriptor().getExceptionAnnotation()).build();
        descriptor.setExceptionDescriptor(exceptionDescriptor);
        return exceptionDescriptor;
    }

    private Method extractMethod(ProceedingJoinPoint joinPoint) throws NoSuchMethodException {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        // signature.getMethod() points to method declared in interface. it is not suit to discover arg names and arg annotations
        // see AopProxyUtils: org.springframework.cache.interceptor.CacheAspectSupport#execute(CacheAspectSupport.Invoker, Object, Method, Object[])
        Class<?> targetClass = joinPoint.getTarget().getClass();
        if (Modifier.isPublic(signature.getMethod().getModifiers())) {
            return targetClass.getMethod(signature.getName(), signature.getParameterTypes());
        } else {
            return ReflectionUtils.findMethod(targetClass, signature.getName(), signature.getParameterTypes());
        }
    }


}
