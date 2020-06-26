/***********************************************************************************
 * Copyright (c) 2013. Nickolay Gerilovich. Russia.
 *   Some Rights Reserved.
 ************************************************************************************/

package com.github.vincemann.aoplog.service;

import com.github.vincemann.aoplog.Severity;
import com.github.vincemann.aoplog.api.AopLoggable;
import com.github.vincemann.aoplog.api.LogInteraction;

/**
 * Baz service interface.
 */
public interface BazService extends AopLoggable {

    void inAbstract(String iFirst, String iSecond);

    void inImpl(String iFirst, String iSecond);

    @LogInteraction(Severity.TRACE)
    void inInterface(String iFirst, String iSecond);
}
