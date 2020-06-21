/***********************************************************************************
 * Copyright (c) 2013. Nickolay Gerilovich. Russia.
 *   Some Rights Reserved.
 ************************************************************************************/

package com.github.vincemann.aoplog.annotation;

import com.github.vincemann.aoplog.Severity;
import org.springframework.core.annotation.AliasFor;


import java.lang.annotation.*;

/**
 * Meta annotation that indicates a log method annotation.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD,ElementType.TYPE})
public @interface Log {

    /**
     * Only works when Annotation is on class.
     */
    LogConfig config() default @LogConfig;

    @AliasFor("level")
    Severity value() default Severity.DEBUG;

    @AliasFor("value")
    Severity level() default Severity.DEBUG;

    boolean disabled() default false;

}