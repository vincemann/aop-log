package com.github.vincemann.aoplog.annotation;

import org.springframework.util.Assert;

import java.lang.annotation.Annotation;


/**
 * Wrapper for Annotations including information about
 * where and in which context annotations were placed.
 */
// dont use otherwise NULL will be equal all the time, when really the reference should be compared
//@EqualsAndHashCode
public class AnnotationInfo<A extends Annotation> {

    private A annotation;
    private Class<?> declaringClass;


    private AnnotationInfo(){}

    public AnnotationInfo(A annotation, Class<?> declaringClass) {
        Assert.notNull(annotation);
        Assert.notNull(declaringClass);
        this.annotation = annotation;
        this.declaringClass = declaringClass;
    }





    // used for caching reasons only, creates a new reference each time
    // equals will return false for diff NULL instances, which is important
    public static AnnotationInfo NULL(){
        return new AnnotationInfo<>();
    }

    public static boolean IS_NULL(AnnotationInfo instance){
        return instance.getAnnotation() == null && instance.getDeclaringClass() == null;
    }

    @Override
    public String toString() {
        return "AnnotationInfo{" +
                "annotation=" + annotation +
                ", declaringClass=" + declaringClass +
                '}';
    }

    public A getAnnotation() {
        return annotation;
    }

    public Class<?> getDeclaringClass() {
        return declaringClass;
    }
}

