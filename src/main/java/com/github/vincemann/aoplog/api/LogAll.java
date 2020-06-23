package com.github.vincemann.aoplog.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface LogAll {
    boolean ignoreGetters() default false;
    boolean ignoreSetters() default false;
    Log config() default @Log;
}
