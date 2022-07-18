package com.github.vincemann.aoplog;

import com.github.vincemann.aoplog.api.annotation.LogInteraction;
import com.github.vincemann.aoplog.api.annotation.LogConfig;
import com.github.vincemann.aoplog.parseAnnotation.SourceAwareAnnotationInfo;
import com.google.common.collect.Sets;

import java.lang.reflect.Method;

import static com.github.vincemann.aoplog.Patterns.GETTER_REGEX;
import static com.github.vincemann.aoplog.Patterns.SETTER_REGEX;


public class LogConfigMethodFilter implements MethodFilter {

    @Override
    public boolean wanted(MethodDescriptor methodDescriptor) {
        SourceAwareAnnotationInfo<LogInteraction> logInfo = methodDescriptor.getInvocationDescriptor().getLogInfo();
        LogConfig classLogConfig = methodDescriptor.getInvocationDescriptor().getClassLogConfig();
        String methodName = methodDescriptor.getMethod().getName();
        if (logInfo == null) {
            return false;
        }
        // Next rules only apply if logInfo ist not explicitly present on method!
        if (logInfo.isClassLevel()) {
            boolean methodDefinedInAnnotationClass = isMethodDefinedInAnnotationClass(methodDescriptor.getMethod(), logInfo);
            if (classLogConfig != null) {
                if (!methodDefinedInAnnotationClass && !classLogConfig.logAllChildrenMethods()) {
                    return false;
                }
                if (classLogConfig.ignoreGetters() && (methodName.matches(GETTER_REGEX))) {
                    return false;
                }
                if (classLogConfig.ignoreSetters() && (methodName.matches(SETTER_REGEX))) {
                    return false;
                }
                if (Sets.newHashSet(classLogConfig.ignoredRegEx()).parallelStream().anyMatch(methodName::matches)){
                    //method is matched regEx -> ignored
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
