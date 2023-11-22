package com.github.vincemann.aoplog;

import com.github.vincemann.aoplog.api.annotation.CustomLogger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
public class CustomLoggerInfoFactoryImpl implements CustomLoggerInfoFactory, ApplicationContextAware {


    private static final Map<CustomLogger,CustomLoggerInfo> CACHE = new HashMap<>();

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public CustomLoggerInfo createCustomLoggerInfo(CustomLogger logger) {
        CustomLoggerInfo cached = CACHE.get(logger);
        if (cached != null)
            return cached;
        String key = logger.key();
        CustomLoggerInfo.Type type;
        CustomLoggerInfo customLoggerInfo = new CustomLoggerInfo();
        if (key.startsWith("arg")) {
            type = CustomLoggerInfo.Type.ARG;
            try {
                int argNum = Integer.parseInt(key.substring(3));
                customLoggerInfo.setArgNum(argNum);
            } catch (NumberFormatException | IndexOutOfBoundsException e) {
                throw new IllegalArgumentException("invalid arg key. Example: arg2");
            }
        } else if (key.equals("ret")) {
            type = CustomLoggerInfo.Type.RET;
        } else {
            throw new IllegalArgumentException("invalid key. No Type found. Available type: arg | ret");
        }
        customLoggerInfo.setType(type);
        String beanname = logger.beanname();
        if (beanname ==null || beanname.isBlank()){
            throw new IllegalArgumentException("no beanname specified");
        }
        com.github.vincemann.aoplog.api.CustomLogger loggerBean = (com.github.vincemann.aoplog.api.CustomLogger) this.applicationContext.getAutowireCapableBeanFactory().getBean(beanname);
        customLoggerInfo.setLogger(loggerBean);
        CACHE.put(logger,customLoggerInfo);
        return customLoggerInfo;
    }


}
