/***********************************************************************************
 * Copyright (c) 2013. Nickolay Gerilovich. Russia.
 *   Some Rights Reserved.
 ************************************************************************************/

package com.github.vincemann.aoplog.service;

import com.github.vincemann.aoplog.api.LogConfig;
import com.github.vincemann.aoplog.api.LogInteraction;

/**
 * Base implementation of {@link BazService}.
 */
@LogInteraction
@LogConfig(ignoreSetters = true)
public abstract class AbstractBazService implements BazService {

    @Override
    public void inAbstract(String aFirst, String aSecond) {
        // do not override
    }


}
