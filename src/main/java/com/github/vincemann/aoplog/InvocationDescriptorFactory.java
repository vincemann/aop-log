package com.github.vincemann.aoplog;

import com.github.vincemann.aoplog.api.annotation.LogInteraction;
import com.github.vincemann.aoplog.parseAnnotation.AnnotationInfo;
import org.springframework.lang.Nullable;

import java.util.Set;

interface InvocationDescriptorFactory {
    public InvocationDescriptor create(@Nullable AnnotationInfo<LogInteraction> methodLog, @Nullable AnnotationInfo<LogInteraction> classLog, @Nullable Set<CustomLoggerInfo> configureCustomLoggers);
}
