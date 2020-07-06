/***********************************************************************************
 * Copyright (c) 2013. Nickolay Gerilovich. Russia.
 *   Some Rights Reserved.
 ************************************************************************************/

package com.github.vincemann.aoplog.service;

/**
 * Implements {@link BarService}. Log level is lower than in {@link AbstractBarService}, {@link BarService}.
 */
public class ExtendedBarService extends AbstractBarService {


    @Override
    public void inExtended(String eFirst, String eSecond) {
        // Log annotation here only
    }

    public void onlyInImpl(String eFirst, String eSecond){

    }


}
