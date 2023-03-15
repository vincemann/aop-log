package com.github.vincemann.aoplog.api;


/**
 * A custom logger bean can implement this interface and be configured to get used instead of normal
 * toString method.
 * @see com.github.vincemann.aoplog.api.annotation.ConfigureCustomLoggers
 */
public interface CustomLogger {
    public String toString(Object o);
}
