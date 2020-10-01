package com.github.vincemann.aoplog;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.logging.Log;
import org.junit.Before;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Method;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;

@Getter
public abstract class AbstractAopLoggerTestCase {

    private static final Object[] PARAM_VALUE = new Object[]{"@1", "@2"};

    @Autowired
    private ProxyAwareAopLogger aspect;

    private LogAdapter logAdapter;
    private Log logger;

    @AllArgsConstructor
    @Getter
    static class TestCase{
        ArgumentCaptor<Method> inputMethodCaptor;
        ArgumentCaptor<Method> outputMethodCaptor;
        ArgumentCaptor<ArgumentDescriptor> argumentCaptor;
    }


    @Before
    public void setUp() throws Exception {
        logAdapter = Mockito.mock(LogAdapter.class);
        logger = Mockito.mock(Log.class);
        aspect.setLogAdapter(logAdapter);
        aspect.afterPropertiesSet();
    }

    protected interface TestRunnable{
        public void run(TestCase testCase);
    }

    protected void testLogAdapter(Severity level, String methodName,TestRunnable test){
        testLogAdapter(level,PARAM_VALUE,Void.TYPE,methodName,test);
    }

    
    protected void testLogAdapter(Severity level, Object[] args, Object result, String methodName, TestRunnable test){
        enableLogSeverity(level);
        ArgumentCaptor<ArgumentDescriptor> capturedArgDescriptor = ArgumentCaptor.forClass(ArgumentDescriptor.class);
        ArgumentCaptor<Method> inputMethod = ArgumentCaptor.forClass(Method.class);
        ArgumentCaptor<Method> outputMethod = ArgumentCaptor.forClass(Method.class);
        Mockito.when(logAdapter.toMessage(inputMethod.capture(),aryEq(args), capturedArgDescriptor.capture()))
                .thenReturn(">");
        Mockito.when(logAdapter.toMessage(outputMethod.capture(), eq(args.length), eq(result)))
                .thenReturn("<");
        InOrder inOrder = inOrder(logger);
        test.run(new TestCase(inputMethod,outputMethod,capturedArgDescriptor));
        verifyLogsHappened(level,inOrder);
        assertEquals(methodName,inputMethod.getValue().getName());
        assertEquals(methodName,outputMethod.getValue().getName());
    }

    protected void testLogAdapterShouldLogNothing(Runnable testRunnable){
        enableAllLogger();
        enableAllLogSeverities();
        alwaysLog();
        testRunnable.run();
        verifyNoLogsHappened();
    }

    protected void alwaysLog(){
        Mockito.when(logAdapter.toMessage(any(Method.class),anyObject(), any(ArgumentDescriptor.class)))
                .thenReturn(">");
        Mockito.when(logAdapter.toMessage(any(Method.class), anyInt(), anyObject()))
                .thenReturn("<");
    }
    protected void enableAllLogger() {
        Mockito.when(logAdapter.getLog(any(Class.class)))
                .thenReturn(logger);
    }


    protected void enableAllLogSeverities(){
        Mockito.when(logger.isDebugEnabled()).thenReturn(true);
        Mockito.when(logger.isInfoEnabled()).thenReturn(true);
        Mockito.when(logger.isWarnEnabled()).thenReturn(true);
        Mockito.when(logger.isErrorEnabled()).thenReturn(true);
        Mockito.when(logger.isTraceEnabled()).thenReturn(true);
        Mockito.when(logger.isFatalEnabled()).thenReturn(true);
    }

    protected void verifyNoLogsHappened(){
        Mockito.verify(logger,never()).fatal(anyString());
        Mockito.verify(logger,never()).debug(anyString());
        Mockito.verify(logger,never()).info(anyString());
        Mockito.verify(logger,never()).warn(anyString());
        Mockito.verify(logger,never()).trace(anyString());
        Mockito.verify(logger,never()).error(anyString());
    }



    protected void enableLogger(Class<?> clazz) {
        Mockito.when(logAdapter.getLog(clazz)).thenReturn(logger);
    }




    protected void verifyLogsHappened(Severity level, InOrder inOrder){

        switch (level){
            case DEBUG:
                inOrder.verify(logger).debug(eq(">"));
                inOrder.verify(logger).debug(eq("<"));
                break;
            case INFO:
                inOrder.verify(logger).info(eq(">"));
                inOrder.verify(logger).info(eq("<"));
                break;
            case WARN:
                inOrder.verify(logger).warn(eq(">"));
                inOrder.verify(logger).warn(eq("<"));
                break;
            case ERROR:
                inOrder.verify(logger).error(eq(">"));
                inOrder.verify(logger).error(eq("<"));
                break;
            case TRACE:
                inOrder.verify(logger).trace(eq(">"));
                inOrder.verify(logger).trace(eq("<"));
                break;
            case FATAL:
                inOrder.verify(logger).fatal(eq(">"));
                inOrder.verify(logger).fatal(eq("<"));
                break;
        }
    }



    protected void enableLogSeverity(Severity level){
        switch (level){
            case DEBUG:
                Mockito.when(logger.isDebugEnabled()).thenReturn(true);
                break;
            case INFO:
                Mockito.when(logger.isInfoEnabled()).thenReturn(true);
                break;
            case WARN:
                Mockito.when(logger.isWarnEnabled()).thenReturn(true);
                break;
            case ERROR:
                Mockito.when(logger.isErrorEnabled()).thenReturn(true);
                break;
            case TRACE:
                Mockito.when(logger.isTraceEnabled()).thenReturn(true);
                break;
            case FATAL:
                Mockito.when(logger.isFatalEnabled()).thenReturn(true);
                break;
        }

    }

    protected void assertParams(ArgumentDescriptor descriptor, String[] names, boolean... indexes) {
        assertArrayEquals(names, descriptor.getNames());
        for (int i = 0; i < indexes.length; i++) {
            assertEquals(indexes[i], descriptor.isArgumentIndexLogged(i));

        }
        assertEquals(-1, descriptor.nextLoggedArgumentIndex(indexes.length));
    }


}
