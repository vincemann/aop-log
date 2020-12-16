package com.github.vincemann.aoplog.api;

public interface BeanNameAware extends org.springframework.beans.factory.BeanNameAware {
    public String getBeanName();
}
