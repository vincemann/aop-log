/***********************************************************************************
 * Copyright (c) 2013. Nickolay Gerilovich. Russia.
 *   Some Rights Reserved.
 ************************************************************************************/

package com.github.vincemann.aoplog;

import com.github.vincemann.aoplog.api.LogException;
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
 * Tests {@link InvocationDescriptor} with log annotated methods and class.
 */
@LogInteraction
public class InvocationDescriptorFactoryClassTestCase {

    InvocationDescriptor currDescriptor;
    InvocationDescriptorFactory factory = new InvocationDescriptorFactoryImpl();

    @Rule
    public MethodRule watchman = new TestWatchman() {
        public void starting(FrameworkMethod method) {
            AnnotationInfo<LogInteraction> methodLog = new AnnotationInfo<>(method.getMethod().getDeclaredAnnotation(LogInteraction.class),this.getClass());
            AnnotationInfo<LogInteraction> classLog = new AnnotationInfo<>(this.getClass().getDeclaredAnnotation(LogInteraction.class),this.getClass());
            currDescriptor = factory.create(methodLog,classLog);
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
    public void testGetSeverityByMethodPriority() throws Exception {
        assertSame(Severity.TRACE, currDescriptor.getSeverity());
    }



}
