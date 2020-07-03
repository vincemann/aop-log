/***********************************************************************************
 * Copyright (c) 2013. Nickolay Gerilovich. Russia.
 *   Some Rights Reserved.
 ************************************************************************************/

package com.github.vincemann.aoplog;

import com.github.vincemann.aoplog.api.LogAllInteractions;
import com.github.vincemann.aoplog.api.LogInteraction;
import com.github.vincemann.aoplog.api.LogException;
import org.junit.*;
import org.junit.rules.MethodRule;
import org.junit.rules.TestWatchman;
import org.junit.runners.model.FrameworkMethod;
import com.github.vincemann.aoplog.parseAnnotation.SourceAwareAnnotationInfo;
import com.github.vincemann.aoplog.parseAnnotation.AnnotationParser;
import com.github.vincemann.aoplog.parseAnnotation.TypeHierachyAnnotationParser;

import java.lang.reflect.Method;

import static org.junit.Assert.*;

/**
 * Tests {@link InvocationDescriptor} with log annotated methods.
 */
public class InvocationDescriptorTestCase {

    Method currMethod;
    InvocationDescriptor currDescriptor;
    AnnotationParser annotationParser = new TypeHierachyAnnotationParser();


    @Rule
    public MethodRule watchman = new TestWatchman() {
        public void starting(FrameworkMethod method) {
            currMethod = method.getMethod();
            LogInteraction methodLog = annotationParser.fromMethod(currMethod, LogInteraction.class);
            LogAllInteractions classLog = annotationParser.fromClass(getClass(), LogAllInteractions.class);
            SourceAwareAnnotationInfo<LogException> logException = annotationParser.fromMethodOrClass(currMethod, LogException.class);
            currDescriptor = new InvocationDescriptor.Builder(methodLog,classLog,logException).build();
        }
    };

    @After
    public void tearDown() throws Exception {
        currMethod = null;
//        loggingAnnotationInfo = null;
//        logExceptionAnnotationInfo = null;
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
        assertNotNull(currDescriptor.getExceptionAnnotation());
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
