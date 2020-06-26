/***********************************************************************************
 * Copyright (c) 2013. Nickolay Gerilovich. Russia.
 *   Some Rights Reserved.
 ************************************************************************************/

package com.github.vincemann.aoplog.service;


import com.github.vincemann.aoplog.api.LogAllInteractions;

/**
 * Implements {@link BazService}.
 */
@LogAllInteractions
public class ClassOnlyBazServiceImpl extends AbstractBazService {

    @Override
    public void inImpl(String xFirst, String xSecond) {
        // nothing to do
    }

    @Override
    public void inInterface(String iFirst, String iSecond) {

    }
}
