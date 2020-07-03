/***********************************************************************************
 * Copyright (c) 2013. Nickolay Gerilovich. Russia.
 *   Some Rights Reserved.
 ************************************************************************************/

package com.github.vincemann.aoplog;

import com.github.vincemann.aoplog.service.AbstractBazService;
import com.github.vincemann.aoplog.service.ClassOnlyBazServiceImpl;
import com.github.vincemann.aoplog.service.BazService;
import com.github.vincemann.aoplog.service.ClassAndMethodBazServiceImpl;
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

import javax.annotation.Resource;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;

/**
 * Tests check that two different classes of the same interface:
 * <ul>
 * <li>can have independent log configurations,</li>
 * <li>can shared a common log config as well (in interface and abstract class)</li>
 * <li>can override and disable common log config</li>
 * <li>method has always precedence</li>
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
    private BazService classAndMethodBazService;

    @Resource(name = "auxBaz")
    private BazService classOnlyBazService;

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
    public void testImpl_Overrides_AbstractClassLogConfig_WithOwnClassLogConfig() throws Exception {
        //abstract class has debug class level, impl has info class level -> should pick info level
        expectServiceLoggerToBe(ClassAndMethodBazServiceImpl.class);
        ArgumentCaptor<ArgumentDescriptor> captured = ArgumentCaptor.forClass(ArgumentDescriptor.class);
        Mockito.when(logAdapter.toMessage(eq("inImpl"), aryEq(PARAM_VALUE), captured.capture())).thenReturn(">");
        Mockito.when(logAdapter.toMessage("inImpl", 2, Void.TYPE)).thenReturn("<");
        enableInfoLogging();
        classAndMethodBazService.inImpl("@1", "@2");
        verifyInfoLogging();
        assertParams(captured.getValue(), G_PARAM_NAMES, true, true);
    }

    @Test
    public void testImpl_doesNotOverrideMethod_fromAbstractClass_shouldUseAbstractClassLogConfig() throws Exception {
        //abstract class has debug class level, impl has info class level -> should pick info level
        expectServiceLoggerToBe(AbstractBazService.class);
        ArgumentCaptor<ArgumentDescriptor> captured = ArgumentCaptor.forClass(ArgumentDescriptor.class);
        Mockito.when(logAdapter.toMessage(eq("inAbstract"), aryEq(PARAM_VALUE), captured.capture())).thenReturn(">");
        Mockito.when(logAdapter.toMessage("inAbstract", 2, Void.TYPE)).thenReturn("<");
        enableDebugLogging();
        classAndMethodBazService.inAbstract("@1", "@2");
        verifyDebugLogging();
        assertParams(captured.getValue(), G_PARAM_NAMES, true, true);
    }

    @Test
    public void testAuxBazInImpl() throws Exception {
        expectServiceLoggerToBe(ClassOnlyBazServiceImpl.class);
        ArgumentCaptor<ArgumentDescriptor> captured = ArgumentCaptor.forClass(ArgumentDescriptor.class);
        Mockito.when(logAdapter.toMessage(eq("inImpl"), aryEq(PARAM_VALUE), captured.capture())).thenReturn(">");
        Mockito.when(logAdapter.toMessage("inImpl", 2, Void.TYPE)).thenReturn("<");



        //EasyMock.replay(logAdapter, logger);
        enableDebugLogging();
        classOnlyBazService.inImpl("@1", "@2");
        verifyDebugLogging();
        assertParams(captured.getValue(), X_PARAM_NAMES, true, true);
        //EasyMock.verify(logAdapter, logger);
    }

    @Test
    public void testAuxBazInAbstract() throws Exception {
        //EasyMock.replay(logAdapter, logger);
        classOnlyBazService.inAbstract("@1", "@2");
        //EasyMock.verify(logAdapter, logger);
    }


    private void expectServiceLoggerToBe(Class<?> clazz) {
        Mockito.when(logAdapter.getLog(clazz)).thenReturn(logger);
    }

    private void enableInfoLogging(){
        Mockito.when(logger.isInfoEnabled()).thenReturn(true);
    }

    private void enableDebugLogging(){
        Mockito.when(logger.isDebugEnabled()).thenReturn(true);
    }
    private void verifyInfoLogging() {
        InOrder inOrder = inOrder(logger);
        inOrder.verify(logger).info(eq(">"));
        inOrder.verify(logger).info(eq("<"));
    }

    private void verifyDebugLogging() {
        InOrder inOrder = inOrder(logger);
        inOrder.verify(logger).debug(eq(">"));
        inOrder.verify(logger).debug(eq("<"));
//
//        logger.debug(">");
//        logger.debug("<");
    }

    private void assertParams(ArgumentDescriptor descriptor, String[] names, boolean first, boolean second) {
        assertArrayEquals(names, descriptor.getNames());
        assertEquals(first, descriptor.isArgumentIndexLogged(0));
        assertEquals(second, descriptor.isArgumentIndexLogged(1));
    }
}
