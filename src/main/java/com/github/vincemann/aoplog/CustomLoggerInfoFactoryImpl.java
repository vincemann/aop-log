package com.github.vincemann.aoplog;

import com.github.vincemann.aoplog.api.annotation.CustomLogger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class CustomLoggerInfoFactoryImpl implements CustomLoggerInfoFactory, ApplicationContextAware {


    private static final Map<CustomLogger,CustomLoggerInfo> CACHE = new HashMap<>();

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public CustomLoggerInfo createCustomLoggerInfo(CustomLogger annotation) {
        CustomLoggerInfo cached = CACHE.get(annotation);
        if (cached != null)
            return cached;

        String beanname = annotation.beanname();
        if (beanname ==null || beanname.isBlank()){
            throw new IllegalArgumentException("no beanname specified");
        }
        com.github.vincemann.aoplog.api.CustomLogger loggerBean = (com.github.vincemann.aoplog.api.CustomLogger) this.applicationContext.getAutowireCapableBeanFactory().getBean(beanname);
        CustomLoggerInfo result = new CustomLoggerInfo(loggerBean,LoggableMethodPart.from(annotation.key()));
        CACHE.put(annotation,result);
        return result;
    }


}
