/***********************************************************************************
 * Copyright (c) 2013. Nickolay Gerilovich. Russia.
 *   Some Rights Reserved.
 ************************************************************************************/

package com.github.vincemann.aoplog.service;

import com.github.vincemann.aoplog.api.LogInteraction;
import com.github.vincemann.aoplog.api.Lp;

/**
 * Implements {@link BarService}. Log level is lower than in {@link AbstractBarService}, {@link BarService}.
 */
public class ExtendedBarService extends AbstractBarService {

    @Override
    public void inExtendedLogInSuperOnly(String eFirst, String eSecond) {
        // Log annotations in interface
    }

    @LogInteraction
    @Override
    public void inExtended(String eFirst, String eSecond) {
        // Log annotation here only
    }

    @LogInteraction
    @Override
    public void overridden(String eFirst, @Lp String eSecond) {
        // Log annotation altered
    }

    @Override
    public void overriddenLogInAbstractOnly(String eFirst, String eSecond) {
        // Log annotation in parent and not altered here
    }
}
