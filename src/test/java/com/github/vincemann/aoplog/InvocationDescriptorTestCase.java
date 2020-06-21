/***********************************************************************************
 * Copyright (c) 2013. Nickolay Gerilovich. Russia.
 *   Some Rights Reserved.
 ************************************************************************************/

package com.github.vincemann.aoplog;

import com.github.vincemann.aoplog.annotation.Log;
import com.github.vincemann.aoplog.annotation.LogException;
import com.github.vincemann.aoplog.annotation.*;
import org.junit.*;
import org.junit.rules.MethodRule;
import org.junit.rules.TestWatchman;
import org.junit.runners.model.FrameworkMethod;

import java.lang.reflect.Method;

import static org.junit.Assert.*;

/**
 * Tests {@link InvocationDescriptor} with log annotated methods.
 */
public class InvocationDescriptorTestCase {

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
//        assertNull(descriptor.getBeforeSeverity());
        assertNull(descriptor.getSeverity());
        assertNull(descriptor.getExceptionAnnotation());
    }

    @Test
    @Log(/*logPoint=LogPoint.IN*/)
    public void testGetBeforeSeverity() throws Exception {
        InvocationDescriptor descriptor = new InvocationDescriptor.Builder(loggingAnnotationInfo,logExceptionAnnotationInfo).build();
        assertSame(Severity.DEBUG, descriptor.getSeverity());
//        assertNull(descriptor.getSeverity());
        assertNull(descriptor.getExceptionAnnotation());
    }

//    @Test
//    @Log(logPoint = LogPoint.IN,level = Severity.INFO)
//   @Log(/*logPoint=LogPoint.IN*/)
//    public void testGetBeforeSeverityByPriority() throws Exception {
//        InvocationDescriptor descriptor = new InvocationDescriptor.Builder(loggingAnnotationInfo,logExceptionAnnotationInfo).build();
//        assertSame(Severity.INFO, descriptor.getBeforeSeverity());
//        assertNull(descriptor.getAfterSeverity());
//        assertNull(descriptor.getExceptionAnnotation());
//    }

    @Test
    @Log(/*logPoint=LogPoint.OUT*/)
    public void testGetAfterSeverity() throws Exception {
        InvocationDescriptor descriptor = new InvocationDescriptor.Builder(loggingAnnotationInfo,logExceptionAnnotationInfo).build();
        assertSame(Severity.DEBUG, descriptor.getSeverity());
//        assertNull(descriptor.getBeforeSeverity());
        assertNull(descriptor.getExceptionAnnotation());
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
    @Log
    public void testGetSeverity() throws Exception {
        InvocationDescriptor descriptor = new InvocationDescriptor.Builder(loggingAnnotationInfo,logExceptionAnnotationInfo).build();
//        assertSame(Severity.DEBUG, descriptor.getBeforeSeverity());
        assertSame(Severity.DEBUG, descriptor.getSeverity());
        assertNull(descriptor.getExceptionAnnotation());
    }

//    @Test
//    @LogWarn
//    @LogInfo(logPoint=LogPoint.OUT)
//   @Log(/*logPoint=LogPoint.IN*/)
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
        InvocationDescriptor descriptor = new InvocationDescriptor.Builder(loggingAnnotationInfo,logExceptionAnnotationInfo).build();
//        assertNull(descriptor.getBeforeSeverity());
        assertNull(descriptor.getSeverity());
        assertNotNull(descriptor.getExceptionAnnotation());
    }

    @Test
    @Log(Severity.INFO)
    @LogException
    public void testGetAll() throws Exception {
        InvocationDescriptor descriptor = new InvocationDescriptor.Builder(loggingAnnotationInfo,logExceptionAnnotationInfo).build();
//        assertSame(Severity.INFO, descriptor.getBeforeSeverity());
        assertSame(Severity.INFO, descriptor.getSeverity());
        assertNotNull(descriptor.getExceptionAnnotation());
    }
}
