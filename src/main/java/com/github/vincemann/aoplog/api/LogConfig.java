package com.github.vincemann.aoplog.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface LogConfig {
    boolean ignoreGetters() default true;
    boolean ignoreSetters() default true;
    String[] ignoredMethods() default {"toString,hashCode,equals"};
    boolean logAllChildrenMethods() default false;
}
