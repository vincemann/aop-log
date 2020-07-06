/***********************************************************************************
 * Copyright (c) 2013. Nickolay Gerilovich. Russia.
 *   Some Rights Reserved.
 ************************************************************************************/

package com.github.vincemann.aoplog;

import lombok.Getter;
import lombok.ToString;

import java.lang.reflect.Method;

/**
 * Method descriptor.
 */
@Getter
@ToString
final class MethodDescriptor {
    private final InvocationDescriptor invocationDescriptor;
    private final ArgumentDescriptor argumentDescriptor;
    private final ExceptionDescriptor exceptionDescriptor;
    private final Method method;

    public MethodDescriptor(InvocationDescriptor invocationDescriptor, ArgumentDescriptor argumentDescriptor, ExceptionDescriptor exceptionDescriptor, Method method) {
        this.invocationDescriptor = invocationDescriptor;
        this.argumentDescriptor = argumentDescriptor;
        this.exceptionDescriptor = exceptionDescriptor;
        this.method = method;
    }

}
