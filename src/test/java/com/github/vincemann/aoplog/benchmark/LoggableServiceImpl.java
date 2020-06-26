/***********************************************************************************
 * Copyright (c) 2013. Nickolay Gerilovich. Russia.
 *   Some Rights Reserved.
 ************************************************************************************/

package com.github.vincemann.aoplog.benchmark;

import com.github.vincemann.aoplog.Severity;
import com.github.vincemann.aoplog.api.LogInteraction;
import com.github.vincemann.aoplog.api.Lp;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implements {@link LoggableService}, provides three logging types: no, direct and aop logging.
 */
public class LoggableServiceImpl implements LoggableService {
    private static Log LOGGER = LogFactory.getLog(LoggableServiceImpl.class);

    @Override
    public int logClearMethod(String a, int b) {
        return b + 1;
    }

    @Override
    public int logManualMethod(String a, int b) {
        LOGGER.isDebugEnabled();
        LOGGER.debug("calling logManualMethod: a=" + a + ", b=" + b);
        int res = b + 1;
        LOGGER.isDebugEnabled();
        LOGGER.debug("returning logManualMethod: res=" + res);
        return res;
    }

    @LogInteraction(Severity.TRACE)
    @Override
    public int aopLogMethod(String a, @Lp int b) {
        return b + 1;
    }
}
