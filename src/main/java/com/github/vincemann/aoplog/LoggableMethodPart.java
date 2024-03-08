package com.github.vincemann.aoplog;

public class LoggableMethodPart {

    enum Type {
        ARG,
        RET;
    }

    private Type type;
    private Integer argNum;


    public static LoggableMethodPart from(String s){
        LoggableMethodPart result = new LoggableMethodPart();
        if (s.startsWith("arg")) {
            result.type = Type.ARG;
            try {
                result.argNum = Integer.parseInt(s.substring(3));
            } catch (NumberFormatException | IndexOutOfBoundsException e) {
                throw new IllegalArgumentException("invalid arg input string. Example: arg2");
            }
        } else if (s.equals("ret")) {
            result.type = Type.RET;
        } else {
            throw new IllegalArgumentException("invalid input string. No Type found. Available type: arg | ret");
        }
        return result;
    }

    public Type getType() {
        return type;
    }

    public Integer getArgNum() {
        return argNum;
    }
}
