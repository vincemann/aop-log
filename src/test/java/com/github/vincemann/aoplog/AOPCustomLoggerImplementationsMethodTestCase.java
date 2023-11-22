/***********************************************************************************
 * Copyright (c) 2013. Nickolay Gerilovich. Russia.
 *   Some Rights Reserved.
 ************************************************************************************/

package com.github.vincemann.aoplog;

import com.github.vincemann.aoplog.service.FooService;
import com.github.vincemann.aoplog.service.SimpleFooService;
import org.junit.Before;
import org.junit.Test;

import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;

import static org.junit.Assert.*;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.inOrder;

/**
 * Tests check logging annotation parameters.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/com/github/vincemann/aoplog/AOPLoggerTestCase-context.xml")
@DirtiesContext
public class AOPCustomLoggerImplementationsMethodTestCase extends AbstractAopCustomLoggerTestCase {

    @Autowired
    private FooService fooService;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        enableLogger(SimpleFooService.class);
    }

    @Test
    public void testLogDebugMethod(){
        testLogAdapter(Severity.DEBUG,new Object[]{},Void.TYPE, "voidMethodZero", testCase -> {
            fooService.voidMethodZero();
            assertParams(testCase.getArgumentCaptor().getValue(), null);
        });
    }

    @Test
    public void testNoAnnotation() throws Exception {
        fooService.stringMethodOne("@1");
    }

    @Test
    public void testLogDebugWithSomeArgsLogged() throws Exception {
        testLogAdapter(Severity.DEBUG,new Object[]{"@1", "@2"},"stringMethodTwo:@1:@2", "stringMethodTwo", testCase -> {
            String res = fooService.stringMethodTwo("@1", "@2");
            assertEquals("stringMethodTwo:@1:@2", res);
            assertParams(testCase.argumentCaptor.getValue(), new String[]{"first", "second"}, false, true);
        });

    }

    @Test
    public void testLogDebugAllArgsLogged() throws Exception {
        testLogAdapter(Severity.DEBUG,new Object[]{"@1", "@2", "@3"},"stringMethodThree:@1:@2:@3", "stringMethodThree", testCase -> {
            String res = fooService.stringMethodThree("@1", "@2", "@3");
            assertEquals("stringMethodThree:@1:@2:@3", res);
            assertParams(testCase.getArgumentCaptor().getValue(), new String[]{"first", "second", "third"}, true, true, true);
        });
    }

    @Test
    public void testLogTraceOfSomeVarArgs() {
        String[] secondArgValue = {"@2-1", "@2-2"};
        ArgumentCaptor<ArgumentDescriptor> captured = ArgumentCaptor.forClass(ArgumentDescriptor.class);
        ArgumentCaptor<Method> inputMethod = ArgumentCaptor.forClass(Method.class);
        ArgumentCaptor<Method> outputMethod = ArgumentCaptor.forClass(Method.class);
        Mockito.when(getLogAdapter().toMessage(inputMethod.capture(), any(),refEq(new Object[]{"@1", new String[]{"@2-1", "@2-2"}}), captured.capture(),any(), any()))
                .thenReturn(">");
        Mockito.when(getLogAdapter().toMessage(outputMethod.capture(),any(), eq(2), eq("stringMethodTwoVarargs:@1:" + Arrays.toString(secondArgValue)),any(),any()))
                .thenReturn("<");
        enableLogSeverity(Severity.TRACE);
        InOrder inOrder =inOrder(getLogger());
        String res = fooService.stringMethodTwoVarargs("@1", "@2-1", "@2-2");
        assertEquals("stringMethodTwoVarargs",inputMethod.getValue().getName());
        assertEquals("stringMethodTwoVarargs",outputMethod.getValue().getName());
        verifyLogsHappened(Severity.TRACE,inOrder);
        assertEquals("stringMethodTwoVarargs:@1:" + Arrays.toString(secondArgValue), res);
        assertParams(captured.getValue(), new String[]{"first", "second"}, false, true);
    }

    @Test
    public void testLogDebugException() {
        String exceptionMsg = "<";
        ArgumentCaptor<ArgumentDescriptor> captured = ArgumentCaptor.forClass(ArgumentDescriptor.class);
        ArgumentCaptor<Method> inputMethod = ArgumentCaptor.forClass(Method.class);
        ArgumentCaptor<Method> outputMethod = ArgumentCaptor.forClass(Method.class);
        Mockito.when(getLogAdapter().toMessage(inputMethod.capture(), any(),aryEq(new Object[]{}), captured.capture(),any(), any()))
                .thenReturn(">");
        Mockito.when(getLogAdapter().toMessage(outputMethod.capture(),any(), eq(0), any(IOException.class), eq(false)))
                .thenReturn(exceptionMsg);

        Mockito.when(getLogger().isDebugEnabled()).thenReturn(true);
        Mockito.when(getLogger().isWarnEnabled()).thenReturn(true);
        try {
            fooService.voidExcMethodZero();
            fail("IOException is expected");
        } catch (IOException e) {
            assertEquals("io fail", e.getMessage());
        }
        assertEquals("voidExcMethodZero",inputMethod.getValue().getName());
        assertEquals("voidExcMethodZero",outputMethod.getValue().getName());
        InOrder inOrder = inOrder(getLogger());
        inOrder.verify(getLogger()).debug(eq(">"));
        inOrder.verify(getLogger()).warn(eq(exceptionMsg));

        assertParams(captured.getValue(), null);
    }

    

}
