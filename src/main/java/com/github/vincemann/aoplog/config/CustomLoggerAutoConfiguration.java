package com.github.vincemann.aoplog.config;

import com.github.vincemann.aoplog.CustomLoggerInfoFactory;
import com.github.vincemann.aoplog.CustomLoggerInfoFactoryImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomLoggerAutoConfiguration {


    @ConditionalOnMissingBean(CustomLoggerInfoFactory.class)
    @Bean
    public CustomLoggerInfoFactory customLoggerInfoFactory(){
        return new CustomLoggerInfoFactoryImpl();
    }
}
