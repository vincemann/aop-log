/***********************************************************************************
 * Copyright (c) 2013. Nickolay Gerilovich. Russia.
 *   Some Rights Reserved.
 ************************************************************************************/

package com.github.vincemann.aoplog.service;

/**
 * Base implementation of {@link BazService}.
 */
public abstract class AbstractBazService implements BazService {

    @Override
    public void inAbstract(String iFirst, String iSecond) {
        // do not override
    }
}
