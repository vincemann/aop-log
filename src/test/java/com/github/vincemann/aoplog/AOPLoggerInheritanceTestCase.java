/***********************************************************************************
 * Copyright (c) 2013. Nickolay Gerilovich. Russia.
 *   Some Rights Reserved.
 ************************************************************************************/

package com.github.vincemann.aoplog;

import com.github.vincemann.aoplog.service.BarService;
import com.github.vincemann.aoplog.service.ExtendedBarService;
import org.apache.commons.logging.Log;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Matchers.eq;

///**
// * Tests check log configuration on an extended class.
// */
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(value = "/com/github/vincemann/aoplog/AOPLoggerInheritanceTestCase-context.xml")
//@DirtiesContext
//public class AOPLoggerInheritanceTestCase {
//
//    private static final String[] A_PARAM_NAMES = new String[]{"aFirst", "aSecond"};
//    private static final String[] E_PARAM_NAMES = new String[]{"eFirst", "eSecond"};
//    private static final Object[] PARAM_VALUE = new Object[]{"@1", "@2"};
//    @Autowired
//    private ProxyAwareAopLogger aspect;
//
//    @Autowired
//    private BarService barService;
//
//    private LogAdapter logAdapter;
//    private Log logger;
//
//    @Before
//    public void setUp() throws Exception {
//        logAdapter = Mockito.mock(LogAdapter.class);
//        logger = Mockito.mock(Log.class);
//        aspect.setLogAdapter(logAdapter);
//        aspect.afterPropertiesSet();
//    }
//
//    private void expectExtendedBarServiceLogger() {
//        Mockito.when(logAdapter.getLog(ExtendedBarService.class)).thenReturn(logger);
//    }
//
//    private void expectInfoLogging() {
//        Mockito.when(logger.isInfoEnabled()).thenReturn(true);
//        logger.info(">");
//        Mockito.when(logger.isInfoEnabled()).thenReturn(true);
//        logger.info("<");
//    }
//
//    private void expectDebugLogging() {
//        Mockito.when(logger.isDebugEnabled()).thenReturn(true);
//        logger.debug(">");
//        Mockito.when(logger.isDebugEnabled()).thenReturn(true);
//        logger.debug("<");
//    }
//
//    private void assertParams(ArgumentDescriptor descriptor, String[] names, boolean first, boolean second) {
//        assertArrayEquals(names, descriptor.getNames());
//        assertEquals(first, descriptor.isArgumentIndexLogged(0));
//        assertEquals(second, descriptor.isArgumentIndexLogged(1));
//    }
//
//    @Test
//    public void testInExtendedLogInSuperOnly() throws Exception {
//        //EasyMock.replay(logAdapter, logger);
//        barService.inExtendedLogInSuperOnly("@1", "@2");
//        //EasyMock.verify(logAdapter, logger);
//    }
//
//    @Test
//    public void testInAbstract() throws Exception {
//        expectExtendedBarServiceLogger();
//        ArgumentCaptor<ArgumentDescriptor> captured = ArgumentCaptor.forClass(ArgumentDescriptor.class);
//        Mockito.when(logAdapter.toMessage(eq("inAbstract"), aryEq(PARAM_VALUE), captured.capture())).thenReturn(">");
//        Mockito.when(logAdapter.toMessage("inAbstract", 2, Void.TYPE)).thenReturn("<");
//
//        expectInfoLogging();
//
//        //EasyMock.replay(logAdapter, logger);
//        barService.inAbstract("@1", "@2");
//        assertParams(captured.getValue(), A_PARAM_NAMES, true, true);
//        //EasyMock.verify(logAdapter, logger);
//    }
//
//    @Test
//    public void testInExtended() throws Exception {
//        expectExtendedBarServiceLogger();
//        ArgumentCaptor<ArgumentDescriptor> captured = ArgumentCaptor.forClass(ArgumentDescriptor.class);
//        Mockito.when(logAdapter.toMessage(eq("inExtended"), aryEq(PARAM_VALUE), captured.capture())).thenReturn(">");
//        Mockito.when(logAdapter.toMessage("inExtended", 2, Void.TYPE)).thenReturn("<");
//
//        expectDebugLogging();
//
//        //EasyMock.replay(logAdapter, logger);
//        barService.inExtended("@1", "@2");
//        assertParams(captured.getValue(), E_PARAM_NAMES, true, true);
//        //EasyMock.verify(logAdapter, logger);
//    }
//
//    @Test
//    public void testOoverridden() throws Exception {
//        expectExtendedBarServiceLogger();
//        ArgumentCaptor<ArgumentDescriptor> captured = ArgumentCaptor.forClass(ArgumentDescriptor.class);
//        Mockito.when(logAdapter.toMessage(eq("overridden"), aryEq(PARAM_VALUE), captured.capture())).thenReturn(">");
//        Mockito.when(logAdapter.toMessage("overridden", 2, Void.TYPE)).thenReturn("<");
//
//        expectDebugLogging();
//
//        //EasyMock.replay(logAdapter, logger);
//        barService.overridden("@1", "@2");
//        assertParams(captured.getValue(), E_PARAM_NAMES, false, true);
//        //EasyMock.verify(logAdapter, logger);
//    }
//
//    @Test
//    public void testOverriddenLogInAbstractOnly() throws Exception {
//        //EasyMock.replay(logAdapter, logger);
//        barService.overriddenLogInAbstractOnly("@1", "@2");
//        //EasyMock.verify(logAdapter, logger);
//    }
//}
