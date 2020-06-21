/***********************************************************************************
 * Copyright (c) 2013. Nickolay Gerilovich. Russia.
 *   Some Rights Reserved.
 ************************************************************************************/

package com.github.vincemann.aoplog;

import com.github.vincemann.aoplog.annotation.Log;
import com.github.vincemann.aoplog.annotation.LogException;
import com.github.vincemann.aoplog.annotation.*;
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
        private AnnotationInfo<Log> loggingInfo;
        private AnnotationInfo<LogException> logExceptionInfo;
        private Severity severity;
        private Severity classSeverity;

        public Builder(@Nullable AnnotationInfo<Log> loggingInfo, @Nullable AnnotationInfo<LogException> logExceptionInfo) {
            this.loggingInfo = loggingInfo;
            this.logExceptionInfo = logExceptionInfo;
        }

        public InvocationDescriptor build() {
            LogException logException = logExceptionInfo== null ? null : logExceptionInfo.getAnnotation();
            if (loggingInfo!=null){
                Log annotation = loggingInfo.getAnnotation();
                setSeverity(annotation.level(),!loggingInfo.isClassLevel());
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
