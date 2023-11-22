package com.github.vincemann.aoplog.parseAnnotation;

import org.springframework.lang.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Set;


public interface AnnotationParser {
    @Nullable
    public <A extends Annotation> SourceAwareAnnotationInfo<A> fromMethodOrClass(Method method, Class<A> type);

    @Nullable
    public <A extends Annotation> SourceAwareAnnotationInfo<A> fromMethodOrClass(Class<?> clazz, String methodName, Class<?>[] argTypes, Class<A> type);

    @Nullable
    public <A extends Annotation> AnnotationInfo<A> fromMethod(Class<?> clazz, String methodName, Class<?>[] argTypes, Class<A> type);

    @Nullable
    public <A extends Annotation> AnnotationInfo<A> fromMethod(Method method, Class<A> type);
    @Nullable
    public <A extends Annotation> AnnotationInfo<A> fromClass(Class<?> clazz, Class<A> type);

    /**
     * Only looks for annotations on declared method
     */
    <A extends Annotation> Set<AnnotationInfo<A>> repeatableFromDeclaredMethod(Class<?> targetClass, String name, Class<?>[] parameterTypes, Class<A> customLoggerClass);
}
