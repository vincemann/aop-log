package com.github.vincemann.aoplog;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

public class MethodUtils {

    private static ConcurrentHashMap<MethodIdentifier,NoSuchMethodException> notFoundCache = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<MethodIdentifier,NoSuchMethodException> declaredNotFoundCache = new ConcurrentHashMap<>();

    private static ConcurrentHashMap<MethodIdentifier,Method> declaredCache = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<MethodIdentifier,Method> cache = new ConcurrentHashMap<>();

    private MethodUtils(){}

    @EqualsAndHashCode
    @AllArgsConstructor
    private static class MethodIdentifier{
        Class<?> clazz;
        String methodName;
        Class<?>[] argTypes;

    }
    /**
     * Supports generics
     */
    public static Method findDeclaredMethod(Class<?> clazz, String methodName, Class<?>[] argTypes) throws NoSuchMethodException {
        MethodIdentifier methodIdentifier = new MethodIdentifier(clazz,methodName,argTypes);
        NoSuchMethodException exception = declaredNotFoundCache.get(methodIdentifier);
        if (exception!=null){
            throw exception;
        }
        Method cached = declaredCache.get(methodIdentifier);
        if (cached!=null){
            return cached;
        }
        try {
            Method result =  clazz.getDeclaredMethod(methodName,argTypes);
            declaredCache.put(methodIdentifier,result);
            return result;
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
                            declaredCache.put(methodIdentifier,method);
                            return method;
                        }
                    }
                }
            }
            declaredNotFoundCache.put(methodIdentifier,e);
            throw e;
        }
    }

    /**
     * Supports generics
     */
    public static Method findMethod(Class<?> clazz, String methodName, Class<?>[] argTypes) throws NoSuchMethodException{
        MethodIdentifier methodIdentifier = new MethodIdentifier(clazz,methodName,argTypes);
        NoSuchMethodException exception = notFoundCache.get(methodIdentifier);
        if (exception!=null){
            throw exception;
        }
        Method cached = cache.get(methodIdentifier);
        if (cached!=null){
            return cached;
        }

        for (Class<?> classInHierarchy : ClassUtils.getClassHierarchy(clazz)) {
            try {
                Method result = findDeclaredMethod(classInHierarchy, methodName, argTypes);
                declaredCache.put(methodIdentifier,result);
                return result;
            }catch (NoSuchMethodException e){
                // keep it like that
            }
        }
        // normal, when method not found. Exception is handled like return value
        NoSuchMethodException ex = new NoSuchMethodException("No Method found: " + methodName + ", " + Arrays.toString(argTypes) + " in hierarchy of: " + clazz);
        notFoundCache.put(methodIdentifier,ex);
        throw ex;
    }

    public static void clearCache(){
        notFoundCache.clear();
        declaredNotFoundCache.clear();
        cache.clear();
        declaredCache.clear();
    }

}
