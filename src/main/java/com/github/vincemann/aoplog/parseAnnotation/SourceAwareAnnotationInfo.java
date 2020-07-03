package com.github.vincemann.aoplog.parseAnnotation;

import lombok.Builder;
import lombok.Getter;
import org.springframework.util.Assert;

import java.lang.annotation.Annotation;

@Getter
public class SourceAwareAnnotationInfo<A extends Annotation> extends AnnotationInfo<A>{
    private boolean classLevel;

    @Builder
    public SourceAwareAnnotationInfo(A annotation, Class<?> declaringClass, Boolean classLevel) {
        super(annotation, declaringClass);
        Assert.notNull(classLevel);
        this.classLevel = classLevel;
    }

    public SourceAwareAnnotationInfo(AnnotationInfo<A> annotationInfo, Boolean classLevel){
        this(annotationInfo.getAnnotation(),annotationInfo.getDeclaringClass(),classLevel);
    }
}
