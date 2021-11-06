/***********************************************************************************
 * Copyright (c) 2013. Nickolay Gerilovich. Russia.
 *   Some Rights Reserved.
 ************************************************************************************/

package com.github.vincemann.aoplog;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Method;

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
    public Object toMessage(Method method,String beanName, Object[] args, ArgumentDescriptor argumentDescriptor) {
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
                    buff.append(asString(args[i]));
                    buff.append(ARG_DELIMITER);
                } else {
                    buff.append(ARG_DELIMITER + "?" + ARG_DELIMITER);
                }
            }
        } else {
            for (int i = argumentDescriptor.nextLoggedArgumentIndex(0); i >= 0; i = argumentDescriptor.nextLoggedArgumentIndex(i + 1)) {
                buff.append(names[i]).append('=').append(asString(args[i]));
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

    @Override
    public Object toMessage(Method method,String beanName, int argCount, Object result) {
//        if (result == null) {
//            return RETURNING + method.getName() + "():" + asString(result);
//        }
        StringBuilder buff = new StringBuilder();
        buff.append(RETURNING).append(method.getName()).append(" { ").append(asString(result)).append(" } ");
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

    protected abstract String asString(Object value);

}
