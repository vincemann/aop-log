/***********************************************************************************
 * Copyright (c) 2013. Nickolay Gerilovich. Russia.
 *   Some Rights Reserved.
 ************************************************************************************/

package com.github.vincemann.aoplog;

import com.github.vincemann.aoplog.api.annotation.LogInteraction;
import com.github.vincemann.aoplog.annotation.AnnotationInfo;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.junit.rules.TestWatchman;
import org.junit.runners.model.FrameworkMethod;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

/**
 * Tests {@link InvocationDescriptor} with log annotated methods and class.
 */
@LogInteraction
public class InvocationDescriptorFactoryMixedTestCase {

    InvocationDescriptor currDescriptor;
    InvocationDescriptorFactory factory = new InvocationDescriptorFactoryImpl();

    @Rule
    public MethodRule watchman = new TestWatchman() {
        public void starting(FrameworkMethod method) {
            LogInteraction classLogAnnotation = InvocationDescriptorFactoryMixedTestCase.class.getDeclaredAnnotation(LogInteraction.class);
            LogInteraction methodLogAnnotation = method.getMethod().getDeclaredAnnotation(LogInteraction.class);
            AnnotationInfo<LogInteraction> methodLog = methodLogAnnotation==null? null : new AnnotationInfo<>(methodLogAnnotation,InvocationDescriptorFactoryMixedTestCase.class);
            AnnotationInfo<LogInteraction> classLog = classLogAnnotation==null? null : new AnnotationInfo<>(classLogAnnotation,InvocationDescriptorFactoryMixedTestCase.class);
            currDescriptor = factory.create(methodLog,classLog, null, null);
        }
    };

    @After
    public void tearDown() throws Exception {
        currDescriptor=null;
    }

    @Test
    public void testOnlyClassAnnotation() throws Exception {
        assertSame(Severity.DEBUG, currDescriptor.getSeverity());
    }

    @Test
    @LogInteraction(Severity.TRACE)
    public void testMethodOverridesClassAnnotation() throws Exception {
        assertSame(Severity.TRACE, currDescriptor.getSeverity());
    }

    @Test
    @LogInteraction(value = Severity.TRACE,disabled = true)
    public void testMethodOverridesClassAnnotation_butDisabled() throws Exception {
        assertNull(currDescriptor.getSeverity());
    }



}
