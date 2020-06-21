package com.github.vincemann.aoplog;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.lang.annotation.Annotation;

@Getter
/**
 * Wrapper for Annotations including information about
 * where and in which context annotations were placed.
 */
class AnnotationInfo<A extends Annotation> {
    private A annotation;
    private boolean classLevel;

    @Builder(access = AccessLevel.PROTECTED)
    public AnnotationInfo(A annotation, boolean classLevel) {
        this.annotation = annotation;
        this.classLevel = classLevel;
    }
}
