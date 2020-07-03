package com.github.vincemann.aoplog.parseAnnotation;

import lombok.Builder;
import lombok.Getter;

import java.lang.annotation.Annotation;

@Getter
public class SourceAwareAnnotationInfo<A extends Annotation> extends AnnotationInfo<A>{
    private boolean classLevel;

    @Builder
    public SourceAwareAnnotationInfo(A annotation, Class<?> declaringClass, boolean classLevel) {
        super(annotation, declaringClass);
        this.classLevel = classLevel;
    }

    public SourceAwareAnnotationInfo(AnnotationInfo<A> annotationInfo, boolean classLevel){
        this(annotationInfo.getAnnotation(),annotationInfo.getDeclaringClass(),classLevel);
    }
}
