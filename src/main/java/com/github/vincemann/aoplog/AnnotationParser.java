package com.github.vincemann.aoplog;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;


public interface AnnotationParser {
    public <A extends Annotation> AnnotationInfo<A> fromMethodOrClass(Method method, Class<A> type);
    public <A extends Annotation> A fromMethod(Method method, Class<A> type);
    public <A extends Annotation> A fromClass(Class<?> clazz, Class<A> type);
}
