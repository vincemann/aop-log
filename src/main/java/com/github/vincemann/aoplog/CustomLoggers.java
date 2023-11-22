package com.github.vincemann.aoplog;

import com.github.vincemann.aoplog.api.annotation.CustomLogger;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target({ElementType.METHOD})
public @interface CustomLoggers {

    CustomLogger[] value();
}
