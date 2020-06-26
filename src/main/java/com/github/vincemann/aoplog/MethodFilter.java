package com.github.vincemann.aoplog;

public interface MethodFilter {
    public boolean wanted(MethodDescriptor methodDescriptor);
}
