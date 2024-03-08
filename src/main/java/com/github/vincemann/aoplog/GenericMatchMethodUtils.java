package com.github.vincemann.aoplog;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

/**
 * util for finding methods of class, supporting generics
 * allows for a lenient match of args -> arg types do not need exact type match, just need to be assignable from each other
 */
public class GenericMatchMethodUtils {

    private static ConcurrentHashMap<MethodIdentifier,NoSuchMethodException> notFoundCache = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<MethodIdentifier,NoSuchMethodException> declaredNotFoundCache = new ConcurrentHashMap<>();

    private static ConcurrentHashMap<MethodIdentifier,Method> declaredCache = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<MethodIdentifier,Method> cache = new ConcurrentHashMap<>();

    private GenericMatchMethodUtils(){}

    private static class MethodIdentifier{
        Class<?> clazz;
        String methodName;
        Class<?>[] argTypes;

        public MethodIdentifier(Class<?> clazz, String methodName, Class<?>[] argTypes) {
            this.clazz = clazz;
            this.methodName = methodName;
            this.argTypes = argTypes;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;

            if (!(o instanceof MethodIdentifier)) return false;

            MethodIdentifier that = (MethodIdentifier) o;

            return new EqualsBuilder().append(clazz, that.clazz).append(methodName, that.methodName).append(argTypes, that.argTypes).isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37).append(clazz).append(methodName).append(argTypes).toHashCode();
        }
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
            // look for generic match
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

        for (Class<?> classInHierarchy : ClassUtils.hierarchy(clazz, ClassUtils.Interfaces.INCLUDE)) {
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
