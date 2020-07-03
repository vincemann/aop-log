/***********************************************************************************
 * Copyright (c) 2013. Nickolay Gerilovich. Russia.
 *   Some Rights Reserved.
 ************************************************************************************/

package com.github.vincemann.aoplog;

import com.github.vincemann.aoplog.api.LogException;
import com.github.vincemann.aoplog.parseAnnotation.SourceAwareAnnotationInfo;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Method exceptions descriptor.
 */
final class ExceptionDescriptor {

    private final Map<Class<? extends Exception>, ExceptionSeverity> exceptionSeverity;
    private final SourceAwareAnnotationInfo<LogException> exceptionAnnotationInfo;

    private ExceptionDescriptor(Map<Class<? extends Exception>, ExceptionSeverity> exceptionSeverity, SourceAwareAnnotationInfo<LogException> exceptionAnnotationInfo) {
        this.exceptionSeverity = exceptionSeverity;
        this.exceptionAnnotationInfo = exceptionAnnotationInfo;
    }

    public Collection<Class<? extends Exception>> getDefinedExceptions() {
        return exceptionSeverity.keySet();
    }

    public ExceptionSeverity getExceptionSeverity(Class<? extends Exception> resolvedException) {
        return exceptionSeverity.get(resolvedException);
    }

    public SourceAwareAnnotationInfo<LogException> getExceptionAnnotationInfo() {
        return exceptionAnnotationInfo;
    }

    /**
     * Builder.
     */
    public static final class Builder {
        private final SourceAwareAnnotationInfo<LogException> exceptionAnnotationInfo;
        private final Map<Class<? extends Exception>, ExceptionSeverity> map = new HashMap<Class<? extends Exception>, ExceptionSeverity>();

        public Builder(SourceAwareAnnotationInfo<LogException> exceptionAnnotationInfo) {
            this.exceptionAnnotationInfo = exceptionAnnotationInfo;
        }

        public ExceptionDescriptor build() {
            setSeverity(exceptionAnnotationInfo.getAnnotation().fatal(), Severity.FATAL);
            setSeverity(exceptionAnnotationInfo.getAnnotation().value(), Severity.ERROR);
            setSeverity(exceptionAnnotationInfo.getAnnotation().warn(), Severity.WARN);
            setSeverity(exceptionAnnotationInfo.getAnnotation().info(), Severity.INFO);
            setSeverity(exceptionAnnotationInfo.getAnnotation().debug(), Severity.DEBUG);
            setSeverity(exceptionAnnotationInfo.getAnnotation().trace(), Severity.TRACE);
            return new ExceptionDescriptor(map, exceptionAnnotationInfo);
        }

        private void setSeverity(LogException.Exc[] exceptionGroups, Severity targetSeverity) {
            for (LogException.Exc exceptions : exceptionGroups) {
                for (Class<? extends Exception> clazz : exceptions.value()) {
                    ExceptionSeverity descriptor = map.get(clazz);
                    if (descriptor == null || Utils.greater(targetSeverity, descriptor.getSeverity())) {
                        map.put(clazz, ExceptionSeverity.create(targetSeverity, exceptions.stacktrace()));
                    }
                }
            }

        }

    }


}
