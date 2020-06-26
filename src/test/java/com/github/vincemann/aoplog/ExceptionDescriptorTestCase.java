/***********************************************************************************
 * Copyright (c) 2013. Nickolay Gerilovich. Russia.
 *   Some Rights Reserved.
 ************************************************************************************/

package com.github.vincemann.aoplog;

import com.github.vincemann.aoplog.api.LogException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.junit.rules.TestWatchman;
import org.junit.runners.model.FrameworkMethod;

import java.lang.reflect.Method;

import static com.github.vincemann.aoplog.TestSupportUtility.assertCollectionConsistOf;
import static com.github.vincemann.aoplog.TestSupportUtility.assertReflectionEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests {@link ExceptionDescriptor}.
 */
public class ExceptionDescriptorTestCase {

    private ExceptionDescriptor currDescriptor;

    @Rule
    public MethodRule watchman = new TestWatchman() {
        public void starting(FrameworkMethod method) {
            Method currMethod = method.getMethod();
            LogException logException = currMethod.getAnnotation(LogException.class);
            currDescriptor = new ExceptionDescriptor.Builder(
                    AnnotationInfo.<LogException>builder()
                            .annotation(logException)
                            .classLevel(false)
                            .build())
                    .build();
        }
    };

    @Test(expected = NullPointerException.class)
    public void testNoAnnotation() throws Exception {
        new ExceptionDescriptor.Builder(null).build();
    }

    @Test
    @LogException
    public void testDefaultAnnotation() throws Exception {
        assertCollectionConsistOf(currDescriptor.getDefinedExceptions(), Exception.class);

        assertNull(currDescriptor.getExceptionSeverity(IllegalArgumentException.class));
        assertReflectionEquals(ExceptionSeverity.create(Severity.ERROR, true), currDescriptor.getExceptionSeverity(Exception.class));
    }

    @Test
    @LogException(value = @LogException.Exc(value = RuntimeException.class, stacktrace = true),
            info = {@LogException.Exc(NoMoneyException.class), @LogException.Exc(AccountBlockedException.class)},
            warn = {@LogException.Exc(AccountException.class), @LogException.Exc(value = IllegalArgumentException.class, stacktrace = true)})
    public void testCustomAnnotation() throws Exception {
        assertCollectionConsistOf(currDescriptor.getDefinedExceptions(),
                RuntimeException.class, NoMoneyException.class, AccountBlockedException.class, AccountException.class, IllegalArgumentException.class);

        assertNull(currDescriptor.getExceptionSeverity(Exception.class));
        assertReflectionEquals(ExceptionSeverity.create(Severity.ERROR, true), currDescriptor.getExceptionSeverity(RuntimeException.class));
        assertReflectionEquals(ExceptionSeverity.create(Severity.INFO, false), currDescriptor.getExceptionSeverity(NoMoneyException.class));
        assertReflectionEquals(ExceptionSeverity.create(Severity.INFO, false), currDescriptor.getExceptionSeverity(AccountBlockedException.class));
        assertReflectionEquals(ExceptionSeverity.create(Severity.WARN, false), currDescriptor.getExceptionSeverity(AccountException.class));
        assertReflectionEquals(ExceptionSeverity.create(Severity.WARN, true), currDescriptor.getExceptionSeverity(IllegalArgumentException.class));
    }

    @Test
    @LogException({})
    public void testEmptyAnnotation() throws Exception {
        assertTrue(currDescriptor.getDefinedExceptions().isEmpty());
    }

    private static class AccountException extends Exception {

    }

    private static class NoMoneyException extends AccountException {

    }

    private static class AccountBlockedException extends AccountException {

    }

}
