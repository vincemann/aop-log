package com.github.vincemann.aoplog.parseAnnotation;

import com.github.vincemann.aoplog.MethodUtils;
import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.ClassUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.github.vincemann.aoplog.parseAnnotation.AnnotationInfo.IS_NULL;

//todo cache all
public class TypeHierarchyAnnotationParser implements AnnotationParser {


    @AllArgsConstructor
    @EqualsAndHashCode
    private static class MethodCacheInfo {
        private Method method;
        private Class<? extends Annotation> wantedAnnotationType;
    }

    @AllArgsConstructor
    @EqualsAndHashCode
    private static class ComplexMethodCacheInfo {
        private Class<?> clazz;
        private String methodName;
        private Class<?>[] argTypes;
        private Class<? extends Annotation> wantedAnnotationType;
    }

    @AllArgsConstructor
    @EqualsAndHashCode
    private static class ClassCacheInfo{
        private Class<?> clazz;
        private Class<? extends Annotation> wantedAnnotationType;
    }

    private ConcurrentHashMap<MethodCacheInfo,AnnotationInfo<? extends Annotation>> methodCache = new ConcurrentHashMap<>();
    private ConcurrentHashMap<ComplexMethodCacheInfo,AnnotationInfo<? extends Annotation>> complexMethodCache = new ConcurrentHashMap<>();
    private ConcurrentHashMap<ComplexMethodCacheInfo,Set<AnnotationInfo>> repeatableAnnotationComplexMethodCache = new ConcurrentHashMap<>();
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
        Set<AnnotationInfo> cached = repeatableAnnotationComplexMethodCache.get(cacheInfo);
        if (cached != null) {
            return (Set<AnnotationInfo<A>>) (Object)cached;
        }
        Set<AnnotationInfo<A>> result = new HashSet<>();
        try {
            Method methodInType = /*type.getDeclaredMethod(methodName,argTypes);*/
                    MethodUtils.findDeclaredMethod(clazz,methodName,argTypes);
            Set<A> annotations = Sets.newHashSet(List.of(methodInType.getDeclaredAnnotationsByType(annotationType)));
            if (!annotations.isEmpty()) {
                result.addAll(annotations.stream().map(a -> new AnnotationInfo<>(a, clazz)).collect(Collectors.toSet()));
            }
        } catch (NoSuchMethodException e) {
            // continue
        }

        repeatableAnnotationComplexMethodCache.put(cacheInfo,(HashSet<AnnotationInfo>)(Object) result);
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
                        MethodUtils.findDeclaredMethod(type,methodName,argTypes);
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

    //    //dont cache this
//    @Override
//    public AnnotationInfo parse(Object target, MethodSignature signature) {
//        //resolve aop and cglib proxies, jdk runtime proxies remain
//        Object unproxied = AopTestUtils.getUltimateTargetObject(target);
//        Class<?> targetClass = unproxied.getClass();
//
//
//        //proxy
//        if (isProxyClass(targetClass)){
//            return extractFromInterfaces(targetClass,signature);
//        }
//        //no proxy
//        else {
//            AnnotationInfo annotationInfo = extractFromClass(targetClass, signature);
//            if (annotationInfo !=null) {
//                return annotationInfo;
//            }else {
//                return extractFromInterfaces(targetClass, signature);
//            }
//        }
//    }
//
//
//    //todo cache this
//    private static AnnotationInfo extractFromClass(Class<?> targetClass, MethodSignature signature){
//        Method method = MethodUtils.getMatchingMethod(targetClass, signature.getName(), signature.getParameterTypes());
//
//        //also searches for Logging as meta annotation
//        Logging methodAnnotation = AnnotationUtils.findAnnotation(method, Logging.class);
//        if (methodAnnotation!=null){
//            return AnnotationInfo.builder()
//                    .annotation(methodAnnotation)
//                    .classLevel(false)
//                    .fromInterface(false)
//                    .targetClass(targetClass)
//                    .method(method)
//                    .build();
//        }
//
//        //also searches for Logging as meta annotation
//        Logging classAnnotation = AnnotationUtils.findAnnotation(targetClass, Logging.class);
//        if (classAnnotation!=null){
//            return AnnotationInfo.builder()
//                    .annotation(classAnnotation)
//                    .classLevel(true)
//                    .fromInterface(false)
//                    .targetClass(targetClass)
//                    .build();
//        }
//        return null;
//    }
//
//    //go through all interfaces in order low -> high in hierarchy and return first match's annotation
//    //method gets checked first than type (for each)
//    //todo cache this
//    private static AnnotationInfo extractFromInterfaces(Class<?> targetClass, MethodSignature signature){
//        //order is: first match will be lowest in hierachy -> first match will be latest
//        List<Class<?>> interfaces = ClassUtils.getAllInterfaces(targetClass);
//        for (Class<?> iface :interfaces){
//            Method method = MethodUtils.getMatchingMethod(iface, signature.getName(), signature.getParameterTypes());
//            if (method!=null) {
//                //interface has method
//                //method
//                Logging mehtodAnnotation = AnnotationUtils.findAnnotation(method,Logging.class);
//                if (mehtodAnnotation != null) {
//                    return AnnotationInfo.builder()
//                            .annotation(mehtodAnnotation)
//                            .classLevel(false)
//                            .fromInterface(true)
//                            .targetClass(iface)
//                            .method(method)
//                            .build();
//                }
//                //type
//                Logging classAnnotation = AnnotationUtils.findAnnotation(iface,Logging.class);
//                if (classAnnotation!=null){
//                    return AnnotationInfo.builder()
//                            .annotation(mehtodAnnotation)
//                            .classLevel(true)
//                            .fromInterface(true)
//                            .targetClass(iface)
//                            .build();
//                }
//            }
//
//        }
//        return null;
//    }
//
//    private static boolean isProxyClass(final Class<?> target) {
//        return target.getCanonicalName().contains("$Proxy");
//    }
}
