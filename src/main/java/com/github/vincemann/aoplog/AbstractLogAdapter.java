/***********************************************************************************
 * Copyright (c) 2013. Nickolay Gerilovich. Russia.
 *   Some Rights Reserved.
 ************************************************************************************/

package com.github.vincemann.aoplog;

import com.github.vincemann.aoplog.api.CustomLogger;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Abstract log adapter.
 */
abstract class AbstractLogAdapter implements LogAdapter {

    protected static final String CALLING = "     ->  CALLING: ";
    protected static final String RETURNING = "     <-  RETURNING: ";
    protected static final String THROWING = "     <-  THROWING: ";
    protected String argDelimiter = System.lineSeparator() + " |==| " + System.lineSeparator();

    private static final Map<CustomLoggerCacheKey,CacheHit<CustomLogger>> CUSTOM_LOGGER_CACHE = new HashMap<>();
    private static final Map<CustomToStringCacheKey,CacheHit<String>> CUSTOM_TO_STRING_CACHE = new HashMap<>();


    @Override
    public Log getLog(Class clazz) {
        return LogFactory.getLog(clazz);
    }

    @Override
    public Log getLog(String name) {
        return LogFactory.getLog(name);
    }

    @Override
    public Object toMessage(Method method, String beanName, Object[] args, ArgumentDescriptor argumentDescriptor, Set<CustomLoggerInfo> customLoggerInfos, Set<CustomToStringInfo> customToStringInfos) {
        if (args.length == 0) {
            StringBuilder buff = new StringBuilder();
            buff.append(CALLING).append(method.getName()).append("()");
            return buff.toString();
        }

        String[] names = argumentDescriptor.getNames();
        StringBuilder buff = new StringBuilder();
        buff.append(CALLING).append(method.getName()).append('(');
        if (args.length > 1) {
            buff.append(args.length).append(" arguments: ");
        }
        if (names == null) {
            for (int i = 0; i < args.length; i++) {
                if (argumentDescriptor.isArgumentIndexLogged(i)) {
                    buff.append(
                            asString(args[i],
                                    selectCustomLogger(customLoggerInfos, LoggableMethodPart.Type.ARG,i+1),
                                    selectCustomToStringMethod(customToStringInfos, LoggableMethodPart.Type.ARG, i + 1)
                            )
                    );
                    buff.append(argDelimiter);
                } else {
                    buff.append(argDelimiter + "?" + argDelimiter);
                }
            }
        } else {
            for (int i = argumentDescriptor.nextLoggedArgumentIndex(0); i >= 0; i = argumentDescriptor.nextLoggedArgumentIndex(i + 1)) {
                buff.append(names[i])
                        .append('=')
                        .append(asString(args[i],
                                selectCustomLogger(customLoggerInfos, LoggableMethodPart.Type.ARG, i + 1),
                                selectCustomToStringMethod(customToStringInfos,LoggableMethodPart.Type.ARG, i + 1))
                        );
                buff.append(argDelimiter);
            }
        }
        // dont know what that is good for
//        if (argumentDescriptor.nextLoggedArgumentIndex(0) != -1) {
//            buff.setLength(buff.length() - 2);
//        }
        // remove trailing arg delimiter
        buff.setLength(buff.length()- argDelimiter.length());
        buff.append(')');
        return buff.toString();
    }


    // returns method name of method to be called instead of toString()
    // return null if none found
    private String selectCustomToStringMethod(Set<CustomToStringInfo> customToStringInfos, LoggableMethodPart.Type type, Integer... argNum) {
        CustomToStringCacheKey cacheKey = new CustomToStringCacheKey(customToStringInfos, type, List.of(argNum));
        CacheHit<String> cached = CUSTOM_TO_STRING_CACHE.get(cacheKey);
        if (cached != null)
            return cached.getElement();

        Set<CustomToStringInfo> toStringInfos;
        String customToStringMethod = null;
        if (customToStringInfos != null)
        {
            if (!customToStringInfos.isEmpty()){
                switch (type){
                    case ARG:
                        toStringInfos = customToStringInfos.stream()
                                .filter(loggerInfo -> loggerInfo.getMethodPart().getType().equals(LoggableMethodPart.Type.ARG)
                                        && loggerInfo.getMethodPart().getArgNum().equals(argNum[0]))
                                .collect(Collectors.toSet());
                        if (toStringInfos.isEmpty()){
                            break;
                        }
                        else if (toStringInfos.size() > 1){
                            throw new IllegalArgumentException("found multiple ARG toStringInfos");
                        }
                        else {
                            customToStringMethod =  toStringInfos.stream().findFirst().get().getMethodName();
                        }
                        break;
                    case RET:
                        toStringInfos = customToStringInfos.stream()
                                .filter(loggerInfo -> loggerInfo.getMethodPart().getType().equals(LoggableMethodPart.Type.RET))
                                .collect(Collectors.toSet());
                        if (toStringInfos.isEmpty()){
                            break;
                        }
                        else if (toStringInfos.size() > 1){
                            throw new IllegalArgumentException("found multiple RET toStringInfos");
                        }else {
                            customToStringMethod = toStringInfos.stream().findFirst().get().getMethodName();
                        }
                        break;
                }
            }
        }
        CUSTOM_TO_STRING_CACHE.put(cacheKey,new CacheHit<>(customToStringMethod));
        return customToStringMethod;
    }


    // use this class so I can express a cache hit, that represents null
    // -> I want to also cache the result, that nothing was found
    @AllArgsConstructor
    @Getter
    private static class CacheHit<T>{
        private T element;
    }

    @EqualsAndHashCode
    @AllArgsConstructor
    private static class CustomLoggerCacheKey{
        Set<CustomLoggerInfo> customLoggerInfos;
        LoggableMethodPart.Type type;
        List<Integer> argNums;
    }

    @EqualsAndHashCode
    @AllArgsConstructor
    private static class CustomToStringCacheKey{
        Set<CustomToStringInfo> customToStringInfos;
        LoggableMethodPart.Type type;
        List<Integer> argNums;

    }

    // returns null if none found
    protected CustomLogger selectCustomLogger(Set<CustomLoggerInfo> customLoggerInfos, LoggableMethodPart.Type type, Integer... argNum){
        CustomLoggerCacheKey cacheKey = new CustomLoggerCacheKey(customLoggerInfos, type, List.of(argNum));
        CacheHit<CustomLogger> cached = CUSTOM_LOGGER_CACHE.get(cacheKey);
        if (cached != null)
            return cached.getElement();

        Set<CustomLoggerInfo> loggers;
        CustomLogger logger = null;
        if (customLoggerInfos != null)
        {
            if (!customLoggerInfos.isEmpty()){
                switch (type){
                    case ARG:
                        loggers = customLoggerInfos.stream()
                                .filter(loggerInfo -> loggerInfo.getMethodPart().getType().equals(LoggableMethodPart.Type.ARG)
                                        && loggerInfo.getMethodPart().getArgNum().equals(argNum[0]))
                                .collect(Collectors.toSet());
                        if (loggers.isEmpty()){
                            break;
                        }
                        else if (loggers.size() > 1){
                            throw new IllegalArgumentException("found multiple ARG loggers");
                        }
                        else {
                            logger =  loggers.stream().findFirst().get().getLogger();
                        }
                        break;
                    case RET:
                        loggers = customLoggerInfos.stream()
                                .filter(loggerInfo -> loggerInfo.getMethodPart().getType().equals(LoggableMethodPart.Type.RET))
                                .collect(Collectors.toSet());
                        if (loggers.isEmpty()){
                            break;
                        }
                        else if (loggers.size() > 1){
                            throw new IllegalArgumentException("found multiple RET loggers");
                        }else {
                            logger = loggers.stream().findFirst().get().getLogger();
                        }
                        break;
                }
            }
        }
        CUSTOM_LOGGER_CACHE.put(cacheKey,new CacheHit<>(logger));
        return logger;
    }

    @Override
    public Object toMessage(Method method, String beanName, int argCount, Object result, Set<CustomLoggerInfo> customLoggerInfo, Set<CustomToStringInfo> customToStringInfos) {
//        if (result == null) {
//            return RETURNING + method.getName() + "():" + asString(result);
//        }
        StringBuilder buff = new StringBuilder();
        buff.append(RETURNING).append(method.getName())
                .append(" { ")
                .append(
                        asString(result,
                                selectCustomLogger(customLoggerInfo, LoggableMethodPart.Type.RET),
                                selectCustomToStringMethod(customToStringInfos, LoggableMethodPart.Type.RET)))
                .append(" } ");
        return buff.toString();
    }

    @Override
    public Object toMessage(Method method,String beanName, int argCount, Exception e, boolean stackTrace) {
        StringBuilder buff = new StringBuilder();
        buff.append(THROWING)
                .append(method.getName())
                .append(" { ")
                .append(e.getClass())
                .append(" } ");
        if (e.getMessage() != null) {
            buff.append("=").append(e.getMessage());
        }
        return buff.toString();
    }

    protected abstract String asString(Object value, CustomLogger customLogger, String customToString);


    public void setArgDelimiter(String argDelimiter) {
        this.argDelimiter = argDelimiter;
    }
}
