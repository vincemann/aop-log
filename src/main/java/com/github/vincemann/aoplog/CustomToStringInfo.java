package com.github.vincemann.aoplog;

public class CustomToStringInfo {

    // method name of method that should get called instead of toString()
    private String methodName;
    private LoggableMethodPart methodPart;

    public CustomToStringInfo(String methodName, LoggableMethodPart methodPart) {
        this.methodName = methodName;
        this.methodPart = methodPart;
    }

    public CustomToStringInfo() {
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public LoggableMethodPart getMethodPart() {
        return methodPart;
    }

    public void setMethodPart(LoggableMethodPart methodPart) {
        this.methodPart = methodPart;
    }
}
