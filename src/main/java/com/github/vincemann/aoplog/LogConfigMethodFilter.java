package com.github.vincemann.aoplog;

import com.github.vincemann.aoplog.api.LogInteraction;
import com.github.vincemann.aoplog.api.LogConfig;
import com.github.vincemann.aoplog.parseAnnotation.SourceAwareAnnotationInfo;

public class LogConfigMethodFilter implements MethodFilter{

    @Override
    public boolean wanted(MethodDescriptor methodDescriptor) {
        SourceAwareAnnotationInfo<LogInteraction> logInfo = methodDescriptor.getInvocationDescriptor().getLogInfo();
        LogConfig classLogConfig = methodDescriptor.getInvocationDescriptor().getClassLogConfig();
        String methodName = methodDescriptor.getMethod().getName();
        if (logInfo==null){
            return false;
        }
        if (classLogConfig!=null){
            if (classLogConfig.ignoreGetters() && (methodName.startsWith("get") || methodName.startsWith("is"))){
                return false;
            }
            if (classLogConfig.ignoreSetters() && (methodName.startsWith("set"))){
                return false;
            }
        }
        return true;
    }
}
