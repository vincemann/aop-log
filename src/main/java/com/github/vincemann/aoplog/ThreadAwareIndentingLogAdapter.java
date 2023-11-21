package com.github.vincemann.aoplog;

import com.github.vincemann.aoplog.api.CustomLogger;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Adds padding to msg, adds ThreadId at the end of payload,
 * adds indentation depending on prior logged msg calls on same thread.
 * -> makes call stack of logged methods visible
 */
@Setter
@Slf4j
public class ThreadAwareIndentingLogAdapter extends UniversalLogAdapter {
    private static final String EMPTY_LINE = " " + System.lineSeparator();
    private final ConcurrentHashMap<Thread, Stack<Method>> thread_callStack = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Thread, Boolean> thread_openException = new ConcurrentHashMap<>();

    private String HARD_START_PADDING_CHAR = "+";
    private String HARD_END_PADDING_CHAR = "=";
    private String SOFT_PADDING_CHAR = "_";
    private int LENGTH = 122;
    private int INDENTATION_LENGTH = 15;
    private String INDENTATION_CHAR = " ";



//    @EqualsAndHashCode
//    @ToString
//    private static class MethodDescriptor{
//        private String name;
//        private Class<?>[] argTypes;
//
//        MethodDescriptor(Method method){
//            this.name=method.getName();
//            this.argTypes=method.getParameterTypes();
//        }
//    }

    public ThreadAwareIndentingLogAdapter(boolean skipNullFields, int cropThreshold, Set<String> excludeFieldNames, boolean forceReflection) {
        super(skipNullFields, cropThreshold, excludeFieldNames, forceReflection);
    }

    public ThreadAwareIndentingLogAdapter(boolean skipNullFields, Set<String> excludeFieldNames, boolean forceReflection) {
        super(skipNullFields, excludeFieldNames, forceReflection);
    }

    protected static String getThreadInfoSuffix() {
        return "  ,Thread: " + Thread.currentThread().getId() + "  ";
    }

    @Override
    public Object toMessage(Method method, String beanName, Object[] args, ArgumentDescriptor argumentDescriptor, Set<CustomLoggerInfo> customLoggerInfo) {
        String msg = (String) super.toMessage(method, beanName, args, argumentDescriptor, customLoggerInfo);
        Boolean openException = thread_openException.getOrDefault(Thread.currentThread(), Boolean.FALSE);
        if (openException) {
            thread_openException.put(Thread.currentThread(), Boolean.FALSE);
            log.trace("open exception found while opening method: " + method);
            log.trace("clearing stack ");
            //exception was never catched by logged method -> clear call stack
            getCallStack().clear();
        }
        int openMethodCalls = getCallStack().size();
        String formattedMsg = formatCall(msg, beanName, openMethodCalls);
        addToCallStack(method);
        log.trace("call stack after input logging: " + getCallStack());
        return formattedMsg;
    }

    @Override
    public Object toMessage(Method method, String beanName, int argCount, Object result, Set<CustomLoggerInfo> customLoggerInfo) {
        Boolean openException = thread_openException.getOrDefault(Thread.currentThread(), Boolean.FALSE);
        if (openException) {
            log.trace("Found open exception, but logging result so it was catched by: " + method);
        }
        thread_openException.put(Thread.currentThread(), Boolean.FALSE);
        String msg = (String) super.toMessage(method, beanName, argCount, result,customLoggerInfo);
        removeFromCallStack(method);
        int openMethodCalls = getCallStack().size();
        String formattedMsg = formatResult(msg, beanName, openMethodCalls);
        log.trace("call stack after output logging: " + getCallStack());
        return formattedMsg;
    }

    @Override
    public Object toMessage(Method method, String beanName, int argCount, Exception e, boolean stackTrace) {
        String msg = (String) super.toMessage(method, beanName, argCount, e, stackTrace);
        boolean removed = removeFromCallStack(method);
        int openMethodCalls = getCallStack().size();
        if (!removed) {
            log.trace("Found LogException only method: " + method + ", was not removed from stack bc was not on top");
            openMethodCalls++;
        } else {
            thread_openException.put(Thread.currentThread(), Boolean.TRUE);
        }
        String formattedMsg = formatResult(msg, beanName, openMethodCalls);
        log.trace("call stack after exception logging: " + getCallStack());
        return formattedMsg;
    }

    protected String formatResult(String msg, String beanName, int openMethodCalls) {
        //open method calls is already updated -> using predecessor
        String indentation = createIndentation(openMethodCalls);
        String softPadding = createPadding(SOFT_PADDING_CHAR);
        String endPadding = createPadding(HARD_END_PADDING_CHAR);
        StringBuilder sb = new StringBuilder();
        if (beanName != null) {
            sb.append(beanName).append(System.lineSeparator());
        } else {
            sb.append(EMPTY_LINE);
        }
        return sb.append(indentation).append(softPadding).append(System.lineSeparator())
                .append(indentation).append(msg).append(getThreadInfoSuffix()).append(System.lineSeparator())
                .append(indentation).append(endPadding).append(System.lineSeparator())
                .toString();
    }

    protected String formatCall(String msg, String beanName, int openMethodCalls) {
        String indentation = createIndentation(openMethodCalls);
        String softPadding = createPadding(SOFT_PADDING_CHAR);
        String startPadding = createPadding(HARD_START_PADDING_CHAR);

        StringBuilder sb = new StringBuilder();
        if (beanName != null) {
            sb.append(beanName).append(System.lineSeparator());
        } else {
            sb.append(EMPTY_LINE);
        }
        return sb.append(indentation).append(startPadding).append(System.lineSeparator())
                .append(indentation).append(msg).append(getThreadInfoSuffix()).append(System.lineSeparator())
                .append(indentation).append(softPadding).append(System.lineSeparator())
                .toString();
    }

    protected String createPadding(String paddingChar) {
        return paddingChar.repeat(LENGTH);
    }


    protected String createIndentation(int openMethodCalls) {
        return INDENTATION_CHAR.repeat((openMethodCalls) * INDENTATION_LENGTH);
    }

    private void addToCallStack(Method method) {
        Stack<Method> stack = getCallStack();
        stack.push(method);
    }

    private boolean removeFromCallStack(Method method) {
        Stack<Method> stack = getCallStack();
        if (stack.isEmpty()) {
            return false;
        }
        if (stack.peek().equals(method)) {
            stack.pop();
            return true;
        } else {
            log.trace("Method Descriptor was not on top of stack but shall be popped -> do nothing: " + method);
            return false;
        }
    }

    private Stack<Method> getCallStack() {
        thread_callStack.putIfAbsent(Thread.currentThread(), new Stack<>());
        return thread_callStack.get(Thread.currentThread());
    }


    @Override
    protected String asString(Object value, CustomLogger customLogger) {
        return super.asString(value, customLogger);
    }
}
