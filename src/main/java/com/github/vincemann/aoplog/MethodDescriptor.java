/***********************************************************************************
 * Copyright (c) 2013. Nickolay Gerilovich. Russia.
 *   Some Rights Reserved.
 ************************************************************************************/

package com.github.vincemann.aoplog;

import java.lang.reflect.Method;

/**
 * Method descriptor.
 */
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

    @Override
    public String toString() {
        return "MethodDescriptor{" +
                "invocationDescriptor=" + invocationDescriptor +
                ", argumentDescriptor=" + argumentDescriptor +
                ", exceptionDescriptor=" + exceptionDescriptor +
                ", method=" + method +
                '}';
    }

    public InvocationDescriptor getInvocationDescriptor() {
        return invocationDescriptor;
    }

    public ArgumentDescriptor getArgumentDescriptor() {
        return argumentDescriptor;
    }

    public ExceptionDescriptor getExceptionDescriptor() {
        return exceptionDescriptor;
    }

    public Method getMethod() {
        return method;
    }
}
