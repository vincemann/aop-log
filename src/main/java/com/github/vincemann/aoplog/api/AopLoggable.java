package com.github.vincemann.aoplog.api;

import org.springframework.test.util.AopTestUtils;


/**
 * Implement this interface to activate proxy based logging of aoplog via its api annotations such as {@link com.github.vincemann.aoplog.api.annotation.LogInteraction}.
 * If your bean is a JDK Runtime Proxy, implement: {@link org.springframework.aop.TargetClassAware} to make it work.
 */
public interface AopLoggable {

}
