package com.github.vincemann.aoplog;

import java.lang.reflect.Method;

public class MethodUtils {

    private MethodUtils(){}

    /**
     * Supports generics
     */
    public static Method findDeclaredMethod(Class<?> clazz, String methodName, Class<?>[] argTypes) throws NoSuchMethodException {
        try {
            return clazz.getDeclaredMethod(methodName,argTypes);
        } catch (NoSuchMethodException e) {
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.getName().equals(methodName)){
                    if (method.getParameterTypes().length==argTypes.length){
                        boolean genericMatch = true;
                        for (int i = 0; i < method.getParameterTypes().length; i++) {
                            if (!method.getParameterTypes()[i].isAssignableFrom(argTypes[i])){
                                genericMatch=false;
                            }
                        }
                        if (genericMatch){
                            return method;
                        }
                    }
                }
            }
            throw e;
        }
    }
}
