package com.github.vincemann.aoplog.api.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target({ElementType.METHOD})
public @interface CustomToStrings {

    CustomToString[] value();
}
