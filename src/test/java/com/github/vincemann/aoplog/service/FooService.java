/***********************************************************************************
 * Copyright (c) 2013. Nickolay Gerilovich. Russia.
 *   Some Rights Reserved.
 ************************************************************************************/

package com.github.vincemann.aoplog.service;

import com.github.vincemann.aoplog.api.AopLoggable;
import com.github.vincemann.aoplog.api.LogParam;

import java.io.IOException;

/**
 * Service interface.
 */
public interface FooService extends AopLoggable {

    void voidMethodZero();

    String stringMethodOne(String first);

    String stringMethodTwo(String first, String second);

    String stringMethodThree(String first, String second, String third);

    String stringMethodTwoVarargs(String first, @LogParam String... second);

    void voidExcMethodZero() throws IOException;
}
