package com.github.vincemann.aoplog.parseAnnotation;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.lang.annotation.Annotation;

/**
 * Wrapper for Annotations including information about
 * where and in which context annotations were placed.
 */
@AllArgsConstructor
@Getter
public class AnnotationInfo<A extends Annotation> {
    private A annotation;
    private Class<?> declaringClass;
}
