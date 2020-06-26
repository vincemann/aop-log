/***********************************************************************************
 * Copyright (c) 2013. Nickolay Gerilovich. Russia.
 *   Some Rights Reserved.
 ************************************************************************************/

package com.github.vincemann.aoplog;

import com.github.vincemann.aoplog.api.LogInteraction;
import com.github.vincemann.aoplog.api.LogAllInteractions;
import com.github.vincemann.aoplog.api.LogException;
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
@LogAllInteractions
@LogException
public class InvocationDescriptorClassTestCase {

    Method currMethod;
    InvocationDescriptor currDescriptor;
    AnnotationParser annotationParser = new HierarchicalAnnotationParser();


    @Rule
    public MethodRule watchman = new TestWatchman() {
        public void starting(FrameworkMethod method) {
            currMethod = method.getMethod();
            LogInteraction methodLog = annotationParser.fromMethod(currMethod, LogInteraction.class);
            LogAllInteractions classLog = annotationParser.fromClass(getClass(), LogAllInteractions.class);
            AnnotationInfo<LogException> logException = annotationParser.fromMethodOrClass(currMethod, LogException.class);
            currDescriptor = new InvocationDescriptor.Builder(methodLog,classLog,logException).build();
        }
    };

    @After
    public void tearDown() throws Exception {
        currMethod = null;
//        logExceptionAnnotationInfo = null;
//        methodLog = null;
    }

    @Test
    public void testNoAnnotations() throws Exception {
        assertSame(Severity.DEBUG, currDescriptor.getSeverity());
        assertNotNull(currDescriptor.getExceptionAnnotation());
    }

    @Test
    @LogInteraction(Severity.TRACE)
    public void testGetSeverityByMethodPriority() throws Exception {
        assertSame(Severity.TRACE, currDescriptor.getSeverity());
        assertNotNull(currDescriptor.getExceptionAnnotation());
    }


    @Test
    @LogException(value = {}, trace = @LogException.Exc(Exception.class))
    public void testGetExceptionAnnotationByMethodPriority() throws Exception {
        //read from class annotation
        assertSame(Severity.DEBUG, currDescriptor.getSeverity());
        LogException exceptionAnnotation = currDescriptor.getExceptionAnnotation().getAnnotation();
        assertEquals(0, exceptionAnnotation.value().length);
        assertEquals(1, exceptionAnnotation.trace().length);
        assertArrayEquals(exceptionAnnotation.trace()[0].value(), new Object[]{Exception.class});
        assertFalse(exceptionAnnotation.trace()[0].stacktrace());
    }
}
