package com.github.vincemann.aoplog;

import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

//todo cache all
public class HierarchicalAnnotationParser implements AnnotationParser {


    @Override
    public <A extends Annotation> A fromMethod(Method method, Class<A> type) {
        return AnnotationUtils.findAnnotation(method, type);
    }

    @Override
    public <A extends Annotation> A fromClass(Class<?> clazz, Class<A> type) {
        return AnnotationUtils.findAnnotation(clazz, type);
//        if (annotation==null){
//            return null;
//        }
//        //does not work in all cases
////        Class<?> annotationDeclaringClass = AnnotationUtils.findAnnotationDeclaringClass(type, clazz);
////        Assert.notNull(annotationDeclaringClass);
//        return annotation;
    }

    @Override
    public <A extends Annotation> AnnotationInfo<A> fromMethodOrClass(Method method, Class<A> type) {
        A fromMethod = fromMethod(method, type);
        if (fromMethod == null) {
            A fromClass = fromClass(method.getDeclaringClass(), type);
            return fromClass == null ? null
                    : AnnotationInfo.<A>builder()
                        .classLevel(true)
                        .annotation(fromClass)
                        .build();
        } else {
            return AnnotationInfo.<A>builder()
                    .annotation(fromMethod)
                    .classLevel(false)
                    .build();
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
