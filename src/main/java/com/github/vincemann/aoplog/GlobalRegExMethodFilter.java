package com.github.vincemann.aoplog;

import com.github.vincemann.aoplog.api.annotation.LogInteraction;
import com.github.vincemann.aoplog.annotation.SourceAwareAnnotationInfo;
import com.google.common.collect.Sets;

import java.util.HashSet;
import java.util.Set;


/**
 * Use to globally specify regEx for methods that should not get logged
 */
public class GlobalRegExMethodFilter implements MethodFilter {

    private Set<String> ignoreRegEx = new HashSet<>();

    public GlobalRegExMethodFilter(String... regEx) {
        ignoreRegEx.addAll(Set.of(regEx));
    }

    @Override
    public boolean wanted(MethodDescriptor methodDescriptor) {
        SourceAwareAnnotationInfo<LogInteraction> logInfo = methodDescriptor.getInvocationDescriptor().getLogInfo();
        String methodName = methodDescriptor.getMethod().getName();
        if (logInfo == null) {
            return false;
        }
        // Next rules only apply if logInfo ist not explicitly present on method!
        if (logInfo.isClassLevel()) {
            if (ignoreRegEx.parallelStream().anyMatch(methodName::matches)){
                //method is matched regEx -> ignored
                return false;
            }
        }
        return true;
    }
}
