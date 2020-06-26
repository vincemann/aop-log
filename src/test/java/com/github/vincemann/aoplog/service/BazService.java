/***********************************************************************************
 * Copyright (c) 2013. Nickolay Gerilovich. Russia.
 *   Some Rights Reserved.
 ************************************************************************************/

package com.github.vincemann.aoplog.service;

import com.github.vincemann.aoplog.api.AopLoggable;

/**
 * Baz service interface.
 */
public interface BazService extends AopLoggable {

    void inAbstract(String iFirst, String iSecond);

    void inImpl(String iFirst, String iSecond);
}
