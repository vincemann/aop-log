/***********************************************************************************
 * Copyright (c) 2013. Nickolay Gerilovich. Russia.
 *   Some Rights Reserved.
 ************************************************************************************/

package com.github.vincemann.aoplog;

import com.github.vincemann.aoplog.service.AuxBazService;
import com.github.vincemann.aoplog.service.BazService;
import com.github.vincemann.aoplog.service.GeneralBazService;
import org.apache.commons.logging.Log;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Matchers.eq;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import static org.mockito.Matchers.eq;import static org.mockito.AdditionalMatchers.aryEq;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Tests check that two different classes of the same interface:
 * <ul>
 * <li>has independent log configurations,</li>
 * <li>a log annotation on a class does not apply for inherited methods</li>
 * </ul>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/com/github/vincemann/aoplog/AOPLoggerInheritanceAnnotatedClassTestCase-context.xml")
@DirtiesContext
public class AOPLoggerInheritanceAnnotatedClassTestCase {

    private static final String[] G_PARAM_NAMES = new String[]{"gFirst", "gSecond"};
    private static final String[] X_PARAM_NAMES = new String[]{"xFirst", "xSecond"};
    private static final Object[] PARAM_VALUE = new Object[]{"@1", "@2"};
    @Autowired
    private ProxyAwareAopLogger aspect;

    @Resource(name = "generalBaz")
    private BazService bazService;

    @Resource(name = "auxBaz")
    private BazService auxBazService;

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
    public void testGeneralBazInImpl() throws Exception {
        expectSimpleBarServiceLogger(GeneralBazService.class);
        ArgumentCaptor<ArgumentDescriptor> captured = ArgumentCaptor.forClass(ArgumentDescriptor.class);
        Mockito.when(logAdapter.toMessage(eq("inImpl"), aryEq(PARAM_VALUE), captured.capture())).thenReturn(">");
        Mockito.when(logAdapter.toMessage("inImpl", 2, Void.TYPE)).thenReturn("<");

        expectInfoLogging();

        //EasyMock.replay(logAdapter, logger);
        bazService.inImpl("@1", "@2");
        assertParams(captured.getValue(), G_PARAM_NAMES, true, true);
        //EasyMock.verify(logAdapter, logger);
    }

    @Test
    public void testGeneralBazInAbstract() throws Exception {
        //EasyMock.replay(logAdapter, logger);
        bazService.inAbstract("@1", "@2");
        //EasyMock.verify(logAdapter, logger);
    }

    @Test
    public void testAuxBazInImpl() throws Exception {
        expectSimpleBarServiceLogger(AuxBazService.class);
        ArgumentCaptor<ArgumentDescriptor> captured = ArgumentCaptor.forClass(ArgumentDescriptor.class);
        Mockito.when(logAdapter.toMessage(eq("inImpl"), aryEq(PARAM_VALUE), captured.capture())).thenReturn(">");
        Mockito.when(logAdapter.toMessage("inImpl", 2, Void.TYPE)).thenReturn("<");

        expectDebugLogging();

        //EasyMock.replay(logAdapter, logger);
        auxBazService.inImpl("@1", "@2");
        assertParams(captured.getValue(), X_PARAM_NAMES, true, true);
        //EasyMock.verify(logAdapter, logger);
    }

    @Test
    public void testAuxBazInAbstract() throws Exception {
        //EasyMock.replay(logAdapter, logger);
        auxBazService.inAbstract("@1", "@2");
        //EasyMock.verify(logAdapter, logger);
    }


    private void expectSimpleBarServiceLogger(Class<?> clazz) {
        Mockito.when(logAdapter.getLog(clazz)).thenReturn(logger);
    }

    private void expectInfoLogging() {
        Mockito.when(logger.isInfoEnabled()).thenReturn(true);
        logger.info(">");
        Mockito.when(logger.isInfoEnabled()).thenReturn(true);
        logger.info("<");
    }

    private void expectDebugLogging() {
        Mockito.when(logger.isDebugEnabled()).thenReturn(true);
        logger.debug(">");
        Mockito.when(logger.isDebugEnabled()).thenReturn(true);
        logger.debug("<");
    }

    private void assertParams(ArgumentDescriptor descriptor, String[] names, boolean first, boolean second) {
        assertArrayEquals(names, descriptor.getNames());
        assertEquals(first, descriptor.isArgumentIndexLogged(0));
        assertEquals(second, descriptor.isArgumentIndexLogged(1));
    }
}
