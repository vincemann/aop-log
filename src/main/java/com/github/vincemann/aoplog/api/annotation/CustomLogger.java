package com.github.vincemann.aoplog.api.annotation;

import com.github.vincemann.aoplog.CustomLoggers;

import java.lang.annotation.*;

/**
 * Put this annotation on any method, that is logged by aop log.
 * Define {@link com.github.vincemann.aoplog.api.CustomLogger}Impl beans, that will be autowired by beanname
 * for logging arguments or the return value.
 * i.E.:
 *
 *     @CustomLogger(key = "arg2", beanname = "mylogger1"),
 *     @CustomLogger(key = "ret", beanname = "mylogger2")
 *     @LogInteraction(value = Severity.INFO)
 *     public LogChild testAop(String s1, LogChild logChild) {
 *         ...
 *         return logChild;
 *     }
 *
 * This would work for a {@link com.github.vincemann.aoplog.api.CustomLogger} with beanname "mylogger1"
 * and use it for logging the first argument instead of just calling the toString() method of arg1.
 * mylogger1.toString(arg1) will be called.
 * The return value will be logged by calling mylogger2.toString(retValue).
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target({ElementType.METHOD})
@Repeatable(CustomLoggers.class)
public @interface CustomLogger {

//    public static final String ARG1 = "arg1";
//    public static final String ARG2 = "arg2";
//    public static final String ARG3 = "arg3";
//    public static final String RET = "ret";

    String key() default "";
    String beanname() default "";
}
