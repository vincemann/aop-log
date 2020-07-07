/***********************************************************************************
 * Copyright (c) 2013. Nickolay Gerilovich. Russia.
 *   Some Rights Reserved.
 ************************************************************************************/

package com.github.vincemann.aoplog;

import org.apache.commons.logging.Log;

import java.lang.reflect.Method;

/**
 * Declares access to the logger and log message creation.
 */
interface LogAdapter {
    Log getLog(Class clazz);

    Log getLog(String name);

    Object toMessage(Method method, Object[] args, ArgumentDescriptor argumentDescriptor);

    Object toMessage(Method method, int argCount, Object result);

    Object toMessage(Method method, int argCount, Exception e, boolean stackTrace);

    void onUnLoggedException(Method method, Exception e);
}
