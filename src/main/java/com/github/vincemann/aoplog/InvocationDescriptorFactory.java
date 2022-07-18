package com.github.vincemann.aoplog;

import com.github.vincemann.aoplog.api.annotation.ConfigureCustomLoggers;
import com.github.vincemann.aoplog.api.annotation.LogInteraction;
import com.github.vincemann.aoplog.parseAnnotation.AnnotationInfo;
import org.springframework.lang.Nullable;

interface InvocationDescriptorFactory {
    public InvocationDescriptor create(@Nullable AnnotationInfo<LogInteraction> methodLog, @Nullable AnnotationInfo<LogInteraction> classLog, @Nullable ConfigureCustomLoggers configureCustomLoggers);
}
