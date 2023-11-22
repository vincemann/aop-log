/***********************************************************************************
 * Copyright (c) 2013. Nickolay Gerilovich. Russia.
 *   Some Rights Reserved.
 ************************************************************************************/

package com.github.vincemann.aoplog;

import org.apache.commons.logging.Log;

import java.lang.reflect.Method;
import java.util.Set;

/**
 * Declares access to the logger and log message creation.
 */
interface LogAdapter {
    Log getLog(Class clazz);

    Log getLog(String name);

    Object toMessage(Method method, String beanName, Object[] args, ArgumentDescriptor argumentDescriptor, Set<CustomLoggerInfo> customLoggerInfo, Set<CustomToStringInfo> customToStringInfos);

    Object toMessage(Method method, String beanName, int argCount, Object result, Set<CustomLoggerInfo> customLoggerInfo, Set<CustomToStringInfo> customToStringInfos);

    Object toMessage(Method method,String beanName, int argCount, Exception e, boolean stackTrace);
}
