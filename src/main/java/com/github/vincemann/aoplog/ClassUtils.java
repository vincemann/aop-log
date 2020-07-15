package com.github.vincemann.aoplog;

import com.google.common.collect.Iterables;
import com.google.common.graph.SuccessorsFunction;
import com.google.common.graph.Traverser;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ClassUtils {

    private ClassUtils(){}

    public static Iterable<Class<?>> getClassHierarchy(Class<?> baseClass) {
        return Traverser.forGraph(
                (SuccessorsFunction<Class<?>>) node -> {
                    Class<?> superclass = node.getSuperclass();
                    List<Class<?>> interfaces = Arrays.asList(node.getInterfaces());
                    return superclass == null ? interfaces
                            : Iterables.concat(interfaces, Collections.singleton(superclass));
                }
        ).breadthFirst(baseClass);
    }
}
