package com.github.vincemann.aoplog.parseAnnotation;

import com.google.common.collect.Iterables;
import com.google.common.graph.SuccessorsFunction;
import com.google.common.graph.Traverser;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

//todo cache all
public class TypeHierachyAnnotationParser implements AnnotationParser {

    private static Iterable<Class<?>> getClassHierarchy(Class<?> baseClass) {
        return Traverser.forGraph(
                (SuccessorsFunction<Class<?>>) node -> {
                    Class<?> superclass = node.getSuperclass();
                    List<Class<?>> interfaces = Arrays.asList(node.getInterfaces());
                    return superclass == null ? interfaces
                            : Iterables.concat(interfaces, Collections.singleton(superclass));
                }
        ).breadthFirst(baseClass);
    }


    @Override
    public <A extends Annotation> AnnotationInfo<A> fromMethod(Method method, Class<A> annotationType) {
        A directlyPresentAnnotation = method.getDeclaredAnnotation(annotationType);
        if (directlyPresentAnnotation!=null){
            return new AnnotationInfo<>(directlyPresentAnnotation,method.getDeclaringClass());
        }
        return fromMethod(method.getDeclaringClass().getSuperclass(),method.getName(),method.getParameterTypes(),annotationType);

    }

    @Override
    public <A extends Annotation> AnnotationInfo<A> fromMethod(Class<?> clazz, String methodName, Class<?>[] argTypes, Class<A> annotationType) {
        for (Class<?> type : getClassHierarchy(clazz)) {
            try {
                Method methodInType = type.getDeclaredMethod(methodName,argTypes);
                A annotation = methodInType.getDeclaredAnnotation(annotationType);
                if (annotation!=null){
                    return new AnnotationInfo<>(annotation,type);
                }
            } catch (NoSuchMethodException e) {
                //continue;
            }
        }
        return null;
    }

    @Override
    public <A extends Annotation> AnnotationInfo<A> fromClass(Class<?> clazz, Class<A> annotationType) {
        for (Class<?> type : getClassHierarchy(clazz)) {
            A annotation = type.getDeclaredAnnotation(annotationType);
            if (annotation!=null){
                return new AnnotationInfo<>(annotation,type);
            }
        }
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
