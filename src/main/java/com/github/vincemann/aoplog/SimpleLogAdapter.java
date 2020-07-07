/***********************************************************************************
 * Copyright (c) 2013. Nickolay Gerilovich. Russia.
 *   Some Rights Reserved.
 ************************************************************************************/

package com.github.vincemann.aoplog;

import java.lang.reflect.Method;

/**
 * Simple log adapter.
 */
public class SimpleLogAdapter extends AbstractLogAdapter {

    @Override
    protected String asString(Object value) {
        return String.valueOf(value);
    }

    @Override
    public void onUnLoggedException(Method method, Exception e) {

    }
}
