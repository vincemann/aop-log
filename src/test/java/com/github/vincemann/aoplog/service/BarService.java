/***********************************************************************************
 * Copyright (c) 2013. Nickolay Gerilovich. Russia.
 *   Some Rights Reserved.
 ************************************************************************************/

package com.github.vincemann.aoplog.service;


import com.github.vincemann.aoplog.Severity;
import com.github.vincemann.aoplog.api.AopLoggable;
import com.github.vincemann.aoplog.api.LogInteraction;

/**
 * Bar service interface.
 */
public interface BarService extends AopLoggable {

    @LogInteraction(Severity.WARN)
    void inExtendedLogInSuperOnly(String iFirst, String iSecond);

    void inAbstract(String iFirst, String iSecond);

    void inExtended(String iFirst, String iSecond);

    void overridden(String iFirst, String iSecond);

    void overriddenLogInAbstractOnly(String iFirst, String iSecond);
}
