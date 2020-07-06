package com.github.vincemann.aoplog;

import lombok.EqualsAndHashCode;
import lombok.Setter;
import org.springframework.util.Assert;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Adds padding to msg, adds ThreadId at the end of payload,
 * adds indentation depending on prior logged msg calls on same thread.
 * -> makes call stack of logged methods visible
 */
@Setter
public class ThreadAwareIndentingLogAdapter extends UniversalLogAdapter {
    private static final String EMPTY_LINE = " "+System.lineSeparator();
    private final Map<Thread, Integer> thread_amountOpenMethodCalls = new HashMap<>();
    private final Map<Thread,MethodDescriptor> thread_openMethod = new HashMap<>();
    private String HARD_START_PADDING_CHAR = "+";
    private String HARD_END_PADDING_CHAR = "=";
    private String SOFT_PADDING_CHAR = "_";
    private int LENGTH = 122;
    private int INDENTATION_LENGTH = 32;
    private String INDENTATION_CHAR = " ";

    @EqualsAndHashCode
    private static class MethodDescriptor{
        private String name;
        private Class<?>[] argTypes;

        MethodDescriptor(Method method){
            this.name=method.getName();
            this.argTypes=method.getParameterTypes();
        }
    }

    public ThreadAwareIndentingLogAdapter(boolean skipNullFields, int cropThreshold, Set<String> excludeFieldNames, boolean forceReflection) {
        super(skipNullFields, cropThreshold, excludeFieldNames, forceReflection);
    }

    public ThreadAwareIndentingLogAdapter(boolean skipNullFields, Set<String> excludeFieldNames, boolean forceReflection) {
        super(skipNullFields, excludeFieldNames, forceReflection);
    }

    @Override
    public Object toMessage(Method method, Object[] args, ArgumentDescriptor argumentDescriptor) {
        String msg = (String) super.toMessage(method,args,argumentDescriptor);
        int openMethodCalls = incrementOpenMethodCalls();
        thread_openMethod.put(Thread.currentThread(),new MethodDescriptor(method));
        return formatCall(msg,openMethodCalls);
    }

    @Override
    public Object toMessage(Method method, int argCount, Object result) {
        String msg = (String) super.toMessage(method, argCount, result);
        int openMethodCalls = decrementOpenMethodCalls();
        return formatResult(msg,openMethodCalls);
    }

    @Override
    public Object toMessage(Method method, int argCount, Exception e, boolean stackTrace) {
        String msg = (String) super.toMessage(method, argCount, e, stackTrace);
        int openMethodCalls;
        if (thread_openMethod.get(Thread.currentThread()).equals(new MethodDescriptor(method))){
            openMethodCalls = decrementOpenMethodCalls();
        }else {
            openMethodCalls = 0;
        }
        return formatResult(msg,openMethodCalls);
    }

    protected String formatResult(String msg, int openMethodCalls){
        //open method calls is already updated -> using predecessor
        String indentation = createIdentation(openMethodCalls+1);
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
        return INDENTATION_CHAR.repeat((openMethodCalls - 1) * INDENTATION_LENGTH);
    }

    private int incrementOpenMethodCalls() {
        int openMethodCalls;
        Integer currentCalls = thread_amountOpenMethodCalls.get(Thread.currentThread());
        if (currentCalls == null) {
            thread_amountOpenMethodCalls.put(Thread.currentThread(), 1);
        } else {
            int incremented = currentCalls + 1;
            thread_amountOpenMethodCalls.put(Thread.currentThread(), incremented);
        }
        openMethodCalls = thread_amountOpenMethodCalls.get(Thread.currentThread());
        return openMethodCalls;
    }

    private int decrementOpenMethodCalls() {
        int openMethodCalls;
        Integer currentCalls = thread_amountOpenMethodCalls.get(Thread.currentThread());
        Assert.notNull(currentCalls);
        int decremented = currentCalls - 1;
        //update
        thread_amountOpenMethodCalls.put(Thread.currentThread(), decremented);
        //get updated
        openMethodCalls = thread_amountOpenMethodCalls.get(Thread.currentThread());
        return openMethodCalls;
    }


}
