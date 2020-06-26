/***********************************************************************************
 * Copyright (c) 2013. Nickolay Gerilovich. Russia.
 *   Some Rights Reserved.
 ************************************************************************************/

package com.github.vincemann.aoplog;

import com.github.vincemann.aoplog.api.LogInteraction;
import com.github.vincemann.aoplog.api.LogAllInteractions;
import com.github.vincemann.aoplog.api.LogConfig;
import com.github.vincemann.aoplog.api.LogException;
import lombok.Getter;
import org.springframework.lang.Nullable;

/**
 * Method descriptor.
 */
@Getter
final class InvocationDescriptor {
    private final Severity severity;
    @Nullable
    private final AnnotationInfo<LogException> exceptionAnnotation;
    @Nullable
    private final AnnotationInfo<LogInteraction> logInfo;
    @Nullable
    private final LogConfig classLogConfig;

    /**
     *
     * @param severity      Effective log severity.
     *                      Can be null if log is {@link LogInteraction#disabled()} or no Log annotation was found.
     * @param exceptionAnnotation
     * @param logInfo       Effective Log annotation, either from class level (extracted from {@link LogAllInteractions} or method level.
     *                      Can be null if log is {@link LogInteraction#disabled()} or no Log annotation was found.
     * @param classLogConfig    Effective {@link LogConfig} from {@link LogAllInteractions} extracted.
     *                          Can be null if log is {@link LogInteraction#disabled()} or method level @{@link LogInteraction} was found (which takes precedence over class level annotation).
     */
    private InvocationDescriptor(Severity severity, @Nullable AnnotationInfo<LogException> exceptionAnnotation, AnnotationInfo<LogInteraction> logInfo, LogConfig classLogConfig) {
        this.severity = severity;
        this.exceptionAnnotation = exceptionAnnotation;
        this.logInfo = logInfo;
        this.classLogConfig = classLogConfig;
    }



    /**
     * Builder.
     * Implements extraction of AnnotationInfo<Log> logInfo from method and classLog.
     */
    public static final class Builder {
        @Nullable
        private final LogInteraction methodLog;
        private final LogAllInteractions classLog;
        private AnnotationInfo<LogException> logExceptionInfo;

        public Builder(@Nullable LogInteraction methodLog, LogAllInteractions classLog , @Nullable AnnotationInfo<LogException> logExceptionInfo) {
            this.methodLog = methodLog;
            this.classLog = classLog;
            this.logExceptionInfo = logExceptionInfo;
        }

        //finds non disabled @Log if any
        //first search for method, if method not found : fall back on class level
        private AnnotationInfo<LogInteraction> evalLogInfo(){
            boolean methodFound = !(methodLog==null);
            boolean classFound = !(classLog==null);
            Boolean methodDisabled = !methodFound? null :  methodLog.disabled();
            Boolean classDisabled = !classFound? null :  classLog.value().disabled();
            if (methodFound){
                if (methodDisabled){
                    //override of class config needs to be possible
//                    if (classFound && !classDisabled){
//                        return new AnnotationInfo<>(classLog.config(),true);
//                    }
                    return null;
                }else {
                    return new AnnotationInfo<>(methodLog,false);
                }
            }else {
                if (classFound && !classDisabled){
                    return new AnnotationInfo<>(classLog.value(),true);
                }
            }
            return null;
        }

        public InvocationDescriptor build() {
            AnnotationInfo<LogInteraction> logInfo = evalLogInfo();

            LogConfig logConfig = null;
            Severity severity = null;

            if (logInfo !=null){
                logConfig= logInfo.isClassLevel() ? classLog.config() : null;
                severity = logInfo.getAnnotation().level();
            }
            return new InvocationDescriptor(
                    severity,
                    logExceptionInfo,
                    logInfo,
                    logConfig);
        }


    }
}
