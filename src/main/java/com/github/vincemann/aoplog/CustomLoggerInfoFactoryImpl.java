package com.github.vincemann.aoplog;

import com.github.vincemann.aoplog.api.annotation.ConfigureCustomLoggers;

import java.util.Set;

public class CustomLoggerInfoFactoryImpl implements CustomLoggerInfoFactory {


    @Override
    public Set<CustomLoggerInfo> createCustomLoggerInfo(ConfigureCustomLoggers configureCustomLoggersAnnotation) {
        return null;
    }
}
