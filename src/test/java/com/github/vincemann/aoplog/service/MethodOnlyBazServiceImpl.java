/***********************************************************************************
 * Copyright (c) 2013. Nickolay Gerilovich. Russia.
 *   Some Rights Reserved.
 ************************************************************************************/

package com.github.vincemann.aoplog.service;


import com.github.vincemann.aoplog.Severity;
import com.github.vincemann.aoplog.api.annotation.LogInteraction;

/**
 * Implements {@link BazService}.
 */
public class MethodOnlyBazServiceImpl extends AbstractBazService {

    @Override
    public void inImpl(String mFirst, String mSecond) {
        // nothing to do
    }

    @LogInteraction(Severity.INFO)
    @Override
    public void inInterface(String mFirst, String mSecond) {

    }

    public void setInImpl(String mFirst, String mSecond){

    }

    @LogInteraction(Severity.INFO)
    @Override
    public void getInImpl(String mFirst, String mSecond) {

    }

    public void onlyInImpl(String cmFirst, String cmSecond){

    }
}
