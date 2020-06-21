/***********************************************************************************
 * Copyright (c) 2013. Nickolay Gerilovich. Russia.
 *   Some Rights Reserved.
 ************************************************************************************/

package com.github.vincemann.aoplog;

import com.github.vincemann.aoplog.annotation.Log;
import com.github.vincemann.aoplog.annotation.LogException;
import com.github.vincemann.aoplog.annotation.*;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.junit.rules.TestWatchman;
import org.junit.runners.model.FrameworkMethod;

import java.lang.reflect.Method;

import static org.junit.Assert.*;

/**
 * Tests {@link InvocationDescriptor} with log annotated methods and class.
 */
@Log(level = Severity.INFO)
@LogException
public class InvocationDescriptorClassTestCase {

    private Method currMethod;
    private AnnotationInfo<Log> loggingAnnotationInfo;
    private AnnotationInfo<LogException> logExceptionAnnotationInfo;
    private AnnotationParser annotationParser = new HierarchicalAnnotationParser();


    @Rule
    public MethodRule watchman = new TestWatchman() {
        public void starting(FrameworkMethod method) {
            currMethod = method.getMethod();
            loggingAnnotationInfo = annotationParser.fromMethodOrClass(currMethod, Log.class);
            logExceptionAnnotationInfo = annotationParser.fromMethodOrClass(currMethod, LogException.class);
        }
    };

    @After
    public void tearDown() throws Exception {
        currMethod = null;
        logExceptionAnnotationInfo = null;
        loggingAnnotationInfo = null;
    }

    @Test
    public void testNoAnnotations() throws Exception {
        InvocationDescriptor descriptor = new InvocationDescriptor.Builder(loggingAnnotationInfo,logExceptionAnnotationInfo).build();
        assertSame(Severity.DEBUG, descriptor.getSeverity());
        assertNotNull(descriptor.getExceptionAnnotation());
    }

    @Test
    @Log(Severity.TRACE)
    public void testGetSeverityByMethodPriority() throws Exception {
        InvocationDescriptor descriptor = new InvocationDescriptor.Builder(loggingAnnotationInfo,logExceptionAnnotationInfo).build();
        assertSame(Severity.TRACE, descriptor.getSeverity());
        assertNotNull(descriptor.getExceptionAnnotation());
    }


    @Test
    @LogException(value = {}, trace = @LogException.Exc(Exception.class))
    public void testGetExceptionAnnotationByMethodPriority() throws Exception {
        InvocationDescriptor descriptor = new InvocationDescriptor.Builder(loggingAnnotationInfo,logExceptionAnnotationInfo).build();
        assertSame(Severity.DEBUG, descriptor.getSeverity());
        LogException exceptionAnnotation = descriptor.getExceptionAnnotation();
        assertEquals(0, exceptionAnnotation.value().length);
        assertEquals(1, exceptionAnnotation.trace().length);
        assertArrayEquals(exceptionAnnotation.trace()[0].value(), new Object[]{Exception.class});
        assertFalse(exceptionAnnotation.trace()[0].stacktrace());
    }
}
