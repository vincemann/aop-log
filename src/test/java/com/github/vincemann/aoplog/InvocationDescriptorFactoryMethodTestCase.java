/***********************************************************************************
 * Copyright (c) 2013. Nickolay Gerilovich. Russia.
 *   Some Rights Reserved.
 ************************************************************************************/

package com.github.vincemann.aoplog;

import com.github.vincemann.aoplog.api.LogException;
import com.github.vincemann.aoplog.api.LogInteraction;
import com.github.vincemann.aoplog.parseAnnotation.AnnotationInfo;
import com.github.vincemann.aoplog.parseAnnotation.AnnotationParser;
import com.github.vincemann.aoplog.parseAnnotation.SourceAwareAnnotationInfo;
import com.github.vincemann.aoplog.parseAnnotation.TypeHierarchyAnnotationParser;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.junit.rules.TestWatchman;
import org.junit.runners.model.FrameworkMethod;

import static org.junit.Assert.*;

/**
 * Tests {@link InvocationDescriptor} with log annotated methods.
 */
public class InvocationDescriptorFactoryMethodTestCase {

    InvocationDescriptor currDescriptor;
    AnnotationParser annotationParser = new TypeHierarchyAnnotationParser();
    InvocationDescriptorFactory factory = new InvocationDescriptorFactoryImpl();

    @Rule
    public MethodRule watchman = new TestWatchman() {
        public void starting(FrameworkMethod method) {
            AnnotationInfo<LogInteraction> methodLog = annotationParser.fromMethod(method.getMethod(), LogInteraction.class);
            AnnotationInfo<LogInteraction> classLog = annotationParser.fromClass(getClass(), LogInteraction.class);
            SourceAwareAnnotationInfo<LogException> logException = annotationParser.fromMethodOrClass(method.getMethod(), LogException.class);
            currDescriptor = factory.create(methodLog,classLog,logException);
        }
    };

    @After
    public void tearDown() throws Exception {
        currDescriptor=null;
    }

    @Test
    public void testNoAnnotations() throws Exception {
//        assertNull(descriptor.getBeforeSeverity());
        assertNull(currDescriptor.getSeverity());
        assertNull(currDescriptor.getExceptionAnnotation());
    }

    @Test
    @LogInteraction()
    public void testGetBeforeSeverity() throws Exception {
        assertSame(Severity.DEBUG, currDescriptor.getSeverity());
//        assertNull(descriptor.getSeverity());
        assertNull(currDescriptor.getExceptionAnnotation());
    }

//    @Test
//    @Log(logPoint = LogPoint.IN,level = Severity.INFO)
//   @Log()
//    public void testGetBeforeSeverityByPriority() throws Exception {
//        InvocationDescriptor descriptor = new InvocationDescriptor.Builder(loggingAnnotationInfo,logExceptionAnnotationInfo).build();
//        assertSame(Severity.INFO, descriptor.getBeforeSeverity());
//        assertNull(descriptor.getAfterSeverity());
//        assertNull(descriptor.getExceptionAnnotation());
//    }

    @Test
    @LogInteraction(/*logPoint=LogPoint.OUT*/)
    public void testGetAfterSeverity() throws Exception {
        assertSame(Severity.DEBUG, currDescriptor.getSeverity());
//        assertNull(descriptor.getBeforeSeverity());
        assertNull(currDescriptor.getExceptionAnnotation());
    }

//    @Test
//    @Log(level = Severity.INFO,logPoint=LogPoint.OUT)
//    @Log(/*logPoint=LogPoint.OUT*/)
//    public void testGetAfterSeverityByPriority() throws Exception {
//        InvocationDescriptor descriptor = new InvocationDescriptor.Builder(loggingAnnotationInfo,logExceptionAnnotationInfo).build();
//        assertSame(Severity.INFO, descriptor.getAfterSeverity());
//        assertNull(descriptor.getBeforeSeverity());
//        assertNull(descriptor.getExceptionAnnotation());
//    }

    @Test
    @LogInteraction
    public void testGetSeverity() throws Exception {
//        assertSame(Severity.DEBUG, descriptor.getBeforeSeverity());
        assertSame(Severity.DEBUG, currDescriptor.getSeverity());
        assertNull(currDescriptor.getExceptionAnnotation());
    }

//    @Test
//    @LogWarn
//    @LogInfo(logPoint=LogPoint.OUT)
//   @Log()
//    @LogTrace
//    public void testGetSeverityByPriority() throws Exception {
//        InvocationDescriptor descriptor = new InvocationDescriptor.Builder(loggingAnnotationInfo,logExceptionAnnotationInfo).build();
//        assertSame(Severity.DEBUG, descriptor.getBeforeSeverity());
//        assertSame(Severity.INFO, descriptor.getAfterSeverity());
//        assertNull(descriptor.getExceptionAnnotation());
//    }

    @Test
    @LogException
    public void testGetExceptionAnnotation() throws Exception {
//        assertNull(descriptor.getBeforeSeverity());
        assertNull(currDescriptor.getSeverity());
    }

    @Test
    @LogInteraction(Severity.INFO)
    @LogException
    public void testGetAll() throws Exception {
//        assertSame(Severity.INFO, descriptor.getBeforeSeverity());
        assertSame(Severity.INFO, currDescriptor.getSeverity());
        assertNotNull(currDescriptor.getExceptionAnnotation());
    }
}
