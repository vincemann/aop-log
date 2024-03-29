/***********************************************************************************
 * Copyright (c) 2013. Nickolay Gerilovich. Russia.
 *   Some Rights Reserved.
 ************************************************************************************/

package com.github.vincemann.aoplog;

import com.github.vincemann.aoplog.annotation.SourceAwareAnnotationInfo;
import com.github.vincemann.aoplog.api.annotation.LogConfig;
import com.github.vincemann.aoplog.api.annotation.LogInteraction;
import org.springframework.lang.Nullable;

import java.util.Set;

/**
 * Method descriptor. With effective Severity and LogInfos.
 */
final class InvocationDescriptor {
    private final Severity severity;
    @Nullable
    private final SourceAwareAnnotationInfo<LogInteraction> logInfo;
    @Nullable
    private final LogConfig classLogConfig;

    private final Set<CustomLoggerInfo> customLoggerInfos;
    private final Set<CustomToStringInfo> customToStringInfos;


    InvocationDescriptor(Severity severity, SourceAwareAnnotationInfo<LogInteraction> logInfo, LogConfig classLogConfig, Set<CustomLoggerInfo> customLoggerInfos, Set<CustomToStringInfo> customToStringInfos) {
        this.severity = severity;
        this.logInfo = logInfo;
        this.classLogConfig = classLogConfig;
        this.customLoggerInfos = customLoggerInfos;
        this.customToStringInfos = customToStringInfos;
    }

    @Override
    public String toString() {
        return "InvocationDescriptor{" +
                "severity=" + severity +
                ", logInfo=" + logInfo +
                ", classLogConfig=" + classLogConfig +
                ", customLoggerInfos=" + customLoggerInfos +
                ", customToStringInfos=" + customToStringInfos +
                '}';
    }

    public Severity getSeverity() {
        return severity;
    }

    @Nullable
    public SourceAwareAnnotationInfo<LogInteraction> getLogInfo() {
        return logInfo;
    }

    @Nullable
    public LogConfig getClassLogConfig() {
        return classLogConfig;
    }

    public Set<CustomLoggerInfo> getCustomLoggerInfos() {
        return customLoggerInfos;
    }

    public Set<CustomToStringInfo> getCustomToStringInfos() {
        return customToStringInfos;
    }
}
