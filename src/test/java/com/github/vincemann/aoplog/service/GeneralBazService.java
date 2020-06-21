/***********************************************************************************
 * Copyright (c) 2013. Nickolay Gerilovich. Russia.
 *   Some Rights Reserved.
 ************************************************************************************/

package com.github.vincemann.aoplog.service;


import com.github.vincemann.aoplog.Severity;
import com.github.vincemann.aoplog.annotation.Log;

/**
 * Implements {@link BazService}.
 */
@Log(Severity.INFO)
public class GeneralBazService extends AbstractBazService {

    @Override
    public void inImpl(String gFirst, String gSecond) {
        // nothing to do
    }
}
