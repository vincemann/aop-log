package com.github.vincemann.aoplog.parseAnnotation;

import lombok.Getter;
import lombok.ToString;
import org.springframework.util.Assert;

import java.lang.annotation.Annotation;

/**
 * Wrapper for Annotations including information about
 * where and in which context annotations were placed.
 */
@Getter
@ToString
public class AnnotationInfo<A extends Annotation> {
    private A annotation;
    private Class<?> declaringClass;

    public AnnotationInfo(A annotation, Class<?> declaringClass) {
        Assert.notNull(annotation);
        Assert.notNull(declaringClass);
        this.annotation = annotation;
        this.declaringClass = declaringClass;
    }
}
