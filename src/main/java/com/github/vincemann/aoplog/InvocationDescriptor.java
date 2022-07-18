/***********************************************************************************
 * Copyright (c) 2013. Nickolay Gerilovich. Russia.
 *   Some Rights Reserved.
 ************************************************************************************/

package com.github.vincemann.aoplog;

import com.github.vincemann.aoplog.api.annotation.ConfigureCustomLoggers;
import com.github.vincemann.aoplog.api.annotation.LogConfig;
import com.github.vincemann.aoplog.api.annotation.LogInteraction;
import com.github.vincemann.aoplog.parseAnnotation.SourceAwareAnnotationInfo;
import lombok.Getter;
import lombok.ToString;
import org.springframework.lang.Nullable;

/**
 * Method descriptor. With effective Severity and LogInfos.
 */
@Getter
@ToString
final class InvocationDescriptor {
    private final Severity severity;
    @Nullable
    private final SourceAwareAnnotationInfo<LogInteraction> logInfo;
    @Nullable
    private final LogConfig classLogConfig;

    private final ConfigureCustomLoggers configureCustomLoggersAnnotation;


    InvocationDescriptor(Severity severity, SourceAwareAnnotationInfo<LogInteraction> logInfo, LogConfig classLogConfig, ConfigureCustomLoggers configureCustomLoggersAnnotation) {
        this.severity = severity;
        this.logInfo = logInfo;
        this.classLogConfig = classLogConfig;
        this.configureCustomLoggersAnnotation = configureCustomLoggersAnnotation;
    }
}
