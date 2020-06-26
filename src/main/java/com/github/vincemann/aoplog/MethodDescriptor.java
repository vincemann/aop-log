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
    private volatile ArgumentDescriptor argumentDescriptor;
    private volatile ExceptionDescriptor exceptionDescriptor;
    private Method method;

    public MethodDescriptor(InvocationDescriptor invocationDescriptor, Method method) {
        this.invocationDescriptor = invocationDescriptor;
        this.method = method;
    }

    public InvocationDescriptor getInvocationDescriptor() {
        return invocationDescriptor;
    }

    public ArgumentDescriptor getArgumentDescriptor() {
        return argumentDescriptor;
    }

    public void setArgumentDescriptor(ArgumentDescriptor argumentDescriptor) {
        this.argumentDescriptor = argumentDescriptor;
    }

    public ExceptionDescriptor getExceptionDescriptor() {
        return exceptionDescriptor;
    }

    public void setExceptionDescriptor(ExceptionDescriptor exceptionDescriptor) {
        this.exceptionDescriptor = exceptionDescriptor;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }
}
