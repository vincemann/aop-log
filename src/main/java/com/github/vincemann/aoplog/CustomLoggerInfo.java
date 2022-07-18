package com.github.vincemann.aoplog;

import com.github.vincemann.aoplog.api.CustomLogger;

public class CustomLoggerInfo {


    static enum Type{
        ARG,
        RET
    }

    private CustomLogger logger;
    private Type type;
    private Integer argNum;
}
