/***********************************************************************************
 * Copyright (c) 2013. Nickolay Gerilovich. Russia.
 *   Some Rights Reserved.
 ************************************************************************************/

package com.github.vincemann.aoplog.service;


import com.github.vincemann.aoplog.api.Log;
import com.github.vincemann.aoplog.api.LogAll;

/**
 * Implements {@link BazService}.
 */
@LogAll
public class AuxBazService extends AbstractBazService {

    @Override
    public void inImpl(String xFirst, String xSecond) {
        // nothing to do
    }
}
