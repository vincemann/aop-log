package com.github.vincemann.aoplog;

import com.github.vincemann.aoplog.api.annotation.ConfigureCustomLoggers;
import com.github.vincemann.aoplog.api.annotation.LogConfig;
import com.github.vincemann.aoplog.api.annotation.LogInteraction;
import com.github.vincemann.aoplog.parseAnnotation.AnnotationInfo;
import com.github.vincemann.aoplog.parseAnnotation.SourceAwareAnnotationInfo;

import java.util.Set;


/**
 *  Create InvocationDescriptor with respect to {@link LogInteraction#disabled()} and precedence of method level annotations over class level annotations.
 *
 *
 * {@link InvocationDescriptor#getSeverity()}      Effective log severity.
 *                      Can be null if log is {@link LogInteraction#disabled()} or no Log annotation was found.
 * {@link InvocationDescriptor#getLogInfo()} ()}        Effective Log annotation, either from class level or method level.
 *                      Can be null if log is {@link LogInteraction#disabled()} or no Log annotation was found.
 * {@link InvocationDescriptor#getClassLogConfig()} ()}     Effective {@link LogConfig} from class declaring LogInteraction annotation, if effective LogInteraction is extracted from class.
 *                          Can be null if log is {@link LogInteraction#disabled()} or method level @{@link LogInteraction} was found (which takes precedence over class level annotation).
 */
public class InvocationDescriptorFactoryImpl implements InvocationDescriptorFactory {




    @Override
    public InvocationDescriptor create(AnnotationInfo<LogInteraction> methodLog, AnnotationInfo<LogInteraction> classLog, Set<CustomLoggerInfo> configureCustomLoggersInfo) {
        LogConfig logConfig = null;
        Severity severity = null;
        SourceAwareAnnotationInfo<LogInteraction> logInfo = evalEffectiveLogInfo(methodLog, classLog);

        if (logInfo !=null){
            logConfig= logInfo.isClassLevel() ?
                    logInfo.getDeclaringClass().getDeclaredAnnotation(LogConfig.class)
                    : null;
            severity = logInfo.getAnnotation().value();
        }
        return new InvocationDescriptor(
                severity,
                logInfo,
                logConfig, configureCustomLoggersInfo);
    }

    private Set<CustomLoggerInfo> createCustomLoggerInfo(ConfigureCustomLoggers configureCustomLoggersAnnotation){

    }

    //finds non disabled @Log if any
    //first search for method, if method not found : fall back on class level
    private SourceAwareAnnotationInfo<LogInteraction> evalEffectiveLogInfo(AnnotationInfo<LogInteraction> methodLog, AnnotationInfo<LogInteraction> classLog){
        boolean methodFound = !(methodLog==null);
        boolean classFound = !(classLog==null);
        Boolean methodDisabled = !methodFound? null :  methodLog.getAnnotation().disabled();
        Boolean classDisabled = !classFound? null :  classLog.getAnnotation().disabled();
        if (methodFound){
            if (methodDisabled){
                //override of class config needs to be possible
//                    if (classFound && !classDisabled){
//                        return new AnnotationInfo<>(classLog.config(),true);
//                    }
                return null;
            }else {
                return new SourceAwareAnnotationInfo<>(methodLog,false);
            }
        }else {
            if (classFound && !classDisabled){
                return new SourceAwareAnnotationInfo<>(classLog,true);
            }
        }
        return null;
    }
}
