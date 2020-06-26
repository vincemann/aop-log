/***********************************************************************************
 * Copyright (c) 2013. Nickolay Gerilovich. Russia.
 *   Some Rights Reserved.
 ************************************************************************************/

package com.github.vincemann.aoplog.service;


import com.github.vincemann.aoplog.Severity;
import com.github.vincemann.aoplog.api.LogInteraction;
import com.github.vincemann.aoplog.api.Lp;

/**
 * Base implementation of {@link BarService}.
 */
public abstract class AbstractBarService implements BarService {

    @LogInteraction(Severity.INFO)
    @Override
    public void inAbstract(String aFirst, String aSecond) {
        // do not override
    }

    @LogInteraction(Severity.INFO)
    @Override
    public void overridden(@Lp String aFirst, String aSecond) {
        // should be overridden
    }

    @LogInteraction(Severity.INFO)
    @Override
    public void overriddenLogInAbstractOnly(String aFirst, String aSecond) {
        // should be overridden
    }
}
