package com.github.vincemann.aoplog.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface ConfigureCustomLoggers {

    /**
     * define {@link com.github.vincemann.aoplog.api.CustomLogger}Impl beans autowired by beanname for logging arguments or the return value
     * of a method or class annotated with @{@link LogInteraction}.
     * i.E.:
     *      @ConfigureCustomLoggers(loggers =
     *             {
     *                     @CustomLogger(key = "arg2", beanname = "mylogger1"),
     *                     @CustomLogger(key = "ret", beanname = "mylogger2")
     *             }
     *     )
     *     @LogInteraction(value = Severity.INFO)
     *     public LogChild testAop(String s1, LogChild logChild) {
     *         ...
     *         return logChild;
     *     }
     *
     * This would work for a {@link CustomLogger} with beanname "mylogger1" and use it for logging the first argument instead
     * of just calling the toString() method of arg1 = mylogger1.toString(arg1).
     * The return value would be logged by calling mylogger2.toString(retValue).
     */
    CustomLogger[] loggers() default {};
}
