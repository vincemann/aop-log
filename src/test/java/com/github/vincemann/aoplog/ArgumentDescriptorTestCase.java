/***********************************************************************************
 * Copyright (c) 2013. Nickolay Gerilovich. Russia.
 *   Some Rights Reserved.
 ************************************************************************************/

package com.github.vincemann.aoplog;

import com.github.vincemann.aoplog.api.LogParam;
import org.junit.Test;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;

import java.lang.reflect.Method;

import static org.junit.Assert.*;

/**
 * Tests {@link ArgumentDescriptor}.
 */
public class ArgumentDescriptorTestCase {

    private LocalVariableTableParameterNameDiscoverer parameterNameDiscoverer = new LocalVariableTableParameterNameDiscoverer();

    private Method getMethod(Class<?> clazz, String methodName) throws NoSuchMethodException {
        for (Method method : clazz.getDeclaredMethods()) {
            if (methodName.equals(method.getName())) {
                return method;
            }
        }
        throw new NoSuchMethodException(methodName);
    }

    @Test
    public void testNoArguments() throws Exception {
        Method method = getMethod(getClass(), "noArguments");
        ArgumentDescriptor descriptor = new ArgumentDescriptor.Builder(method, method.getParameterTypes().length, parameterNameDiscoverer).build();
        assertEquals(-1, descriptor.nextLoggedArgumentIndex(0));
        assertNull(descriptor.getNames());
    }

    private void noArguments() {
        // used in test methods viq reflection
    }

    @Test
    public void testOneArgument() throws Exception {
        Method method = getMethod(getClass(), "oneArgument");
        ArgumentDescriptor descriptor = new ArgumentDescriptor.Builder(method, method.getParameterTypes().length, parameterNameDiscoverer).build();
        assertEquals(0, descriptor.nextLoggedArgumentIndex(0));
        assertTrue(descriptor.isArgumentIndexLogged(0));
        assertEquals(-1, descriptor.nextLoggedArgumentIndex(1));
        if (descriptor.getNames() != null) {
            assertArrayEquals(new String[]{"foo"}, descriptor.getNames());
        }
    }

    private void oneArgument(String foo) {
        // used in test methods viq reflection
    }

    @Test
    public void testTwoArguments() throws Exception {
        Method method = getMethod(getClass(), "twoArguments");
        ArgumentDescriptor descriptor = new ArgumentDescriptor.Builder(method, method.getParameterTypes().length, parameterNameDiscoverer).build();
        assertEquals(0, descriptor.nextLoggedArgumentIndex(0));
        assertTrue(descriptor.isArgumentIndexLogged(0));
        assertEquals(-1, descriptor.nextLoggedArgumentIndex(1));
        if (descriptor.getNames() != null) {
            assertArrayEquals(new String[]{"foo", "foo2"}, descriptor.getNames());
        }
    }

    private void twoArguments(@LogParam String foo, String foo2) {
        // used in test methods viq reflection
    }


    @Test
    public void testVarArguments() throws Exception {
        Method method = getMethod(getClass(), "varArguments");
        ArgumentDescriptor descriptor = new ArgumentDescriptor.Builder(method, method.getParameterTypes().length, parameterNameDiscoverer).build();
        assertEquals(1, descriptor.nextLoggedArgumentIndex(0));
        assertFalse(descriptor.isArgumentIndexLogged(0));
        assertTrue(descriptor.isArgumentIndexLogged(1));
        assertEquals(1, descriptor.nextLoggedArgumentIndex(1));
        assertEquals(-1, descriptor.nextLoggedArgumentIndex(2));
        if (descriptor.getNames() != null) {
            assertArrayEquals(new String[]{"foo", "foo2"}, descriptor.getNames());
        }
    }

    private void varArguments(String foo, @LogParam String... foo2) {
        // used in test methods viq reflection
    }

}
