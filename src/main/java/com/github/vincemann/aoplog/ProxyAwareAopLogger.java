package com.github.vincemann.aoplog;

import com.github.vincemann.aoplog.api.Log;
import com.github.vincemann.aoplog.api.LogAll;
import com.github.vincemann.aoplog.api.LogException;
import com.github.vincemann.aoplog.api.UltimateTargetClassAware;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.lang.Nullable;
import org.springframework.test.util.AopTestUtils;
import org.springframework.util.ReflectionUtils;


import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.ConcurrentMap;
import java.util.EnumMap;
import java.util.Map;
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

    @Override
    public void afterPropertiesSet() throws Exception {
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

    public ProxyAwareAopLogger(AnnotationParser annotationParser) {
        this.annotationParser = annotationParser;
    }

    @Around("this(com.github.vincemann.aoplog.api.InteractionLoggable)")
    public Object log(ProceedingJoinPoint joinPoint) throws Throwable {
        LoggedMethodCall loggedCall = new LoggedMethodCall(joinPoint,findTargetClass(joinPoint));

        if (!loggedCall.methodWanted() || (!loggedCall.isLoggingOn() && !loggedCall.isExceptionLoggingOn())){
            return loggedCall.proceed();
        }

        if (loggedCall.isLoggingOn()) {
            loggedCall.logInvocation();
        }

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

        if (loggedCall.isLoggingOn()) {
            loggedCall.logResult();
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
        Log methodLog;
        LogAll classLog;
        //stores NOT DISABLED @Log from method or @LogAll's config()
        AnnotationInfo<Log> logInfo;
        boolean foundAny;


        AnnotationInfo<LogException> logExceptionInfo;
        MethodDescriptor methodDescriptor;
        InvocationDescriptor invocationDescriptor;
        ArgumentDescriptor argumentDescriptor;
        ProceedingJoinPoint joinPoint;
        org.apache.commons.logging.Log logger;

        Object result;

        LoggedMethodCall(ProceedingJoinPoint joinPoint,Class<?> targetClass) throws NoSuchMethodException {
            this.joinPoint = joinPoint;
            this.method = extractMethod(joinPoint);
            this.targetClass = targetClass;
            this.methodLog = annotationParser.fromMethod(method, Log.class);
            this.classLog = annotationParser.fromClass(targetClass,LogAll.class);
            this.foundAny = methodLog!=null || classLog!=null;
            this.logInfo = evalLogInfo();
            this.logExceptionInfo = annotationParser.fromMethodOrClass(method,LogException.class);
            this.methodDescriptor = evalMethodDescriptor(method,logInfo,logExceptionInfo);
            this.invocationDescriptor = methodDescriptor.getInvocationDescriptor();
            this.args = joinPoint.getArgs();
            this.logger = logAdapter.getLog(targetClass);
            this.argumentDescriptor=evalArgumentDescriptor(methodDescriptor,method,args.length);
        }

        //finds non disabled @Log if any
        //first search for method, if method disabled or not found : fall back on class level
        private AnnotationInfo<Log> evalLogInfo(){
            boolean methodFound = !(methodLog==null);
            boolean classFound = !(classLog==null);
            Boolean methodDisabled = methodLog==null? null :  methodLog.disabled();
            Boolean classDisabled = classLog==null? null :  classLog.config().disabled();
            if (methodFound){
                if (methodDisabled){
                    if (classFound && !classDisabled){
                        return new AnnotationInfo<>(classLog.config(),true);
                    }
                }else {
                    return new AnnotationInfo<>(methodLog,false);
                }
            }else {
                if (classFound && !classDisabled){
                    return new AnnotationInfo<>(classLog.config(),true);
                }
            }
            return null;
        }

        boolean methodWanted(){
            if (logInfo==null){
                return false;
            }
            if (classLog!=null){
                if (classLog.ignoreGetters() && (method.getName().startsWith("get") || method.getName().startsWith("is"))){
                    return false;
                }
                if (classLog.ignoreSetters() && (method.getName().startsWith("set"))){
                    return false;
                }
            }
            return true;
        }

        Object proceed() throws Throwable {
            result = joinPoint.proceed(args);
            return result;
        }

        void logException(Exception e){
            ExceptionDescriptor exceptionDescriptor = evalExceptionDescriptor(methodDescriptor, invocationDescriptor);
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

        boolean isLoggingOn(){
            return isLoggingOn(invocationDescriptor.getSeverity());
        }

        private boolean isLoggingOn(Severity severity) {
            return severity != null && logStrategies.get(severity).isLogEnabled(logger);
        }
    }



    private MethodDescriptor evalMethodDescriptor(Method method, AnnotationInfo<Log> log,  @Nullable AnnotationInfo<LogException> logExceptionInfo) {
        MethodDescriptor cached = cache.get(method);
        if (cached != null) {
            return cached;
        }
        cached = new MethodDescriptor(new InvocationDescriptor.Builder(log,logExceptionInfo).build());
        MethodDescriptor prev = cache.putIfAbsent(method, cached);
        return prev == null ? cached : prev;
    }

    private ArgumentDescriptor evalArgumentDescriptor(MethodDescriptor descriptor, Method method, int argumentCount) {
        if (descriptor.getArgumentDescriptor() != null) {
            return descriptor.getArgumentDescriptor();
        }
        ArgumentDescriptor argumentDescriptor = new ArgumentDescriptor.Builder(method, argumentCount, localVariableNameDiscoverer).build();
        descriptor.setArgumentDescriptor(argumentDescriptor);
        return argumentDescriptor;
    }

    private ExceptionDescriptor evalExceptionDescriptor(MethodDescriptor descriptor, InvocationDescriptor invocationDescriptor) {
        if (descriptor.getExceptionDescriptor() != null) {
            return descriptor.getExceptionDescriptor();
        }
        ExceptionDescriptor exceptionDescriptor = new ExceptionDescriptor.Builder(invocationDescriptor.getExceptionAnnotation()).build();
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
