/***********************************************************************************
 * Copyright (c) 2013. Nickolay Gerilovich. Russia.
 *   Some Rights Reserved.
 ************************************************************************************/

package com.github.vincemann.aoplog;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests {@link ToStringDetector}.
 */
public class ToStringDetectorTestCase {

    @Test
    public void testHasToString() throws Exception {
        assertFalse(ToStringDetector.INSTANCE.hasToString(Object.class));
        assertFalse(ToStringDetector.INSTANCE.hasToString(this.getClass()));
        assertTrue(ToStringDetector.INSTANCE.hasToString(Integer.class));
        assertTrue(ToStringDetector.INSTANCE.hasToString(ArrayList.class));
    }

    @Test
    public void testInterfaceHasToString() throws Exception {
        assertTrue(ToStringDetector.INSTANCE.hasToString(Foo.class));
        Foo foo = new Foo() {
        };
        assertFalse(ToStringDetector.INSTANCE.hasToString(foo.getClass()));
    }

    private static interface Foo {
        @Override
        String toString();
    }
}
