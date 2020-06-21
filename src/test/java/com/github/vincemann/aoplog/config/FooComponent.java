/***********************************************************************************
 * Copyright (c) 2013. Nickolay Gerilovich. Russia.
 *   Some Rights Reserved.
 ************************************************************************************/

package com.github.vincemann.aoplog.config;

import com.github.vincemann.aoplog.annotation.Log;
import org.springframework.stereotype.Component;

/**
 * Simple component, does not implement any interface, has non public method.
 */
@Component
public class FooComponent {

    @Log()
    void voidMethodZero() {
        // nothing to do
    }

    @Log()
    public IntHolder intMethodZero() {
        return new IntHolder();
    }

}
