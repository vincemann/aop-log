package com.github.vincemann.aoplog;

import com.github.vincemann.aoplog.api.annotation.ConfigureCustomLoggers;

import java.util.Set;

public interface CustomLoggerInfoFactory {
    Set<CustomLoggerInfo> createCustomLoggerInfo(ConfigureCustomLoggers configureCustomLoggersAnnotation);
}
