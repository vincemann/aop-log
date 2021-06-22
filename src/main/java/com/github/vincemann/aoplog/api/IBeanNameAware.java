package com.github.vincemann.aoplog.api;

import org.springframework.beans.factory.BeanNameAware;

public interface IBeanNameAware extends BeanNameAware {
    public String getBeanName();
}
