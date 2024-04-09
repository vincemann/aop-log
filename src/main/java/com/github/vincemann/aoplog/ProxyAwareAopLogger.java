package com.github.vincemann.aoplog;

import com.github.vincemann.aoplog.annotation.AnnotationInfo;
import com.github.vincemann.aoplog.annotation.AnnotationParser;
import com.github.vincemann.aoplog.annotation.SourceAwareAnnotationInfo;
import com.github.vincemann.aoplog.api.annotation.CustomLogger;
import com.github.vincemann.aoplog.api.annotation.CustomToString;
import com.github.vincemann.aoplog.api.annotation.LogException;
import com.github.vincemann.aoplog.api.annotation.LogInteraction;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


@Aspect
public class ProxyAwareAopLogger implements InitializingBean, ApplicationContextAware {

    private final Log log = LogFactory.getLog(ProxyAwareAopLogger.class);

    private final LocalVariableTableParameterNameDiscoverer localVariableNameDiscoverer = new LocalVariableTableParameterNameDiscoverer();
    private final ExceptionResolver exceptionResolver = new ExceptionResolver();
    private final ConcurrentMap<LoggedMethodIdentifier, MethodDescriptor> cache = new ConcurrentHashMap<>();
    //I add this impl bc otherwise the ignoreGetters and ignoreSetters in @LogConfig, that is handled by this component, would be ignored,
    // which would be unexpected behavior
    private final List<MethodFilter> methodFilters = Lists.newArrayList(new LogConfigMethodFilter());
    // private static final Log LOGGER = LogFactory.getLog(AOPLogger.class);
    private LogAdapter logAdapter;
    private Map<Severity, LogStrategy> logStrategies;
    private AnnotationParser annotationParser;
    private InvocationDescriptorFactory invocationDescriptorFactory;
    private ConcurrentHashMap<Thread, LoggedMethodCall> thread_lastLoggedCall_map = new ConcurrentHashMap<>();
    private CustomLoggerInfoFactory customLoggerInfoFactory;

    private ApplicationContext applicationContext;

    public ProxyAwareAopLogger(AnnotationParser annotationParser, InvocationDescriptorFactory invocationDescriptorFactory, CustomLoggerInfoFactory customLoggerInfoFactory, MethodFilter... methodFilters) {
        this.annotationParser = annotationParser;
        this.invocationDescriptorFactory = invocationDescriptorFactory;
        this.customLoggerInfoFactory = customLoggerInfoFactory;
        this.methodFilters.addAll(Lists.newArrayList(methodFilters));
    }

    public ProxyAwareAopLogger() {
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() {
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

    @Around("target(com.github.vincemann.aoplog.api.AopLoggable)")
    public Object log(ProceedingJoinPoint joinPoint) throws Throwable {
        LoggedMethodCall loggedCall = new LoggedMethodCall(joinPoint);
        if (log.isDebugEnabled())
            log.debug("LoggedMethodCall:  " + loggedCall);


        if (isDuplicateProxyLogging(loggedCall)) {
            if (log.isDebugEnabled())
                log.debug("Skipping duplicate logging of proxy call");
            return loggedCall.proceed();
        } else {
            thread_lastLoggedCall_map.put(Thread.currentThread(), loggedCall);
        }


        //method filter only restricts interaction logging, logException is independent subsystem
        for (MethodFilter methodFilter : methodFilters) {
            if (!methodFilter.wanted(loggedCall.methodDescriptor)) {
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

    protected boolean isDuplicateProxyLogging(LoggedMethodCall call) {
        LoggedMethodCall precedingCall = thread_lastLoggedCall_map.get(Thread.currentThread());
        if (precedingCall != null) {
            if (call.getMethodDescriptor().equals(precedingCall.getMethodDescriptor()) &&
                    StringUtils.equals(call.getBeanName(), precedingCall.getBeanName())) {
                return true;
            }
        }
        return false;
    }

    protected Object proceedWithExceptionLogging(LoggedMethodCall loggedCall) throws Throwable {
        try {
            loggedCall.proceed();
        } catch (Exception e) {
            //exception was not catched by logged Method
            if (loggedCall.isExceptionLoggingOn()) {
                loggedCall.logException(e);
            } else {
                if (loggedCall.isInteractionLoggingOn()) {
                    loggedCall.logInteractionException(e);
                }
            }
            throw e;
        }
        return loggedCall.getResult();
    }

    //todo maybe need to be changed
    private Method extractMethod(Class<?> targetClass, ProceedingJoinPoint joinPoint) throws NoSuchMethodException {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
//        Class<?> targetClass = joinPoint.getTarget().getClass();
        // signature.getMethod() points to method declared in interface. it is not suit to discover arg names and arg annotations
        // see AopProxyUtils: org.springframework.cache.interceptor.CacheAspectSupport#execute(CacheAspectSupport.Invoker, Object, Method, Object[])
//        Class<?> targetClass = joinPoint.getTarget().getClass();
        if (Modifier.isPublic(signature.getMethod().getModifiers())) {
            return targetClass.getMethod(signature.getName(), signature.getParameterTypes());
        } else {
            return ReflectionUtils.findMethod(targetClass, signature.getName(), signature.getParameterTypes());
        }
    }


    private static class LoggedMethodIdentifier {
        private String name;
        private Class<?>[] argTypes;
        private Class<?> targetClass;

        LoggedMethodIdentifier(Method method, Class<?> targetClass) {
            this.name = method.getName();
            this.argTypes = method.getParameterTypes();
            this.targetClass = targetClass;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;

            if (!(o instanceof LoggedMethodIdentifier)) return false;

            LoggedMethodIdentifier that = (LoggedMethodIdentifier) o;

            return new EqualsBuilder().append(getName(), that.getName()).append(getArgTypes(), that.getArgTypes()).append(getTargetClass(), that.getTargetClass()).isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37).append(getName()).append(getArgTypes()).append(getTargetClass()).toHashCode();
        }

        @Override
        public String toString() {
            return "LoggedMethodIdentifier{" +
                    "name='" + name + '\'' +
                    ", argTypes=" + Arrays.toString(argTypes) +
                    ", targetClass=" + targetClass +
                    '}';
        }

        public String getName() {
            return name;
        }

        public Class<?>[] getArgTypes() {
            return argTypes;
        }

        public Class<?> getTargetClass() {
            return targetClass;
        }
    }

    public class LoggedMethodCall {
        Method method;
        Class<?> targetClass;
        String beanName;
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

        LoggedMethodCall(ProceedingJoinPoint joinPoint) throws NoSuchMethodException {
            this.joinPoint = joinPoint;
            this.args = joinPoint.getArgs();
            this.targetClass = AopProxyUtils.ultimateTargetClass(joinPoint.getTarget());
            this.method = extractMethod(targetClass,joinPoint);
            this.beanName = findBeanName(targetClass);
            this.methodDescriptor = createMethodDescriptor();
            this.exceptionDescriptor = methodDescriptor.getExceptionDescriptor();
            this.invocationDescriptor = methodDescriptor.getInvocationDescriptor();
            this.argumentDescriptor = methodDescriptor.getArgumentDescriptor();
            this.logger = logAdapter.getLog(targetClass);
        }

        public LoggedMethodCall(Method method, Class<?> targetClass, String beanName, Class<?> logClass, Object[] args, MethodDescriptor methodDescriptor, ArgumentDescriptor argumentDescriptor, ExceptionDescriptor exceptionDescriptor, InvocationDescriptor invocationDescriptor, ProceedingJoinPoint joinPoint, Log logger, Object result) {
            this.method = method;
            this.targetClass = targetClass;
            this.beanName = beanName;
            this.logClass = logClass;
            this.args = args;
            this.methodDescriptor = methodDescriptor;
            this.argumentDescriptor = argumentDescriptor;
            this.exceptionDescriptor = exceptionDescriptor;
            this.invocationDescriptor = invocationDescriptor;
            this.joinPoint = joinPoint;
            this.logger = logger;
            this.result = result;
        }

        protected String findBeanName(Class<?> targetClass) {
            String[] allBeanNames = applicationContext.getBeanNamesForType(targetClass, true, false);
            if (allBeanNames.length == 1)
                return allBeanNames[0];
            else
                return "";
        }

        private Set<CustomToStringInfo> parseCustomToStringInfos() {
            Set<CustomToStringInfo> infos = new HashSet<>();
            Set<AnnotationInfo<CustomToString>> customToStringAnnotations = annotationParser.repeatableFromDeclaredMethod(targetClass, method.getName(), method.getParameterTypes(), CustomToString.class);
            for (AnnotationInfo<CustomToString> customToStringAnnotation : customToStringAnnotations) {
                CustomToString annotation = customToStringAnnotation.getAnnotation();
                infos.add(
                        new CustomToStringInfo(annotation.toStringMethod(), LoggableMethodPart.from(annotation.key()))
                );
            }
            return infos;
        }

        protected Set<CustomLoggerInfo> parseCustomLoggerInfos(){
            Set<CustomLoggerInfo> infos = new HashSet<>();
            Set<AnnotationInfo<CustomLogger>> customLoggerAnnotations = annotationParser.repeatableFromDeclaredMethod(targetClass, method.getName(), method.getParameterTypes(), CustomLogger.class);
            for (AnnotationInfo<CustomLogger> customLoggerAnnotation : customLoggerAnnotations) {
                CustomLogger annotation =customLoggerAnnotation.getAnnotation();
                CustomLoggerInfo info = customLoggerInfoFactory.createCustomLoggerInfo(annotation);
                infos.add(info);
            }
            return infos;
        }

        private MethodDescriptor createMethodDescriptor() {
            if (log.isTraceEnabled()){
                log.trace("Searching for method Descriptor in cache with key: " + new LoggedMethodIdentifier(method, targetClass));
                log.trace("cache:: " + cache);
            }

            synchronized (cache) {
                MethodDescriptor cached = cache.get(new LoggedMethodIdentifier(method, targetClass));
                if (cached != null) {
                    //log.trace("key: " + new LoggedMethodIdentifier(method,targetClass));
                    if (log.isTraceEnabled())
                        log.trace("Returning method Descriptor from cache: " + cached);
                    return cached;
                } else {
                    AnnotationInfo<LogInteraction> methodLogInfo = annotationParser.fromMethod(targetClass, method.getName(), method.getParameterTypes(), LogInteraction.class);
                    AnnotationInfo<LogInteraction> classLogInfo = annotationParser.fromClass(targetClass, LogInteraction.class);
                    Set<CustomLoggerInfo> customLoggerInfos = parseCustomLoggerInfos();
                    Set<CustomToStringInfo> customToStringInfos = parseCustomToStringInfos();

                    SourceAwareAnnotationInfo<LogException> logExceptionInfo = annotationParser.fromMethodOrClass(method, LogException.class);
                    ArgumentDescriptor argumentDescriptor = new ArgumentDescriptor.Builder(method, args.length, localVariableNameDiscoverer).build();
                    ExceptionDescriptor exceptionDescriptor = new ExceptionDescriptor.Builder(logExceptionInfo).build();
                    InvocationDescriptor invocationDescriptor = invocationDescriptorFactory.create(methodLogInfo, classLogInfo, customLoggerInfos,customToStringInfos);
                    cached = new MethodDescriptor(invocationDescriptor, argumentDescriptor, exceptionDescriptor, method);
                    cache.put(new LoggedMethodIdentifier(method, targetClass), cached);
                    return cached;
                }
            }
        }


        @Override
        public int hashCode() {
            return Objects.hashCode(getMethodDescriptor(), getArgumentDescriptor(), getExceptionDescriptor(), getInvocationDescriptor());
        }

        Object proceed() throws Throwable {
            result = joinPoint.proceed(args);
            return result;
        }

        void logException(Exception e) {
            Class<? extends Exception> resolved = exceptionResolver.resolve(exceptionDescriptor, e);
            if (resolved != null) {
                ExceptionSeverity excSeverity = exceptionDescriptor.getExceptionSeverity(resolved);
                if (isLoggingOn(excSeverity.getSeverity())) {
                    logStrategies.get(excSeverity.getSeverity())
                            .logException(logger, method, beanName, args.length, e, excSeverity.getStackTrace());
                }
            }
        }

        void logInteractionException(Exception e) {
            Severity severity = invocationDescriptor.getSeverity();
            if (isLoggingOn(severity)) {
                logStrategies.get(severity)
                        .logException(logger, method, beanName, args.length, e, false);
            }
        }


        void logInvocation() {
            logStrategies.get(invocationDescriptor.getSeverity())
                    .logBefore(logger, method, beanName, args, argumentDescriptor,invocationDescriptor.getCustomLoggerInfos(),invocationDescriptor.getCustomToStringInfos());
        }

        void logResult() {
            Object loggedResult = (method.getReturnType() == Void.TYPE) ? Void.TYPE : result;
            logStrategies.get(invocationDescriptor.getSeverity())
                    .logAfter(logger, method, beanName, args.length, loggedResult,invocationDescriptor.getCustomLoggerInfos(),invocationDescriptor.getCustomToStringInfos());
        }

        boolean isExceptionLoggingOn() {
            SourceAwareAnnotationInfo<LogException> logExceptionInfo = exceptionDescriptor.getExceptionAnnotationInfo();
            if (logExceptionInfo == null) {
                return false;
            }
            return (logExceptionInfo.getAnnotation().value().length > 0 && isLoggingOn(Severity.ERROR)) ||
                    (logExceptionInfo.getAnnotation().fatal().length > 0 && isLoggingOn(Severity.FATAL)) ||
                    (logExceptionInfo.getAnnotation().trace().length > 0 && isLoggingOn(Severity.TRACE)) ||
                    (logExceptionInfo.getAnnotation().debug().length > 0 && isLoggingOn(Severity.DEBUG)) ||
                    (logExceptionInfo.getAnnotation().info().length > 0 && isLoggingOn(Severity.INFO)) ||
                    (logExceptionInfo.getAnnotation().warn().length > 0 && isLoggingOn(Severity.WARN));
        }

        boolean isInteractionLoggingOn() {
            return isLoggingOn(invocationDescriptor.getSeverity());
        }

        private boolean isLoggingOn(Severity severity) {
            return severity != null && logStrategies.get(severity).isLogEnabled(logger);
        }

//        @Override
//        public String toString() {
//            return LazyInitLogUtils.toString(this);
//        }

        public Method getMethod() {
            return method;
        }

        public Class<?> getTargetClass() {
            return targetClass;
        }

        public String getBeanName() {
            return beanName;
        }

        public Class<?> getLogClass() {
            return logClass;
        }

        public Object[] getArgs() {
            return args;
        }

        public MethodDescriptor getMethodDescriptor() {
            return methodDescriptor;
        }

        public ArgumentDescriptor getArgumentDescriptor() {
            return argumentDescriptor;
        }

        public ExceptionDescriptor getExceptionDescriptor() {
            return exceptionDescriptor;
        }

        public InvocationDescriptor getInvocationDescriptor() {
            return invocationDescriptor;
        }

        public ProceedingJoinPoint getJoinPoint() {
            return joinPoint;
        }

        public Log getLogger() {
            return logger;
        }

        public Object getResult() {
            return result;
        }
    }

    public LocalVariableTableParameterNameDiscoverer getLocalVariableNameDiscoverer() {
        return localVariableNameDiscoverer;
    }

    public ExceptionResolver getExceptionResolver() {
        return exceptionResolver;
    }

    public List<MethodFilter> getMethodFilters() {
        return methodFilters;
    }

    public LogAdapter getLogAdapter() {
        return logAdapter;
    }

    public Map<Severity, LogStrategy> getLogStrategies() {
        return logStrategies;
    }

    public AnnotationParser getAnnotationParser() {
        return annotationParser;
    }

    public InvocationDescriptorFactory getInvocationDescriptorFactory() {
        return invocationDescriptorFactory;
    }

    public CustomLoggerInfoFactory getCustomLoggerInfoFactory() {
        return customLoggerInfoFactory;
    }
}
