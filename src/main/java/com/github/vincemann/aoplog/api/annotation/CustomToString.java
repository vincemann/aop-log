package com.github.vincemann.aoplog.api.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target({ElementType.METHOD})
@Repeatable(CustomToStrings.class)
public @interface CustomToString {

    String key() default "";
    String toStringMethod() default "";
}
