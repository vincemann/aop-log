/***********************************************************************************
 * Copyright (c) 2013. Nickolay Gerilovich. Russia.
 *   Some Rights Reserved.
 ************************************************************************************/

package com.github.vincemann.aoplog.service;

import com.github.vincemann.aoplog.api.LogAllInteractions;

/**
 * Base implementation of {@link BazService}.
 */
@LogAllInteractions
public abstract class AbstractBazService implements BazService {

    @Override
    public void inAbstract(String iFirst, String iSecond) {
        // do not override
    }

}
