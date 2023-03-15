package com.github.vincemann.aoplog.api;

import org.springframework.beans.factory.BeanNameAware;

/**
 * Optional:
 * Implement this interface in order to also log the beanname by {@link com.github.vincemann.aoplog.ProxyAwareAopLogger}.
 */
public interface IBeanNameAware extends BeanNameAware {
    public String getBeanName();
}
