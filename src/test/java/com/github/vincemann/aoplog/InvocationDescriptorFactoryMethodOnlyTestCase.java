/***********************************************************************************
 * Copyright (c) 2013. Nickolay Gerilovich. Russia.
 *   Some Rights Reserved.
 ************************************************************************************/

package com.github.vincemann.aoplog;

import com.github.vincemann.aoplog.api.LogInteraction;
import com.github.vincemann.aoplog.parseAnnotation.AnnotationInfo;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.junit.rules.TestWatchman;
import org.junit.runners.model.FrameworkMethod;

import static org.junit.Assert.*;

/**
 * Tests {@link InvocationDescriptor} with log annotated methods.
 */
public class InvocationDescriptorFactoryMethodOnlyTestCase {

    InvocationDescriptor currDescriptor;
    InvocationDescriptorFactory factory = new InvocationDescriptorFactoryImpl();

    @Rule
    public MethodRule watchman = new TestWatchman() {
        public void starting(FrameworkMethod method) {
            LogInteraction classLogAnnotation = InvocationDescriptorFactoryMethodOnlyTestCase.class.getDeclaredAnnotation(LogInteraction.class);
            LogInteraction methodLogAnnotation = method.getMethod().getDeclaredAnnotation(LogInteraction.class);
            AnnotationInfo<LogInteraction> methodLog = methodLogAnnotation==null? null : new AnnotationInfo<>(methodLogAnnotation,InvocationDescriptorFactoryMethodOnlyTestCase.class);
            AnnotationInfo<LogInteraction> classLog = classLogAnnotation==null? null : new AnnotationInfo<>(classLogAnnotation,InvocationDescriptorFactoryMethodOnlyTestCase.class);
            currDescriptor = factory.create(methodLog,classLog);
        }
    };

    @After
    public void tearDown() throws Exception {
        currDescriptor=null;
    }

    @Test
    public void testNoAnnotations() throws Exception {
        assertNull(currDescriptor.getSeverity());
    }

    @Test
    @LogInteraction
    public void testDefaultAnnotation() throws Exception {
        assertSame(Severity.DEBUG, currDescriptor.getSeverity());
    }


    @Test
    @LogInteraction(Severity.INFO)
    public void testSpecifiedLogLevel() throws Exception {
        assertSame(Severity.INFO, currDescriptor.getSeverity());
    }
}
