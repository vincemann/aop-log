/***********************************************************************************
 * Copyright (c) 2013. Nickolay Gerilovich. Russia.
 *   Some Rights Reserved.
 ************************************************************************************/

package com.github.vincemann.aoplog;

import com.github.vincemann.aoplog.service.FooService;
import com.github.vincemann.aoplog.service.SimpleFooService;
import org.apache.commons.logging.Log;
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
import java.util.Arrays;

//import static com.github.vincemann.aoplog.TestSupportUtility.arrayEqual;
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
public class AOPLoggerImplementationsMethodAnnotatedTestCase {

    @Autowired
    private ProxyAwareAopLogger aspect;

    @Autowired
    private FooService fooService;

    private LogAdapter logAdapter;
    private Log logger;

    @Before
    public void setUp() throws Exception {
        logAdapter = Mockito.mock(LogAdapter.class);
        logger = Mockito.mock(Log.class);
        aspect.setLogAdapter(logAdapter);
        aspect.afterPropertiesSet();
    }

    @Test
    public void testLogDebugMethod(){
        stubSimpleFooServiceLogger();
        ArgumentCaptor<ArgumentDescriptor> captured = ArgumentCaptor.forClass(ArgumentDescriptor.class);
        String invocationMsg = ">";
        String resultMsg = "<";
        Mockito.when(logAdapter.toMessage(eq("voidMethodZero"), aryEq(new Object[]{}), captured.capture()))
                .thenReturn(invocationMsg);
        Mockito.when(logAdapter.toMessage(eq("voidMethodZero"), eq(0), eq(Void.TYPE)))
                .thenReturn(resultMsg);

        Mockito.when(logger.isDebugEnabled()).thenReturn(true);
        InOrder inOrder = inOrder(logger);
        fooService.voidMethodZero();

        inOrder.verify(logger).debug(eq(invocationMsg));
        inOrder.verify(logger).debug(eq(resultMsg));
        assertParams(captured.getValue(), null);
    }

    @Test
    public void testNoAnnotation() throws Exception {
        fooService.stringMethodOne("@1");
    }

    @Test
    public void testLogDebugWithSomeArgsLogged() throws Exception {
        String invocationMsg = ">";
        String resultMsg = "<";

        stubSimpleFooServiceLogger();
        ArgumentCaptor<ArgumentDescriptor> captured = ArgumentCaptor.forClass(ArgumentDescriptor.class);
        Mockito.when(logAdapter.toMessage(eq("stringMethodTwo"), aryEq(new Object[]{"@1", "@2"}), captured.capture()))
                .thenReturn(invocationMsg);
        Mockito.when(logAdapter.toMessage(eq("stringMethodTwo"), eq(2), eq("stringMethodTwo:@1:@2")))
                .thenReturn(resultMsg);
        Mockito.when(logger.isDebugEnabled()).thenReturn(true);
//        logger.debug(">");
        Mockito.when(logger.isDebugEnabled()).thenReturn(true);
//        logger.debug("<");
        //EasyMock.replay(logAdapter, logger);
        String res = fooService.stringMethodTwo("@1", "@2");

        InOrder inOrder = inOrder(logger);
        inOrder.verify(logger).debug(eq(invocationMsg));
        inOrder.verify(logger).debug(eq(resultMsg));

        assertEquals("stringMethodTwo:@1:@2", res);
        assertParams(captured.getValue(), new String[]{"first", "second"}, false, true);
        //EasyMock.verify(logAdapter, logger);
    }

    @Test
    public void testLogDebugAllArgsLogged() throws Exception {
        String invocationMsg = ">";
        String resultMsg = "<";

        stubSimpleFooServiceLogger();
        ArgumentCaptor<ArgumentDescriptor> captured = ArgumentCaptor.forClass(ArgumentDescriptor.class);
        Mockito.when(logAdapter.toMessage(eq("stringMethodThree"), aryEq(new Object[]{"@1", "@2", "@3"}), captured.capture())).thenReturn(invocationMsg);
        Mockito.when(logAdapter.toMessage(eq("stringMethodThree"), eq(3), eq("stringMethodThree:@1:@2:@3"))).thenReturn(resultMsg);
        Mockito.when(logger.isDebugEnabled()).thenReturn(true);
//        logger.debug(invocationMsg);
        Mockito.when(logger.isDebugEnabled()).thenReturn(true);
//        logger.debug(resultMsg);
        //EasyMock.replay(logAdapter, logger);
        String res = fooService.stringMethodThree("@1", "@2", "@3");
        InOrder inOrder = inOrder(logger);
        inOrder.verify(logger).debug(eq(invocationMsg));
        inOrder.verify(logger).debug(eq(resultMsg));
        assertEquals("stringMethodThree:@1:@2:@3", res);
        assertParams(captured.getValue(), new String[]{"first", "second", "third"}, true, true, true);
        //EasyMock.verify(logAdapter, logger);
    }

    @Test
    public void testLogTraceOfSomeVarArgs() throws Exception {
        String invocationMsg = ">";
        String resultMsg = "<";

        stubSimpleFooServiceLogger();
        String[] secondArgValue = {"@2-1", "@2-2"};
        ArgumentCaptor<ArgumentDescriptor> captured = ArgumentCaptor.forClass(ArgumentDescriptor.class);
        Mockito.when(logAdapter.toMessage(eq("stringMethodTwoVarargs"), refEq(new Object[]{"@1", new String[]{"@2-1", "@2-2"}}), captured.capture()))
                .thenReturn(invocationMsg);
        Mockito.when(logAdapter.toMessage(eq("stringMethodTwoVarargs"), eq(2), eq("stringMethodTwoVarargs:@1:" + Arrays.toString(secondArgValue))))
                .thenReturn(resultMsg);
        Mockito.when(logger.isTraceEnabled()).thenReturn(true);
//        logger.trace(invocationMsg);
        Mockito.when(logger.isTraceEnabled()).thenReturn(true);
//        logger.trace(resultMsg);
        //EasyMock.replay(logAdapter, logger);
        String res = fooService.stringMethodTwoVarargs("@1", "@2-1", "@2-2");
        InOrder inOrder = inOrder(logger);
        inOrder.verify(logger).trace(eq(invocationMsg));
        inOrder.verify(logger).trace(eq(resultMsg));

        assertEquals("stringMethodTwoVarargs:@1:" + Arrays.toString(secondArgValue), res);
        assertParams(captured.getValue(), new String[]{"first", "second"}, false, true);
        //EasyMock.verify(logAdapter, logger);
    }

    @Test
    public void testLogDebugException() throws Exception {
        String invocationMsg = ">";
        String exceptionMsg = "<";
        stubSimpleFooServiceLogger();
        ArgumentCaptor<ArgumentDescriptor> captured = ArgumentCaptor.forClass(ArgumentDescriptor.class);
        Mockito.when(logAdapter.toMessage(eq("voidExcMethodZero"), aryEq(new Object[]{}), captured.capture()))
                .thenReturn(invocationMsg);
        Mockito.when(logAdapter.toMessage(eq("voidExcMethodZero"), eq(0), any(IOException.class), eq(false)))
                .thenReturn(exceptionMsg);

        Mockito.when(logger.isDebugEnabled()).thenReturn(true);
//        logger.debug(invocationMsg);
        Mockito.when(logger.isWarnEnabled()).thenReturn(true);
//        logger.warn("io thrown");
        //EasyMock.replay(logAdapter, logger);
        try {
            fooService.voidExcMethodZero();
            fail("IOException is expected");
        } catch (IOException e) {
            assertEquals("io fail", e.getMessage());
        }
        InOrder inOrder = inOrder(logger);
        inOrder.verify(logger).debug(eq(invocationMsg));
        inOrder.verify(logger).warn(eq(exceptionMsg));

        assertParams(captured.getValue(), null);
        //EasyMock.verify(logAdapter, logger);
    }

    private void stubSimpleFooServiceLogger() {
        Mockito.when(logAdapter.getLog(SimpleFooService.class)).thenReturn(logger);
    }

    private void assertParams(ArgumentDescriptor descriptor, String[] names, boolean... indexes) {
        assertArrayEquals(names, descriptor.getNames());
        for (int i = 0; i < indexes.length; i++) {
            assertEquals(indexes[i], descriptor.isArgumentIndexLogged(i));

        }
        assertEquals(-1, descriptor.nextLoggedArgumentIndex(indexes.length));
    }

}
