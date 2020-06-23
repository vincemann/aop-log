/***********************************************************************************
 * Copyright (c) 2013. Nickolay Gerilovich. Russia.
 *   Some Rights Reserved.
 ************************************************************************************/

package com.github.vincemann.aoplog;

import com.github.vincemann.aoplog.api.Log;
import com.github.vincemann.aoplog.api.LogException;
import org.springframework.lang.Nullable;

/**
 * Method descriptor.
 */
final class InvocationDescriptor {
    private final Severity severity;
    @Nullable
    private final LogException exceptionAnnotation;

    private InvocationDescriptor(Severity severity, @Nullable LogException exceptionAnnotation) {
        this.severity = severity;
        this.exceptionAnnotation = exceptionAnnotation;
    }


    public Severity getSeverity() {
        return severity;
    }

    public LogException getExceptionAnnotation() {
        return exceptionAnnotation;
    }

    /**
     * Builder.
     */
    public static final class Builder {
        private AnnotationInfo<Log> logInfo;
        private AnnotationInfo<LogException> logExceptionInfo;
        private Severity severity;
        private Severity classSeverity;

        public Builder(@Nullable AnnotationInfo<Log> logInfo, @Nullable AnnotationInfo<LogException> logExceptionInfo) {
            this.logInfo = logInfo;
            this.logExceptionInfo = logExceptionInfo;
        }

        public InvocationDescriptor build() {
            LogException logException = logExceptionInfo== null ? null : logExceptionInfo.getAnnotation();
            if (logInfo !=null){
                setSeverity(logInfo.getAnnotation().level(),!logInfo.isClassLevel());
            }

            if (severity!=null) {
                return new InvocationDescriptor(
                        severity,
                        logException
                );
            }

            return new InvocationDescriptor(
                    classSeverity,
                    logException
            );

        }


        private void setSeverity(Severity targetSeverity, boolean fromMethod) {
            if (fromMethod) {
                severity = targetSeverity;
            } else {
                classSeverity=targetSeverity;
            }
        }

    }
}