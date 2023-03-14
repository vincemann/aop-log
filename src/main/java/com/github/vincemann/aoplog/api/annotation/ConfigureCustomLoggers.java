package com.github.vincemann.aoplog.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface ConfigureCustomLoggers {

    /**
     * define {@link CustomLogger}Impl beans autowired by beanname for arguments or return value.
     * i.E.:
     * @LogInteraction(smartLoggers= {
     *        @SmartLogger(key=“arg1“,logger=“ownerLoggerShort“),
     *        @SmartLogger(key=“ret“,logger=“ownerLoggerFull“),
     * , severity=INFO)
     * public Owner create(Owner o, String param2){
     * 	//…
     * 	return o;
     * }
     *
     * This would look for a SmartLogger with beanname "ownerLoggerShort" and use it for logging the first arguement instead
     * of just calling the toString() method of arg1.
     * @return
     */
    CustomLogger[] loggers() default {};
}
