package com.github.vincemann.aoplog;

import lombok.Setter;

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
public class ThreadAwareIndentingLogAdapter extends UniversalLogAdapter {
    private static final String EMPTY_LINE = " "+System.lineSeparator();
    private final ConcurrentHashMap<Thread, Stack<Method>> thread_callStack = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Thread,Boolean> thread_openException = new ConcurrentHashMap<>();
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

    @Override
    public Object toMessage(Method method, Object[] args, ArgumentDescriptor argumentDescriptor) {
        String msg = (String) super.toMessage(method,args,argumentDescriptor);
        Boolean openException = thread_openException.getOrDefault(Thread.currentThread(), Boolean.FALSE);
        if (openException){
            thread_openException.put(Thread.currentThread(),Boolean.FALSE);
//            System.err.println("open exception found while opening method: " + method);
//            System.err.println("clearing stack ");
            //exception was never catched by logged method -> clear call stack
            getStack().clear();
        }
        int openMethodCalls = getStack().size();
        String formattedMsg = formatCall(msg, openMethodCalls);
        addToCallStack(method);
//        System.err.println("call stack after input logging: " + getStack());
        return formattedMsg;
    }

    @Override
    public Object toMessage(Method method, int argCount, Object result) {
//        Boolean openException = thread_openException.getOrDefault(Thread.currentThread(), Boolean.FALSE);
//        if (openException){
//            System.err.println("Found open exception, but logging result so it was catched by: " + method );
//        }
        thread_openException.put(Thread.currentThread(),Boolean.FALSE);
        String msg = (String) super.toMessage(method, argCount, result);
        removeFromCallStack(method);
        int openMethodCalls = getStack().size();
        String formattedMsg = formatResult(msg, openMethodCalls);
//        System.err.println("call stack after output logging: " + getStack());
        return formattedMsg;
    }

    @Override
    public Object toMessage(Method method, int argCount, Exception e, boolean stackTrace) {
        String msg = (String) super.toMessage(method, argCount, e, stackTrace);
        boolean removed = removeFromCallStack(method);
        int openMethodCalls = getStack().size();
        if (!removed){
//            System.err.println("Found LogException only method: " + method + ", was not removed from stack bc was not on top");
            openMethodCalls++;
        }else {
            thread_openException.put(Thread.currentThread(),Boolean.TRUE);
        }
        String formattedMsg = formatResult(msg, openMethodCalls);
//        System.err.println("call stack after exception logging: " + getStack());
        return formattedMsg;
    }

    protected String formatResult(String msg, int openMethodCalls){
        //open method calls is already updated -> using predecessor
        String indentation = createIdentation(openMethodCalls);
        String softPadding = createPadding(SOFT_PADDING_CHAR);
        String endPadding = createPadding(HARD_END_PADDING_CHAR);
        return new StringBuilder()
                .append(EMPTY_LINE)
                .append(indentation).append(softPadding).append(System.lineSeparator())
                .append(indentation).append(msg).append(getThreadInfoSuffix()).append(System.lineSeparator())
                .append(indentation).append(endPadding).append(System.lineSeparator())
                .toString();
    }

    protected String formatCall(String msg, int openMethodCalls){
        String indentation = createIdentation(openMethodCalls);
        String softPadding = createPadding(SOFT_PADDING_CHAR);
        String startPadding = createPadding(HARD_START_PADDING_CHAR);

        return new StringBuilder()
                .append(EMPTY_LINE)
                .append(indentation).append(startPadding).append(System.lineSeparator())
                .append(indentation).append(msg).append(getThreadInfoSuffix()).append(System.lineSeparator())
                .append(indentation).append(softPadding).append(System.lineSeparator())
                .toString();
    }


    protected static String getThreadInfoSuffix() {
        return "  ,Thread: " + Thread.currentThread().getId() + "  ";
    }

    protected String createPadding(String paddingChar) {
        return paddingChar.repeat(LENGTH);
    }


    protected String createIdentation(int openMethodCalls){
        return INDENTATION_CHAR.repeat((openMethodCalls) * INDENTATION_LENGTH);
    }

    private void addToCallStack(Method method) {
        Stack<Method> stack = getStack();
        stack.push(method);
    }

    private boolean removeFromCallStack(Method method) {
        Stack<Method> stack = getStack();
        if (stack.peek().equals(method)){
            stack.pop();
            return true;
        }else {
//            System.err.println("Method Descriptor was not on top of stack but shall be popped -> do nothing: " + method);
            return false;
        }
    }

    private Stack<Method> getStack(){
        thread_callStack.putIfAbsent(Thread.currentThread(),new Stack<>());
        return thread_callStack.get(Thread.currentThread());
    }



}
