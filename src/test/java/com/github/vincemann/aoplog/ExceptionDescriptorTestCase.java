/***********************************************************************************
 * Copyright (c) 2013. Nickolay Gerilovich. Russia.
 *   Some Rights Reserved.
 ************************************************************************************/

package com.github.vincemann.aoplog;

import com.github.vincemann.aoplog.api.annotation.LogException;
import com.github.vincemann.aoplog.parseAnnotation.SourceAwareAnnotationInfo;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.junit.rules.TestWatchman;
import org.junit.runners.model.FrameworkMethod;

import java.lang.reflect.Method;

import static com.github.vincemann.aoplog.TestSupportUtility.assertCollectionConsistOf;
import static com.github.vincemann.aoplog.TestSupportUtility.assertReflectionEquals;
import static org.junit.Assert.*;
import static org.junit.Assert.assertFalse;

/**
 * Tests {@link ExceptionDescriptor}.
 */
@LogException(value = {}, trace = @LogException.Exc(Exception.class))
public class ExceptionDescriptorTestCase {

    private ExceptionDescriptor methodExceptionDescriptor;
    private ExceptionDescriptor classExceptionDescriptor;

    @Rule
    public MethodRule watchman = new TestWatchman() {
        public void starting(FrameworkMethod method) {
            Method currMethod = method.getMethod();
            LogException logException = currMethod.getDeclaredAnnotation(LogException.class);
            methodExceptionDescriptor = logException==null? null : new ExceptionDescriptor.Builder(
                    SourceAwareAnnotationInfo.<LogException>builder()
                            .annotation(logException)
                            .classLevel(false)
                            .declaringClass(ExceptionDescriptorTestCase.class)
                            .build())
                    .build();

            LogException classLogException = ExceptionDescriptorTestCase.class.getDeclaredAnnotation(LogException.class);
            classExceptionDescriptor = classLogException==null? null : new ExceptionDescriptor.Builder(
                    SourceAwareAnnotationInfo.<LogException>builder()
                            .annotation(classLogException)
                            .classLevel(true)
                            .declaringClass(ExceptionDescriptorTestCase.class)
                            .build())
                    .build();
        }
    };

//    @Test(expected = NullPointerException.class)
//    public void testNoAnnotation() throws Exception {
//        new ExceptionDescriptor.Builder(null).build();
//    }

    @Test
    @LogException
    public void testDefaultAnnotation() throws Exception {
        assertCollectionConsistOf(methodExceptionDescriptor.getDefinedExceptions(), Exception.class);

        assertNull(methodExceptionDescriptor.getExceptionSeverity(IllegalArgumentException.class));
        assertReflectionEquals(ExceptionSeverity.create(Severity.ERROR, true), methodExceptionDescriptor.getExceptionSeverity(Exception.class));
    }

    @Test
    @LogException(value = @LogException.Exc(value = RuntimeException.class, stacktrace = true),
            info = {@LogException.Exc(NoMoneyException.class), @LogException.Exc(AccountBlockedException.class)},
            warn = {@LogException.Exc(AccountException.class), @LogException.Exc(value = IllegalArgumentException.class, stacktrace = true)})
    public void testCustomAnnotation() throws Exception {
        assertCollectionConsistOf(methodExceptionDescriptor.getDefinedExceptions(),
                RuntimeException.class, NoMoneyException.class, AccountBlockedException.class, AccountException.class, IllegalArgumentException.class);

        assertNull(methodExceptionDescriptor.getExceptionSeverity(Exception.class));
        assertReflectionEquals(ExceptionSeverity.create(Severity.ERROR, true), methodExceptionDescriptor.getExceptionSeverity(RuntimeException.class));
        assertReflectionEquals(ExceptionSeverity.create(Severity.INFO, false), methodExceptionDescriptor.getExceptionSeverity(NoMoneyException.class));
        assertReflectionEquals(ExceptionSeverity.create(Severity.INFO, false), methodExceptionDescriptor.getExceptionSeverity(AccountBlockedException.class));
        assertReflectionEquals(ExceptionSeverity.create(Severity.WARN, false), methodExceptionDescriptor.getExceptionSeverity(AccountException.class));
        assertReflectionEquals(ExceptionSeverity.create(Severity.WARN, true), methodExceptionDescriptor.getExceptionSeverity(IllegalArgumentException.class));
    }

    @Test
    public void testGetClassLevelOnlyExceptionAnnotation() throws Exception {
        //read from class annotation
        assertNotNull(classExceptionDescriptor);
        assertSame(Severity.TRACE, classExceptionDescriptor.getExceptionSeverity(Exception.class).getSeverity());
        LogException exceptionAnnotation = classExceptionDescriptor.getExceptionAnnotationInfo().getAnnotation();
        assertEquals(0, exceptionAnnotation.value().length);
        assertEquals(1, exceptionAnnotation.trace().length);
        assertArrayEquals(exceptionAnnotation.trace()[0].value(), new Object[]{Exception.class});
        assertFalse(exceptionAnnotation.trace()[0].stacktrace());
    }

    @Test
    public void testGetExceptionAnnotationByMethodPriority() throws Exception {

    }

    @Test
    @LogException({})
    public void testEmptyAnnotation() throws Exception {
        assertTrue(methodExceptionDescriptor.getDefinedExceptions().isEmpty());
    }

    private static class AccountException extends Exception {

    }

    private static class NoMoneyException extends ExceptionDescriptorTestCase.AccountException {

    }

    private static class AccountBlockedException extends ExceptionDescriptorTestCase.AccountException {

    }

}
