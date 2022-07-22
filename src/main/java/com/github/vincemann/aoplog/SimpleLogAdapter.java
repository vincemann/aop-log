/***********************************************************************************
 * Copyright (c) 2013. Nickolay Gerilovich. Russia.
 *   Some Rights Reserved.
 ************************************************************************************/

package com.github.vincemann.aoplog;

import com.github.vincemann.aoplog.api.CustomLogger;

/**
 * Simple log adapter.
 */
public class SimpleLogAdapter extends AbstractLogAdapter {

    @Override
    protected String asString(Object value, CustomLogger customLogger) {
        return String.valueOf(value);
    }

}
