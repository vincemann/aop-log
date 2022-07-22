/***********************************************************************************
 * Copyright (c) 2013. Nickolay Gerilovich. Russia.
 *   Some Rights Reserved.
 ************************************************************************************/

package com.github.vincemann.aoplog;

import com.github.vincemann.aoplog.api.CustomLogger;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Abstract log adapter.
 */
abstract class AbstractLogAdapter implements LogAdapter {

    protected static final String CALLING = "     ->  CALLING: ";
    protected static final String RETURNING = "     <-  RETURNING: ";
    protected static final String THROWING = "     <-  THROWING: ";
    protected static final String ARG_DELIMITER = " |==| ";


    @Override
    public Log getLog(Class clazz) {
        return LogFactory.getLog(clazz);
    }

    @Override
    public Log getLog(String name) {
        return LogFactory.getLog(name);
    }

    @Override
    public Object toMessage(Method method, String beanName, Object[] args, ArgumentDescriptor argumentDescriptor, Set<CustomLoggerInfo> customLoggerInfo) {
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
                    buff.append(asString(args[i], selectCustomLogger(customLoggerInfo, CustomLoggerInfo.Type.ARG,i+1)));
                    buff.append(ARG_DELIMITER);
                } else {
                    buff.append(ARG_DELIMITER + "?" + ARG_DELIMITER);
                }
            }
        } else {
            for (int i = argumentDescriptor.nextLoggedArgumentIndex(0); i >= 0; i = argumentDescriptor.nextLoggedArgumentIndex(i + 1)) {
                buff.append(names[i]).append('=').append(asString(args[i], selectCustomLogger(customLoggerInfo, CustomLoggerInfo.Type.ARG, i + 1)));
                buff.append(ARG_DELIMITER);
            }
        }
        // dont know what that is good for
//        if (argumentDescriptor.nextLoggedArgumentIndex(0) != -1) {
//            buff.setLength(buff.length() - 2);
//        }
        // remove trailing arg delimiter
        buff.setLength(buff.length()-ARG_DELIMITER.length());
        buff.append(')');
        return buff.toString();
    }

    protected CustomLogger selectCustomLogger(Set<CustomLoggerInfo> customLoggerInfo, CustomLoggerInfo.Type type, Integer... argNum){
        Set<CustomLoggerInfo> loggers;
        if (customLoggerInfo == null)
        {
            return null;
        }
        if (customLoggerInfo.isEmpty()){
            return null;
        }
        switch (type){
            case ARG:
                loggers = customLoggerInfo.stream()
                        .filter(loggerInfo -> loggerInfo.getType().equals(CustomLoggerInfo.Type.ARG) && loggerInfo.getArgNum().equals(argNum[0]))
                        .collect(Collectors.toSet());
                if (loggers.isEmpty()){
                    return null;
                }
                else if (loggers.size() > 1){
                    throw new IllegalArgumentException("found multiple ARG loggers");
                }
                return loggers.stream().findFirst().get().getLogger();
            case RET:
                loggers = customLoggerInfo.stream()
                        .filter(loggerInfo -> loggerInfo.getType().equals(CustomLoggerInfo.Type.RET))
                        .collect(Collectors.toSet());
                if (loggers.isEmpty()){
                    return null;
                }
                else if (loggers.size() > 1){
                    throw new IllegalArgumentException("found multiple RET loggers");
                }
                return loggers.stream().findFirst().get().getLogger();
        }
        throw new IllegalArgumentException("Wrong arg type set");
    }

    @Override
    public Object toMessage(Method method,String beanName, int argCount, Object result, CustomLoggerInfo customLoggerInfo) {
//        if (result == null) {
//            return RETURNING + method.getName() + "():" + asString(result);
//        }
        StringBuilder buff = new StringBuilder();
        buff.append(RETURNING).append(method.getName()).append(" { ").append(asString(result, selectCustomLogger(customLoggerInfo, CustomLoggerInfo.Type.ARG, i + 1))).append(" } ");
        return buff.toString();
    }

    @Override
    public Object toMessage(Method method,String beanName, int argCount, Exception e, boolean stackTrace) {
        StringBuilder buff = new StringBuilder();
        buff.append(THROWING).append(method.getName()).append(" { ").append(e.getClass()).append(" } ");
        if (e.getMessage() != null) {
            buff.append("=").append(e.getMessage());
        }
        return buff.toString();
    }

    protected abstract String asString(Object value, CustomLogger customLogger);

}
