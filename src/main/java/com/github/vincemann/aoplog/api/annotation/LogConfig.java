package com.github.vincemann.aoplog.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @see com.github.vincemann.aoplog.GlobalRegExMethodFilter
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface LogConfig {
    boolean ignoreGetters() default true;
    boolean ignoreSetters() default true;
    String[] ignoredRegEx() default {"toString","hashCode","equals"};
    boolean logAllChildrenMethods() default false;
}
