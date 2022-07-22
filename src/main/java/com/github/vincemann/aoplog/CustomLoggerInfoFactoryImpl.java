package com.github.vincemann.aoplog;

import com.github.vincemann.aoplog.api.annotation.ConfigureCustomLoggers;
import com.github.vincemann.aoplog.api.annotation.CustomLogger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.HashSet;
import java.util.Set;

public class CustomLoggerInfoFactoryImpl implements CustomLoggerInfoFactory, ApplicationContextAware {


    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public Set<CustomLoggerInfo> createCustomLoggerInfo(ConfigureCustomLoggers configureCustomLoggersAnnotation) {
        Set<CustomLoggerInfo> result = new HashSet<>();
        for (CustomLogger logger : configureCustomLoggersAnnotation.loggers()) {
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
                throw new IllegalArgumentException("invalid key. No Type found. Available type: arg | ret")
            }
            customLoggerInfo.setType(type);
            com.github.vincemann.aoplog.api.CustomLogger loggerBean = (com.github.vincemann.aoplog.api.CustomLogger) applicationContext.getAutowireCapableBeanFactory().getBean(logger.beanname());
            customLoggerInfo.setLogger(loggerBean);
        }
        return result;
    }


}
