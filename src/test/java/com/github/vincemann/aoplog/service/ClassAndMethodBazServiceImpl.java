/***********************************************************************************
 * Copyright (c) 2013. Nickolay Gerilovich. Russia.
 *   Some Rights Reserved.
 ************************************************************************************/

package com.github.vincemann.aoplog.service;


import com.github.vincemann.aoplog.Severity;
import com.github.vincemann.aoplog.api.annotation.LogConfig;
import com.github.vincemann.aoplog.api.annotation.LogInteraction;

/**
 * Implements {@link BazService}.
 */
@LogInteraction(Severity.INFO)
@LogConfig(ignoreGetters = true,ignoreSetters = false)
public class ClassAndMethodBazServiceImpl extends AbstractBazService {

    @Override
    public void inImpl(String cmFirst, String cmSecond) {
        // nothing to do
    }

    @Override
    public void inInterface(String iFirst, String iSecond) {

    }

    @Override
    public void getInImpl(String cmFirst, String cmSecond) {

    }

    @Override
    public void setInImpl(String cmFirst, String cmSecond) {

    }

}
