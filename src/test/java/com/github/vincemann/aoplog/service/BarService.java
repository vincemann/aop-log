/***********************************************************************************
 * Copyright (c) 2013. Nickolay Gerilovich. Russia.
 *   Some Rights Reserved.
 ************************************************************************************/

package com.github.vincemann.aoplog.service;


import com.github.vincemann.aoplog.api.AopLoggable;
import com.github.vincemann.aoplog.api.LogConfig;
import com.github.vincemann.aoplog.api.LogInteraction;

/**
 * Bar service interface.
 */
public interface BarService extends AopLoggable {

    void inAbstract(String iFirst, String iSecond);

    void inExtended(String iFirst, String iSecond);

}
