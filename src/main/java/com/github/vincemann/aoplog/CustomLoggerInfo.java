package com.github.vincemann.aoplog;

import com.github.vincemann.aoplog.api.CustomLogger;

public class CustomLoggerInfo {

    private CustomLogger logger;
    private LoggableMethodPart methodPart;

    public CustomLoggerInfo(CustomLogger logger, LoggableMethodPart methodPart) {
        this.logger = logger;
        this.methodPart = methodPart;
    }

    public CustomLoggerInfo() {
    }

    public CustomLogger getLogger() {
        return logger;
    }

    public void setLogger(CustomLogger logger) {
        this.logger = logger;
    }

    public LoggableMethodPart getMethodPart() {
        return methodPart;
    }

    public void setMethodPart(LoggableMethodPart methodPart) {
        this.methodPart = methodPart;
    }
}
