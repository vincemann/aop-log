/***********************************************************************************
 * Copyright (c) 2013. Nickolay Gerilovich. Russia.
 *   Some Rights Reserved.
 ************************************************************************************/

package com.github.vincemann.aoplog.service;


import com.github.vincemann.aoplog.Severity;
import com.github.vincemann.aoplog.api.LogInteraction;

/**
 * Implements {@link BazService}.
 */
@LogInteraction(Severity.INFO)
public class ClassAndMethodBazServiceImpl extends AbstractBazService {

    @Override
    public void inImpl(String gFirst, String gSecond) {
        // nothing to do
    }

    @Override
    public void inInterface(String iFirst, String iSecond) {

    }
}
