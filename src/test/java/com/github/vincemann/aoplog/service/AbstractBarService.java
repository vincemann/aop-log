/***********************************************************************************
 * Copyright (c) 2013. Nickolay Gerilovich. Russia.
 *   Some Rights Reserved.
 ************************************************************************************/

package com.github.vincemann.aoplog.service;


import com.github.vincemann.aoplog.api.annotation.LogConfig;
import com.github.vincemann.aoplog.api.annotation.LogInteraction;

/**
 * Base implementation of {@link BarService}.
 */
@LogInteraction
@LogConfig(logAllChildrenMethods = true)
public abstract class AbstractBarService implements BarService {

    @Override
    public void inAbstract(String aFirst, String aSecond) {
        // do not override
    }
}
