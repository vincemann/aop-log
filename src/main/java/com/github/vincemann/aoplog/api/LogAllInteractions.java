package com.github.vincemann.aoplog.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface LogAllInteractions {
    LogConfig config() default @LogConfig;
    LogInteraction value() default @LogInteraction;
}
