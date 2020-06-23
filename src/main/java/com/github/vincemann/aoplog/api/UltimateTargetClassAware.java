package com.github.vincemann.aoplog.api;

/**
 * Used to get the ultimate target class of this, when this is proxied by JDK Runtime proxy.
 */
public interface UltimateTargetClassAware {
    /**
     * Just return the class that needs to implement this method.
     * But dont return this.getClass(), rather return MyClass.class.
     */
    Class<?> getTargetClass();
}
