/***********************************************************************************
 * Copyright (c) 2013. Nickolay Gerilovich. Russia.
 *   Some Rights Reserved.
 ************************************************************************************/

package com.github.vincemann.aoplog;

import org.apache.commons.logging.Log;

import java.lang.reflect.Method;
import java.util.Set;

/**
 * Defines log strategies.
 */
abstract class LogStrategy {

    private final LogAdapter logAdapter;

    LogStrategy(LogAdapter logAdapter) {
        this.logAdapter = logAdapter;
    }

    protected LogAdapter getLogAdapter() {
        return logAdapter;
    }

    /**
     * If current strategy logging enabled.
     *
     * @param logger current logger
     * @return <code>true</code> if logging enabled, otherwise <code>false</code>
     */
    public abstract boolean isLogEnabled(Log logger);

    /**
     * Logs calling of the method.
     *
     * @param logger              current logger
     * @param method              method name
     * @param args                arguments of the method
     * @param argumentDescriptor  argument descriptor
     * @param customLoggerInfo
     * @param customToStringInfos
     */
    public abstract void logBefore(Log logger, Method method, String beanName, Object[] args, ArgumentDescriptor argumentDescriptor, Set<CustomLoggerInfo> customLoggerInfo, Set<CustomToStringInfo> customToStringInfos);

    /**
     * Logs returning from the method.
     *
     * @param logger              current logger
     * @param method              method name
     * @param argCount            parameter count number of the method
     * @param result              returned result of the method
     * @param customLoggerInfo
     * @param customToStringInfos
     */
    public abstract void logAfter(Log logger, Method method, String beanName, int argCount, Object result, Set<CustomLoggerInfo> customLoggerInfo, Set<CustomToStringInfo> customToStringInfos);

    /**
     * Logs throwing exception from the method.
     *
     * @param logger current logger
     * @param method method name
     * @param argCount parameter count number of the method
     * @param e exception thrown from the method
     * @param stackTrace if stack trace should be logged
     */
    public abstract void logException(Log logger, Method method,String beanName, int argCount, Exception e, boolean stackTrace);

    /**
     * Provides fatal strategy.
     */
    static final class FatalLogStrategy extends LogStrategy {

        FatalLogStrategy(LogAdapter logAdapter) {
            super(logAdapter);
        }

        @Override
        public boolean isLogEnabled(Log logger) {
            return logger.isFatalEnabled();
        }

        @Override
        public void logBefore(Log logger, Method method, String beanName, Object[] args, ArgumentDescriptor argumentDescriptor, Set<CustomLoggerInfo> customLoggerInfo, Set<CustomToStringInfo> customToStringInfos) {
            logger.fatal(getLogAdapter().toMessage(method,beanName, args, argumentDescriptor,customLoggerInfo, customToStringInfos));
        }

        @Override
        public void logAfter(Log logger, Method method, String beanName, int argCount, Object result, Set<CustomLoggerInfo> customLoggerInfo, Set<CustomToStringInfo> customToStringInfos) {
            logger.fatal(getLogAdapter().toMessage(method,beanName, argCount, result, customLoggerInfo,customToStringInfos));
        }

        @Override
        public void logException(Log logger, Method method,String beanName, int argCount, Exception e, boolean stackTrace) {
            if (stackTrace) {
                logger.fatal(getLogAdapter().toMessage(method,beanName, argCount, e, stackTrace), e);
            } else {
                logger.fatal(getLogAdapter().toMessage(method,beanName, argCount, e, stackTrace));
            }
        }
    }

    /**
     * Provides error strategy.
     */
    static final class ErrorLogStrategy extends LogStrategy {

        ErrorLogStrategy(LogAdapter logAdapter) {
            super(logAdapter);
        }

        @Override
        public boolean isLogEnabled(Log logger) {
            return logger.isErrorEnabled();
        }

        @Override
        public void logBefore(Log logger, Method method, String beanName , Object[] args, ArgumentDescriptor argumentDescriptor, Set<CustomLoggerInfo> customLoggerInfo, Set<CustomToStringInfo> customToStringInfos) {
            logger.error(getLogAdapter().toMessage(method,beanName, args, argumentDescriptor,customLoggerInfo, customToStringInfos));
        }

        @Override
        public void logAfter(Log logger, Method method, String beanName , int argCount, Object result, Set<CustomLoggerInfo> customLoggerInfo, Set<CustomToStringInfo> customToStringInfos) {
            logger.error(getLogAdapter().toMessage(method,beanName,argCount, result, customLoggerInfo,customToStringInfos));
        }

        @Override
        public void logException(Log logger, Method method,String beanName , int argCount, Exception e, boolean stackTrace) {
            if (stackTrace) {
                logger.error(getLogAdapter().toMessage(method,beanName, argCount, e, stackTrace), e);
            } else {
                logger.error(getLogAdapter().toMessage(method,beanName, argCount, e, stackTrace));
            }
        }

    }

    /**
     * Provides warn strategy.
     */
    static final class WarnLogStrategy extends LogStrategy {

        WarnLogStrategy(LogAdapter logAdapter) {
            super(logAdapter);
        }

        @Override
        public boolean isLogEnabled(Log logger) {
            return logger.isWarnEnabled();
        }

        @Override
        public void logBefore(Log logger, Method method, String beanName, Object[] args, ArgumentDescriptor argumentDescriptor, Set<CustomLoggerInfo> customLoggerInfo, Set<CustomToStringInfo> customToStringInfos) {
            logger.warn(getLogAdapter().toMessage(method,beanName, args, argumentDescriptor, customLoggerInfo, customToStringInfos));
        }

        @Override
        public void logAfter(Log logger, Method method, String beanName, int argCount, Object result, Set<CustomLoggerInfo> customLoggerInfo, Set<CustomToStringInfo> customToStringInfos) {
            logger.warn(getLogAdapter().toMessage(method,beanName, argCount, result, customLoggerInfo,customToStringInfos));
        }

        @Override
        public void logException(Log logger, Method method,String beanName, int argCount, Exception e, boolean stackTrace) {
            if (stackTrace) {
                logger.warn(getLogAdapter().toMessage(method,beanName, argCount, e, stackTrace), e);
            } else {
                logger.warn(getLogAdapter().toMessage(method,beanName, argCount, e, stackTrace));
            }
        }

    }

    /**
     * Provides info strategy.
     */
    static final class InfoLogStrategy extends LogStrategy {

        InfoLogStrategy(LogAdapter logAdapter) {
            super(logAdapter);
        }

        @Override
        public boolean isLogEnabled(Log logger) {
            return logger.isInfoEnabled();
        }

        @Override
        public void logBefore(Log logger, Method method, String beanName, Object[] args, ArgumentDescriptor argumentDescriptor, Set<CustomLoggerInfo> customLoggerInfo, Set<CustomToStringInfo> customToStringInfos) {
            logger.info(getLogAdapter().toMessage(method,beanName, args, argumentDescriptor,customLoggerInfo, customToStringInfos));
        }

        @Override
        public void logAfter(Log logger, Method method, String beanName, int argCount, Object result, Set<CustomLoggerInfo> customLoggerInfo, Set<CustomToStringInfo> customToStringInfos) {
            logger.info(getLogAdapter().toMessage(method,beanName, argCount, result, customLoggerInfo,customToStringInfos));
        }

        @Override
        public void logException(Log logger, Method method,String beanName, int argCount, Exception e, boolean stackTrace) {
            if (stackTrace) {
                logger.info(getLogAdapter().toMessage(method,beanName, argCount, e, stackTrace), e);
            } else {
                logger.info(getLogAdapter().toMessage(method,beanName, argCount, e, stackTrace));
            }
        }

    }

    /**
     * Provides debug strategy.
     */
    static final class DebugLogStrategy extends LogStrategy {


        DebugLogStrategy(LogAdapter logAdapter) {
            super(logAdapter);
        }

        @Override
        public boolean isLogEnabled(Log logger) {
            return logger.isDebugEnabled();
        }

        @Override
        public void logBefore(Log logger, Method method, String beanName, Object[] args, ArgumentDescriptor argumentDescriptor, Set<CustomLoggerInfo> customLoggerInfo, Set<CustomToStringInfo> customToStringInfos) {
            logger.debug(
                    getLogAdapter().toMessage(method,beanName,  args, argumentDescriptor,customLoggerInfo,customToStringInfos)
            );
        }

        @Override
        public void logAfter(Log logger, Method method, String beanName, int argCount, Object result, Set<CustomLoggerInfo> customLoggerInfo, Set<CustomToStringInfo> customToStringInfos) {
            logger.debug(getLogAdapter().toMessage(method,beanName,  argCount, result,customLoggerInfo,customToStringInfos));
        }

        @Override
        public void logException(Log logger, Method method,String beanName, int argCount, Exception e, boolean stackTrace) {
            if (stackTrace) {
                logger.debug(getLogAdapter().toMessage(method,beanName,  argCount, e, stackTrace), e);
            } else {
                logger.debug(getLogAdapter().toMessage(method,beanName,  argCount, e, stackTrace));
            }
        }

    }

    /**
     * Provides trace strategy.
     */
    static final class TraceLogStrategy extends LogStrategy {

        TraceLogStrategy(LogAdapter logAdapter) {
            super(logAdapter);
        }

        @Override
        public boolean isLogEnabled(Log logger) {
            return logger.isTraceEnabled();
        }

        @Override
        public void logBefore(Log logger, Method method, String beanName, Object[] args, ArgumentDescriptor argumentDescriptor, Set<CustomLoggerInfo> customLoggerInfo, Set<CustomToStringInfo> customToStringInfos) {
            logger.trace(getLogAdapter().toMessage(method,beanName,  args, argumentDescriptor,customLoggerInfo, customToStringInfos));
        }

        @Override
        public void logAfter(Log logger, Method method, String beanName, int argCount, Object result, Set<CustomLoggerInfo> customLoggerInfos, Set<CustomToStringInfo> customToStringInfos) {
            logger.trace(getLogAdapter().toMessage(method,beanName,  argCount, result, customLoggerInfos,customToStringInfos));
        }

        @Override
        public void logException(Log logger, Method method,String beanName, int argCount, Exception e, boolean stackTrace) {
            if (stackTrace) {
                logger.trace(getLogAdapter().toMessage(method,beanName,  argCount, e, stackTrace), e);
            } else {
                logger.trace(getLogAdapter().toMessage(method,beanName,  argCount, e, stackTrace));
            }
        }
    }

}
