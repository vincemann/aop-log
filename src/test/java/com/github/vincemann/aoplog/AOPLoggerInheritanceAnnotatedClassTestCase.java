/***********************************************************************************
 * Copyright (c) 2013. Nickolay Gerilovich. Russia.
 *   Some Rights Reserved.
 ************************************************************************************/

package com.github.vincemann.aoplog;

import com.github.vincemann.aoplog.service.BazService;
import com.github.vincemann.aoplog.service.ClassAndMethodBazServiceImpl;
import com.github.vincemann.aoplog.service.DisabledBazService;
import com.github.vincemann.aoplog.service.MethodOnlyBazServiceImpl;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

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
public class AOPLoggerInheritanceAnnotatedClassTestCase extends AbstractAopLoggerTestCase{

    //method only impl
    private static final String[] M_PARAM_NAMES = new String[]{"mFirst", "mSecond"};
    //class and method impl
    private static final String[] CM_PARAM_NAMES = new String[]{"cmFirst", "cmSecond"};
    //abstract impl
    private static final String[] A_PARAM_NAMES = new String[]{"aFirst", "aSecond"};
    //interface
    private static final String[] I_PARAM_NAMES = new String[]{"iFirst", "iSecond"};

    private static final String[] D_PARAM_NAMES = new String[]{"dFirst", "dSecond"};

    @Resource(name = "generalBaz")
    private BazService methodOnlyBazService;

    @Resource(name = "auxBaz")
    private BazService classAndMethodBazService;

    @Resource(name = "disabledBaz")
    private BazService disabledBazService;

    @Test
    public void testImpl_OverridesMethod_fromAbstractClass_withClassLog_shouldUseImplClassLog(){
        //abstract class has debug class level, impl has info class level -> should pick info level
        enableLogger(ClassAndMethodBazServiceImpl.class);
        testLogAdapter(Severity.INFO, "inImpl", testCase -> {
            classAndMethodBazService.inImpl("@1", "@2");
            assertParams(testCase.getArgumentCaptor().getValue(), CM_PARAM_NAMES, true, true);
        });
    }

    @Test
    public void testImpl_doesNotOverrideMethod_fromAbstractClass_shouldStillUseImplClassLog() throws Exception {
        //abstract class has debug class level, impl has info class level -> should pick info level
        enableLogger(ClassAndMethodBazServiceImpl.class);
        testLogAdapter(Severity.INFO, "inAbstract", testCase -> {
            classAndMethodBazService.inAbstract("@1", "@2");
            assertParams(testCase.getArgumentCaptor().getValue(), A_PARAM_NAMES, true, true);
        });
//        ArgumentCaptor<ArgumentDescriptor> captured = ArgumentCaptor.forClass(ArgumentDescriptor.class);
//        ArgumentCaptor<Method> firstMethod = ArgumentCaptor.forClass(Method.class);
//        ArgumentCaptor<Method> secondMethod = ArgumentCaptor.forClass(Method.class);
//        Mockito.when(logAdapter.toMessage(firstMethod.capture(), aryEq(PARAM_VALUE), captured.capture())).thenReturn(">");
//        Mockito.when(logAdapter.toMessage(secondMethod.capture(), 2, Void.TYPE)).thenReturn("<");
//        enableInfoLogging();
//        classAndMethodBazService.inAbstract("@1", "@2");
//        assertEquals("inAbstract",firstMethod.getValue().getName());
//        assertEquals("inAbstract",secondMethod.getValue().getName());
//        verifyInfoLogging();
//        assertParams(testCase.getArgumentCaptor().getValue(), A_PARAM_NAMES, true, true);
    }

    @Test
    public void testImpl_inheritsClassLog_fromAbstractClass(){
        enableLogger(MethodOnlyBazServiceImpl.class);
        testLogAdapter(Severity.DEBUG, "inImpl", testCase -> {
            methodOnlyBazService.inImpl("@1", "@2");
            assertParams(testCase.getArgumentCaptor().getValue(), M_PARAM_NAMES, true, true);
        });
//        ArgumentCaptor<ArgumentDescriptor> captured = ArgumentCaptor.forClass(ArgumentDescriptor.class);
//        ArgumentCaptor<Method> firstMethod = ArgumentCaptor.forClass(Method.class);
//        ArgumentCaptor<Method> secondMethod = ArgumentCaptor.forClass(Method.class);
//
//        Mockito.when(logAdapter.toMessage(eq("inImpl"), aryEq(PARAM_VALUE), captured.capture())).thenReturn(">");
//        Mockito.when(logAdapter.toMessage("inImpl", 2, Void.TYPE)).thenReturn("<");
//        enableDebugLogging();
//        methodOnlyBazService.inImpl("@1", "@2");
//        assertEquals("inImpl",firstMethod.getValue().getName());
//        assertEquals("inImpl",secondMethod.getValue().getName());
//        verifyDebugLogging();
//        assertParams(testCase.getArgumentCaptor().getValue(), M_PARAM_NAMES, true, true);
    }

    @Test
    public void testImpl_inheritsClassConfig_fromAbstractClass(){
        //setters are disabled in abstract logConfig -> impl inherits -> setter wont be logged
        enableLogger(MethodOnlyBazServiceImpl.class);
        methodOnlyBazService.setInImpl("@1", "@2");
    }

    @Test
    public void testImpl_overridesClassConfig_fromAbstractClass_withClassConfig(){
        //setters are disabled in abstract logConfig -> impl overrides with config that allows setter
        enableLogger(ClassAndMethodBazServiceImpl.class);
        testLogAdapter(Severity.INFO, "setInImpl", testCase -> {
            classAndMethodBazService.setInImpl("@1", "@2");
            assertParams(testCase.getArgumentCaptor().getValue(), CM_PARAM_NAMES, true, true);
        });
//        ArgumentCaptor<ArgumentDescriptor> captured = ArgumentCaptor.forClass(ArgumentDescriptor.class);
//        ArgumentCaptor<Method> firstMethod = ArgumentCaptor.forClass(Method.class);
//        ArgumentCaptor<Method> secondMethod = ArgumentCaptor.forClass(Method.class);
//        Mockito.when(logAdapter.toMessage(eq("setInImpl"), aryEq(PARAM_VALUE), captured.capture())).thenReturn(">");
//        Mockito.when(logAdapter.toMessage("setInImpl", 2, Void.TYPE)).thenReturn("<");
//        enableInfoLogging();
//        classAndMethodBazService.setInImpl("@1", "@2");
//        assertEquals("setInImpl",firstMethod.getValue().getName());
//        assertEquals("setInImpl",secondMethod.getValue().getName());
//        verifyInfoLogging();
//        assertParams(testCase.getArgumentCaptor().getValue(), CM_PARAM_NAMES, true, true);
    }

    @Test
    public void testImpl_overridesClassLog_fromAbstractClass_withMethodLog(){
        //annotation from method in impl has precedence over inherited class annotation
        enableLogger(MethodOnlyBazServiceImpl.class);
        testLogAdapter(Severity.INFO, "getInImpl", testCase -> {
            methodOnlyBazService.getInImpl("@1", "@2");
            assertParams(testCase.getArgumentCaptor().getValue(), M_PARAM_NAMES, true, true);
        });


//        ArgumentCaptor<ArgumentDescriptor> captured = ArgumentCaptor.forClass(ArgumentDescriptor.class);
//        ArgumentCaptor<Method> firstMethod = ArgumentCaptor.forClass(Method.class);
//        ArgumentCaptor<Method> secondMethod = ArgumentCaptor.forClass(Method.class);
//        Mockito.when(logAdapter.toMessage(eq("getInImpl"), aryEq(PARAM_VALUE), captured.capture())).thenReturn(">");
//        Mockito.when(logAdapter.toMessage("getInImpl", 2, Void.TYPE)).thenReturn("<");
//        enableInfoLogging();
//        methodOnlyBazService.getInImpl("@1", "@2");
//        assertEquals("getInImpl",firstMethod.getValue().getName());
//        assertEquals("getInImpl",secondMethod.getValue().getName());
//        verifyInfoLogging();
//        assertParams(testCase.getArgumentCaptor().getValue(), M_PARAM_NAMES, true, true);
    }

    @Test
    public void testImpl_interfaceMethodLog_overridesImplClassLog(){
        enableLogger(ClassAndMethodBazServiceImpl.class);
        testLogAdapter(Severity.INFO, "inInterface", testCase -> {
            classAndMethodBazService.inInterface("@1", "@2");
            assertParams(testCase.getArgumentCaptor().getValue(), I_PARAM_NAMES, true, true);
        });


//        ArgumentCaptor<ArgumentDescriptor> captured = ArgumentCaptor.forClass(ArgumentDescriptor.class);
//        ArgumentCaptor<Method> firstMethod = ArgumentCaptor.forClass(Method.class);
//        ArgumentCaptor<Method> secondMethod = ArgumentCaptor.forClass(Method.class);
//        Mockito.when(logAdapter.toMessage(eq("inInterface"), aryEq(PARAM_VALUE), captured.capture())).thenReturn(">");
//        Mockito.when(logAdapter.toMessage("inInterface", 2, Void.TYPE)).thenReturn("<");
//        enableTraceLogging();
//        classAndMethodBazService.inInterface("@1", "@2");
//        assertEquals("inInterface",firstMethod.getValue().getName());
//        assertEquals("inInterface",secondMethod.getValue().getName());
//        verifyTraceLogging();
//        assertParams(testCase.getArgumentCaptor().getValue(), I_PARAM_NAMES, true, true);
    }

    @Test
    public void testImpl_methodLog_overridesInterfaceMethodLog(){
        enableLogger(MethodOnlyBazServiceImpl.class);
        testLogAdapter(Severity.INFO, "inInterface", testCase -> {
            methodOnlyBazService.inInterface("@1", "@2");
            assertParams(testCase.getArgumentCaptor().getValue(), M_PARAM_NAMES, true, true);
        });

//        ArgumentCaptor<ArgumentDescriptor> captured = ArgumentCaptor.forClass(ArgumentDescriptor.class);
//        ArgumentCaptor<Method> firstMethod = ArgumentCaptor.forClass(Method.class);
//        ArgumentCaptor<Method> secondMethod = ArgumentCaptor.forClass(Method.class);
//        Mockito.when(logAdapter.toMessage(eq("inInterface"), aryEq(PARAM_VALUE), captured.capture())).thenReturn(">");
//        Mockito.when(logAdapter.toMessage("inInterface", 2, Void.TYPE)).thenReturn("<");
//        enableInfoLogging();
//        methodOnlyBazService.inInterface("@1", "@2");
//        assertEquals("inInterface",firstMethod.getValue().getName());
//        assertEquals("inInterface",secondMethod.getValue().getName());
//        verifyInfoLogging();
//        assertParams(testCase.getArgumentCaptor().getValue(), M_PARAM_NAMES, true, true);
    }

    //disable tests
    @Test
    public void testImpl_overridesAbstractClassLog_withDisabledClassLog(){
        enableLogger(DisabledBazService.class);
        disabledBazService.inImpl("@1", "@2");
    }

    @Test
    public void testImpl_overridesAbstractClassLog_withDisabledClassLog_butInterfaceMethodLogMethod_stillLogged(){
        enableLogger(DisabledBazService.class);
        testLogAdapter(Severity.TRACE, "inInterface", testCase -> {
            disabledBazService.inInterface("@1", "@2");
            assertParams(testCase.getArgumentCaptor().getValue(), D_PARAM_NAMES, true, true);
        });

//        ArgumentCaptor<ArgumentDescriptor> captured = ArgumentCaptor.forClass(ArgumentDescriptor.class);
//        ArgumentCaptor<Method> firstMethod = ArgumentCaptor.forClass(Method.class);
//        ArgumentCaptor<Method> secondMethod = ArgumentCaptor.forClass(Method.class);
//        Mockito.when(logAdapter.toMessage(eq("inInterface"), aryEq(PARAM_VALUE), captured.capture())).thenReturn(">");
//        Mockito.when(logAdapter.toMessage("inInterface", 2, Void.TYPE)).thenReturn("<");
//        enableTraceLogging();
//        disabledBazService.inInterface("@1", "@2");
//        assertEquals("inInterface",firstMethod.getValue().getName());
//        assertEquals("inInterface",secondMethod.getValue().getName());
//        verifyTraceLogging();
//        assertParams(testCase.getArgumentCaptor().getValue(), D_PARAM_NAMES, true, true);
    }

    @Test
    public void testImpl_hasMethodLog_onMethodFilteredOutMethod_shouldStillBeLogged(){
        //setter from DisabledBazService is ignored via class level disabled AND method filter, but still gets called bc explicitly annotated
        enableLogger(DisabledBazService.class);
        testLogAdapter(Severity.INFO, "setInImpl", testCase -> {
            disabledBazService.inInterface("@1", "@2");
            assertParams(testCase.getArgumentCaptor().getValue(), D_PARAM_NAMES, true, true);
        });

//        ArgumentCaptor<ArgumentDescriptor> captured = ArgumentCaptor.forClass(ArgumentDescriptor.class);
//        ArgumentCaptor<Method> firstMethod = ArgumentCaptor.forClass(Method.class);
//        ArgumentCaptor<Method> secondMethod = ArgumentCaptor.forClass(Method.class);
//        Mockito.when(logAdapter.toMessage(eq("setInImpl"), aryEq(PARAM_VALUE), captured.capture())).thenReturn(">");
//        Mockito.when(logAdapter.toMessage("setInImpl", 2, Void.TYPE)).thenReturn("<");
//        enableInfoLogging();
//        disabledBazService.setInImpl("@1", "@2");
//        assertEquals("setInImpl",firstMethod.getValue().getName());
//        assertEquals("setInImpl",secondMethod.getValue().getName());
//        verifyInfoLogging();
//        assertParams(testCase.getArgumentCaptor().getValue(), D_PARAM_NAMES, true, true);
    }



//    @Test
//    public void testAuxBazInImpl() throws Exception {
//        expectServiceLoggerToBe(ClassAndMethodBazServiceImpl.class);
//        ArgumentCaptor<ArgumentDescriptor> captured = ArgumentCaptor.forClass(ArgumentDescriptor.class);
//        Mockito.when(logAdapter.toMessage(eq("inImpl"), aryEq(PARAM_VALUE), captured.capture())).thenReturn(">");
//        Mockito.when(logAdapter.toMessage("inImpl", 2, Void.TYPE)).thenReturn("<");
//
//
//
//        //EasyMock.replay(logAdapter, logger);
//        enableDebugLogging();
//        classAndMethodBazService.inImpl("@1", "@2");
//        verifyDebugLogging();
//        assertParams(testCase.getArgumentCaptor().getValue(), X_PARAM_NAMES, true, true);
//        //EasyMock.verify(logAdapter, logger);
//    }
//
//    @Test
//    public void testAuxBazInAbstract() throws Exception {
//        //EasyMock.replay(logAdapter, logger);
//        classAndMethodBazService.inAbstract("@1", "@2");
//        //EasyMock.verify(logAdapter, logger);
//    }


//    private void enableInfoLogging(){
//        Mockito.when(logger.isInfoEnabled()).thenReturn(true);
//    }
//
//    private void enableDebugLogging(){
//        Mockito.when(logger.isDebugEnabled()).thenReturn(true);
//    }
//    private void verifyInfoLogging() {
//        InOrder inOrder = inOrder(logger);
//        inOrder.verify(logger).info(eq(">"));
//        inOrder.verify(logger).info(eq("<"));
//    }
//
//    private void verifyDebugLogging() {
//        InOrder inOrder = inOrder(logger);
//        inOrder.verify(logger).debug(eq(">"));
//        inOrder.verify(logger).debug(eq("<"));
////
////        logger.debug(">");
////        logger.debug("<");
//    }
//
//    private void enableTraceLogging(){
//        Mockito.when(logger.isTraceEnabled()).thenReturn(true);
//    }
//    private void verifyTraceLogging() {
//        InOrder inOrder = inOrder(logger);
//        inOrder.verify(logger).trace(eq(">"));
//        inOrder.verify(logger).trace(eq("<"));
//    }


    private void assertParams(ArgumentDescriptor descriptor, String[] names, boolean first, boolean second) {
        assertArrayEquals(names, descriptor.getNames());
        assertEquals(first, descriptor.isArgumentIndexLogged(0));
        assertEquals(second, descriptor.isArgumentIndexLogged(1));
    }
}
