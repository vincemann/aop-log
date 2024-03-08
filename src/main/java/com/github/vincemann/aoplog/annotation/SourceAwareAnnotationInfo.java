package com.github.vincemann.aoplog.annotation;

import org.springframework.util.Assert;

import java.lang.annotation.Annotation;

public class SourceAwareAnnotationInfo<A extends Annotation>
        extends AnnotationInfo<A>{
    private boolean classLevel;


    public SourceAwareAnnotationInfo(A annotation, Class<?> declaringClass, Boolean classLevel) {
        super(annotation, declaringClass);
        Assert.notNull(classLevel);
        this.classLevel = classLevel;
    }

    public SourceAwareAnnotationInfo(AnnotationInfo<A> annotationInfo, Boolean classLevel){
        this(annotationInfo.getAnnotation(),annotationInfo.getDeclaringClass(),classLevel);
    }



    @Override
    public String toString() {
        return "SourceAwareAnnotationInfo{" +
                "classLevel=" + classLevel +
                ", annotation=" + getAnnotation() +
                ", declaringClass=" + getDeclaringClass() +
                '}';
    }

    public boolean isClassLevel() {
        return classLevel;
    }

    public static final class Builder<A extends Annotation> {
        private A annotation;
        private Class<?> declaringClass;
        private boolean classLevel;

        private Builder() {
        }

        public static <A extends Annotation> Builder<A> builder() {
            return new Builder<>();
        }

        public Builder annotation(A annotation) {
            this.annotation = annotation;
            return this;
        }

        public Builder declaringClass(Class<?> declaringClass) {
            this.declaringClass = declaringClass;
            return this;
        }

        public Builder classLevel(boolean classLevel) {
            this.classLevel = classLevel;
            return this;
        }

        public SourceAwareAnnotationInfo build() {
            return new SourceAwareAnnotationInfo(this.annotation, declaringClass, this.classLevel);
        }
    }
}
