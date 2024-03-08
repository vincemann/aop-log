package com.github.vincemann.aoplog.annotation;

import com.github.vincemann.aoplog.GenericMatchMethodUtils;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.github.vincemann.aoplog.annotation.AnnotationInfo.IS_NULL;

public class TypeHierarchyAnnotationParser implements AnnotationParser {

    private ConcurrentHashMap<MethodCacheInfo,AnnotationInfo<? extends Annotation>> methodCache = new ConcurrentHashMap<>();
    private ConcurrentHashMap<ComplexMethodCacheInfo,AnnotationInfo<? extends Annotation>> complexMethodCache = new ConcurrentHashMap<>();
    private ConcurrentHashMap<ComplexMethodCacheInfo,Set<? extends AnnotationInfo>> repeatableAnnotationComplexMethodCache = new ConcurrentHashMap<>();
    private ConcurrentHashMap<ClassCacheInfo,AnnotationInfo<? extends Annotation>> classCache = new ConcurrentHashMap<>();


    @Override
    public <A extends Annotation> AnnotationInfo<A> fromMethod(Method method, Class<A> annotationType) {
        MethodCacheInfo cacheInfo = new MethodCacheInfo(method, annotationType);
        AnnotationInfo<? extends Annotation> cached = methodCache.get(cacheInfo);
        if (cached != null) {
            return nullAwareCached(cached);
        }
        A directlyPresentAnnotation = method.getDeclaredAnnotation(annotationType);
        if (directlyPresentAnnotation != null){
            AnnotationInfo<A> result = new AnnotationInfo<>(directlyPresentAnnotation, method.getDeclaringClass());
            methodCache.put(cacheInfo,result);
            return result;
        }
        Class<?> startClassNode = method.getDeclaringClass().getSuperclass()==null? method.getDeclaringClass() : method.getDeclaringClass().getSuperclass();
        AnnotationInfo<A> result = fromMethod(startClassNode, method.getName(), method.getParameterTypes(), annotationType);
        if (result == null){
            methodCache.put(cacheInfo,AnnotationInfo.NULL());
        }else {
            methodCache.put(cacheInfo,result);
        }
        return result;
    }

    @Override
    public <A extends Annotation> Set<AnnotationInfo<A>> repeatableFromDeclaredMethod(Class<?> clazz, String methodName, Class<?>[] argTypes, Class<A> annotationType) {
        ComplexMethodCacheInfo cacheInfo = new ComplexMethodCacheInfo(clazz, methodName,argTypes,annotationType);
        Set<? extends AnnotationInfo> cached = repeatableAnnotationComplexMethodCache.get(cacheInfo);
        if (cached != null) {
            return (Set<AnnotationInfo<A>>) cached;
        }
        Set<AnnotationInfo<A>> result = new HashSet<>();
        try {
            Method methodInType = /*type.getDeclaredMethod(methodName,argTypes);*/
                    GenericMatchMethodUtils.findDeclaredMethod(clazz,methodName,argTypes);
            Set<A> annotations = Sets.newHashSet(List.of(methodInType.getDeclaredAnnotationsByType(annotationType)));
            if (!annotations.isEmpty()) {
                result.addAll(annotations.stream().map(a -> new AnnotationInfo<>(a, clazz)).collect(Collectors.toSet()));
            }
        } catch (NoSuchMethodException e) {
            // continue
        }

        repeatableAnnotationComplexMethodCache.put(cacheInfo,result);
        return result;
    }

    @Override
    public <A extends Annotation> AnnotationInfo<A> fromMethod(Class<?> clazz, String methodName, Class<?>[] argTypes, Class<A> annotationType) {

        ComplexMethodCacheInfo cacheInfo = new ComplexMethodCacheInfo(clazz, methodName,argTypes,annotationType);
        AnnotationInfo<? extends Annotation> cached = complexMethodCache.get(cacheInfo);
        if (cached != null) {
            return nullAwareCached(cached);
        }

        for (Class<?> type : ClassUtils.hierarchy(clazz, ClassUtils.Interfaces.INCLUDE)) {
            try {
                Method methodInType = /*type.getDeclaredMethod(methodName,argTypes);*/
                        GenericMatchMethodUtils.findDeclaredMethod(type,methodName,argTypes);
                A annotation = methodInType.getDeclaredAnnotation(annotationType);
                if (annotation!=null){
                    AnnotationInfo<A> result = new AnnotationInfo<>(annotation, type);
                    complexMethodCache.put(cacheInfo,result);
                    return result;
                }
            } catch (NoSuchMethodException e) {
                //continue;
            }
        }
        complexMethodCache.put(cacheInfo,AnnotationInfo.NULL());
        return null;
    }




    @Override
    public <A extends Annotation> AnnotationInfo<A> fromClass(Class<?> clazz, Class<A> annotationType) {
        ClassCacheInfo cacheInfo = new ClassCacheInfo(clazz,annotationType);
        AnnotationInfo<? extends Annotation> cached = classCache.get(cacheInfo);
        if (cached != null)
            return nullAwareCached(cached);
        for (Class<?> type : ClassUtils.hierarchy(clazz, ClassUtils.Interfaces.INCLUDE)) {
            A annotation = type.getDeclaredAnnotation(annotationType);
            if (annotation!=null){
                AnnotationInfo<A> result = new AnnotationInfo<>(annotation, type);
                classCache.put(cacheInfo,result);
                return result;
            }
        }
        classCache.put(cacheInfo,AnnotationInfo.NULL());
        return null;
//        return AnnotationUtils.findAnnotation(clazz, type);
//        if (annotation==null){
//            return null;
//        }
//        //does not work in all cases
////        Class<?> annotationDeclaringClass = AnnotationUtils.findAnnotationDeclaringClass(type, clazz);
////        Assert.notNull(annotationDeclaringClass);
//        return annotation;
    }

    @Override
    public <A extends Annotation> SourceAwareAnnotationInfo<A> fromMethodOrClass(Class<?> clazz, String methodName, Class<?>[] argTypes, Class<A> type) {
        AnnotationInfo<A> fromMethod = fromMethod(clazz,methodName,argTypes, type);
        return fromMethodOrClass(fromMethod,type,clazz);
    }



    @Override
    public <A extends Annotation> SourceAwareAnnotationInfo<A> fromMethodOrClass(Method method, Class<A> type) {
        AnnotationInfo<A> fromMethod = fromMethod(method, type);
        return fromMethodOrClass(fromMethod,type,method.getDeclaringClass());
    }

    public <A extends Annotation> SourceAwareAnnotationInfo<A> fromMethodOrClass(AnnotationInfo<A> fromMethod,Class<A> type, Class<?> clazz){
        if (fromMethod == null) {
            AnnotationInfo<A> fromClass = fromClass(clazz, type);
            return fromClass == null ? null
                    : new SourceAwareAnnotationInfo<>(fromClass,true);
        } else {
            return new SourceAwareAnnotationInfo<>(fromMethod,false);
        }
    }


    private static <A extends Annotation> AnnotationInfo<A> nullAwareCached(AnnotationInfo cachedResult){
        if (IS_NULL(cachedResult)){
            return null;
        }else {
            return cachedResult;
        }
    }


    private static class MethodCacheInfo {
        private Method method;
        private Class<? extends Annotation> wantedAnnotationType;

        public MethodCacheInfo(Method method, Class<? extends Annotation> wantedAnnotationType) {
            this.method = method;
            this.wantedAnnotationType = wantedAnnotationType;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;

            if (!(o instanceof MethodCacheInfo)) return false;

            MethodCacheInfo that = (MethodCacheInfo) o;

            return new EqualsBuilder().append(method, that.method).append(wantedAnnotationType, that.wantedAnnotationType).isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37).append(method).append(wantedAnnotationType).toHashCode();
        }
    }

    private static class ComplexMethodCacheInfo {
        private Class<?> clazz;
        private String methodName;
        private Class<?>[] argTypes;
        private Class<? extends Annotation> wantedAnnotationType;

        public ComplexMethodCacheInfo(Class<?> clazz, String methodName, Class<?>[] argTypes, Class<? extends Annotation> wantedAnnotationType) {
            this.clazz = clazz;
            this.methodName = methodName;
            this.argTypes = argTypes;
            this.wantedAnnotationType = wantedAnnotationType;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;

            if (!(o instanceof ComplexMethodCacheInfo)) return false;

            ComplexMethodCacheInfo that = (ComplexMethodCacheInfo) o;

            return new EqualsBuilder().append(clazz, that.clazz).append(methodName, that.methodName).append(argTypes, that.argTypes).append(wantedAnnotationType, that.wantedAnnotationType).isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37).append(clazz).append(methodName).append(argTypes).append(wantedAnnotationType).toHashCode();
        }
    }

    private static class ClassCacheInfo{
        private Class<?> clazz;
        private Class<? extends Annotation> wantedAnnotationType;

        public ClassCacheInfo(Class<?> clazz, Class<? extends Annotation> wantedAnnotationType) {
            this.clazz = clazz;
            this.wantedAnnotationType = wantedAnnotationType;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;

            if (!(o instanceof ClassCacheInfo)) return false;

            ClassCacheInfo that = (ClassCacheInfo) o;

            return new EqualsBuilder().append(clazz, that.clazz).append(wantedAnnotationType, that.wantedAnnotationType).isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37).append(clazz).append(wantedAnnotationType).toHashCode();
        }
    }
}
