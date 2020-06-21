package com.github.vincemann.aoplog;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.lang.annotation.Annotation;

@Getter
class ClassAnnotationInfo<A extends Annotation> {
    private A annotation;
    private Class<?> targetClass;

    @Builder
    public ClassAnnotationInfo(A annotation, Class<?> targetClass) {
        this.annotation = annotation;
        this.targetClass = targetClass;
    }
}
