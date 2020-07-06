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

    protected static final String CALLING = "calling: ";
    protected static final String RETURNING = "returning: ";
    protected static final String THROWING = "throwing: ";

    @Override
    public Log getLog(Class clazz) {
        return LogFactory.getLog(clazz);
    }

    @Override
    public Log getLog(String name) {
        return LogFactory.getLog(name);
    }

    @Override
    public Object toMessage(Method method, Object[] args, ArgumentDescriptor argumentDescriptor) {
        if (args.length == 0) {
            return CALLING + method + "()";
        }

        String[] names = argumentDescriptor.getNames();
        StringBuilder buff = new StringBuilder(CALLING).append(method).append('(');
        if (args.length > 1) {
            buff.append(args.length).append(" arguments: ");
        }
        if (names == null) {
            for (int i = 0; i < args.length; i++) {
                if (argumentDescriptor.isArgumentIndexLogged(i)) {
                    buff.append(asString(args[i]));
                    buff.append(", ");
                } else {
                    buff.append("?, ");
                }
            }
        } else {
            for (int i = argumentDescriptor.nextLoggedArgumentIndex(0); i >= 0; i = argumentDescriptor.nextLoggedArgumentIndex(i + 1)) {
                buff.append(names[i]).append('=').append(asString(args[i]));
                buff.append(", ");
            }
        }
        if (argumentDescriptor.nextLoggedArgumentIndex(0) != -1) {
            buff.setLength(buff.length() - 2);
        }
        buff.append(')');
        return buff.toString();
    }

    @Override
    public Object toMessage(Method method, int argCount, Object result) {
        if (argCount == 0) {
            return RETURNING + method + "():" + asString(result);
        }
        return RETURNING + method + '(' + argCount + " arguments):" + asString(result);
    }

    @Override
    public Object toMessage(Method method, int argCount, Exception e, boolean stackTrace) {
        String message;
        if (argCount == 0) {
            message = THROWING + method + "():" + e.getClass();
        } else {
            message = THROWING + method + '(' + argCount + " arguments):" + e.getClass();
        }
        if (e.getMessage() != null) {
            message += '=' + e.getMessage();
        }
        return message;
    }

    protected abstract String asString(Object value);

}
