package com.github.vincemann.aoplog;

import com.github.vincemann.aoplog.api.LogInteraction;
import com.github.vincemann.aoplog.api.LogException;
import com.github.vincemann.aoplog.api.UltimateTargetClassAware;
import com.github.vincemann.aoplog.parseAnnotation.AnnotationInfo;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import com.github.vincemann.aoplog.parseAnnotation.SourceAwareAnnotationInfo;
import com.github.vincemann.aoplog.parseAnnotation.AnnotationParser;
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
    private final ConcurrentMap<LoggedMethodIdentifier, MethodDescriptor> cache = new ConcurrentHashMap<>();
    private AnnotationParser annotationParser;
    //I add this impl bc otherwise the ignoreGetters and ignoreSetters in @LogConfig, that is handled by this component, would be ignored,
    // which would be unexpected behavior
    private final List<MethodFilter> methodFilters = Lists.newArrayList(new LogConfigMethodFilter());
    private InvocationDescriptorFactory invocationDescriptorFactory;

    @EqualsAndHashCode
    @Getter
    @ToString
    private static class LoggedMethodIdentifier {
        private String name;
        private Class<?>[] argTypes;
        private Class<?> targetClass;
        LoggedMethodIdentifier(Method method,Class<?> targetClass){
            this.name=method.getName();
            this.argTypes=method.getParameterTypes();
            this.targetClass = targetClass;
        }
    }

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
    public ProxyAwareAopLogger(AnnotationParser annotationParser, InvocationDescriptorFactory invocationDescriptorFactory, MethodFilter... methodFilters) {
        this.annotationParser = annotationParser;
        this.invocationDescriptorFactory = invocationDescriptorFactory;
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
                //exception was not catched by logged Method
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
        //class that has chosen annotation or declares method with valid annotation
        Class<?> logClass;
        Object[] args;

        MethodDescriptor methodDescriptor;
        ArgumentDescriptor argumentDescriptor;
        ExceptionDescriptor exceptionDescriptor;
        InvocationDescriptor invocationDescriptor;
        ProceedingJoinPoint joinPoint;
        org.apache.commons.logging.Log logger;

        Object result;

        LoggedMethodCall(ProceedingJoinPoint joinPoint,Class<?> targetClass) throws NoSuchMethodException {
            this.joinPoint = joinPoint;
            this.args = joinPoint.getArgs();
            this.targetClass = targetClass;
            this.method = extractMethod(targetClass,joinPoint);
            this.methodDescriptor = createMethodDescriptor();
            this.exceptionDescriptor = methodDescriptor.getExceptionDescriptor();
            this.invocationDescriptor = methodDescriptor.getInvocationDescriptor();
            this.argumentDescriptor = methodDescriptor.getArgumentDescriptor();
            this.logger = logAdapter.getLog(targetClass);
        }

        private MethodDescriptor createMethodDescriptor(){
//            System.err.println("Searching for method Descriptor in cache with key: " + new LoggedMethodIdentifier(method,targetClass));
//            System.err.println("cache:: " + cache);
            synchronized (cache) {
                MethodDescriptor cached = cache.get(new LoggedMethodIdentifier(method,targetClass));
                if (cached != null) {
//                    System.err.println("Returning method Descriptor from cache: " + cached);
                    return cached;
                } else {
                    AnnotationInfo<LogInteraction> methodLogInfo = annotationParser.fromMethod(targetClass, method.getName(), method.getParameterTypes(), LogInteraction.class);
                    AnnotationInfo<LogInteraction> classLogInfo = annotationParser.fromClass(targetClass, LogInteraction.class);
                    SourceAwareAnnotationInfo<LogException> logExceptionInfo = annotationParser.fromMethodOrClass(method, LogException.class);
                    ArgumentDescriptor argumentDescriptor = new ArgumentDescriptor.Builder(method, args.length, localVariableNameDiscoverer).build();
                    ExceptionDescriptor exceptionDescriptor = new ExceptionDescriptor.Builder(logExceptionInfo).build();
                    InvocationDescriptor invocationDescriptor = invocationDescriptorFactory.create(methodLogInfo, classLogInfo);
                    cached = new MethodDescriptor(invocationDescriptor, argumentDescriptor, exceptionDescriptor, method);
                    cache.put(new LoggedMethodIdentifier(method,targetClass), cached);
                    return cached;
                }
            }
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
                    logStrategies.get(excSeverity.getSeverity())
                            .logException(logger, method, args.length, e, excSeverity.getStackTrace());
                }
            }
        }

        void logInvocation(){
            logStrategies.get(invocationDescriptor.getSeverity())
                    .logBefore(logger, method, args, argumentDescriptor);
        }

        void logResult(){
            Object loggedResult = (method.getReturnType() == Void.TYPE) ? Void.TYPE : result;
            logStrategies.get(invocationDescriptor.getSeverity())
                    .logAfter(logger, method, args.length, loggedResult);
        }

        boolean isExceptionLoggingOn(){
            SourceAwareAnnotationInfo<LogException> logExceptionInfo = exceptionDescriptor.getExceptionAnnotationInfo();
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

    //todo maybe need to be changed
    private Method extractMethod(Class<?> targetClass, ProceedingJoinPoint joinPoint) throws NoSuchMethodException {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        // signature.getMethod() points to method declared in interface. it is not suit to discover arg names and arg annotations
        // see AopProxyUtils: org.springframework.cache.interceptor.CacheAspectSupport#execute(CacheAspectSupport.Invoker, Object, Method, Object[])
//        Class<?> targetClass = joinPoint.getTarget().getClass();
        if (Modifier.isPublic(signature.getMethod().getModifiers())) {
            return targetClass.getMethod(signature.getName(), signature.getParameterTypes());
        } else {
            return ReflectionUtils.findMethod(targetClass, signature.getName(), signature.getParameterTypes());
        }
    }


}
