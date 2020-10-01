package com.github.vincemann.aoplog;

import com.github.vincemann.aoplog.api.LogInteraction;
import com.github.vincemann.aoplog.api.LogConfig;
import com.github.vincemann.aoplog.parseAnnotation.SourceAwareAnnotationInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.lang.reflect.Method;

public class LogConfigMethodFilter implements MethodFilter {

    @Override
    public boolean wanted(MethodDescriptor methodDescriptor) {
        SourceAwareAnnotationInfo<LogInteraction> logInfo = methodDescriptor.getInvocationDescriptor().getLogInfo();
        LogConfig classLogConfig = methodDescriptor.getInvocationDescriptor().getClassLogConfig();
        String methodName = methodDescriptor.getMethod().getName();
        if (logInfo == null) {
            return false;
        }
        if (logInfo.isClassLevel()) {
            boolean methodDefinedInAnnotationClass = isMethodDefinedInAnnotationClass(methodDescriptor.getMethod(), logInfo);
            if (classLogConfig != null) {
                if (!methodDefinedInAnnotationClass && !classLogConfig.logAllChildrenMethods()) {
                    return false;
                }
                if (classLogConfig.ignoreGetters() && (methodName.startsWith("get") || methodName.startsWith("is"))) {
                    return false;
                }
                if (classLogConfig.ignoreSetters() && (methodName.startsWith("set"))) {
                    return false;
                }
                if (Sets.newHashSet(classLogConfig.ignoredMethods()).parallelStream().filter(e -> e.equals(methodName)).count()!=0){
                    //method is ignored by name
                    return false;
                }
            } else {
                if (!methodDefinedInAnnotationClass) {
                    return false;
                }
            }
        }
        return true;
    }

    // checks whether the annotated class or super classes of it, declared the called method
    private boolean isMethodDefinedInAnnotationClass(Method targetMethod, SourceAwareAnnotationInfo<LogInteraction> classLogInfo) {
        try {
            MethodUtils.findMethod(classLogInfo.getDeclaringClass(),targetMethod.getName(),targetMethod.getParameterTypes());
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }
}
