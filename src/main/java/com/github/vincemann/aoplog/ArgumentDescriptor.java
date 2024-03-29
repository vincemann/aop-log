/***********************************************************************************
 * Copyright (c) 2013. Nickolay Gerilovich. Russia.
 *   Some Rights Reserved.
 ************************************************************************************/

package com.github.vincemann.aoplog;

import com.github.vincemann.aoplog.api.annotation.LogParam;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.core.ParameterNameDiscoverer;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.BitSet;

/**
 * Method arguments descriptor.
 */

final class ArgumentDescriptor {
    private final BitSet loggedValueIndexes;
    private final String[] names;

    private ArgumentDescriptor(BitSet loggedValueIndexes, String[] names) {
        this.loggedValueIndexes = loggedValueIndexes;
        this.names = names;
    }

    public int nextLoggedArgumentIndex(int i) {
        return loggedValueIndexes.nextSetBit(i);
    }

    //is argument with that index getting logged? see @Lp annotation
    public boolean isArgumentIndexLogged(int i) {
        return loggedValueIndexes.get(i);
    }

    /**
     * Gets names of method parameters.
     *
     * @return all parameter names or <code>null</code> if the method has no parameters or the names can not be discovered
     */
    public String[] getNames() {
        return names;
    }

    @Override
    public String toString() {
        return "ArgumentDescriptor{" +
                "loggedValueIndexes=" + loggedValueIndexes +
                ", names=" + Arrays.toString(names) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof ArgumentDescriptor)) return false;

        ArgumentDescriptor that = (ArgumentDescriptor) o;

        return new EqualsBuilder().append(loggedValueIndexes, that.loggedValueIndexes).append(getNames(), that.getNames()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(loggedValueIndexes).append(getNames()).toHashCode();
    }

    /**
     * Builder.
     */
    public static final class Builder {
        private static final ArgumentDescriptor NO_ARGUMENTS_DESCRIPTOR = new ArgumentDescriptor(new BitSet(0), null);
        private final Method method;
        private final int argumentCount;
        private final ParameterNameDiscoverer parameterNameDiscoverer;

        public Builder(Method method, int argumentCount, ParameterNameDiscoverer parameterNameDiscoverer) {
            this.method = method;
            this.argumentCount = argumentCount;
            this.parameterNameDiscoverer = parameterNameDiscoverer;
        }

        public ArgumentDescriptor build() {
            if (argumentCount == 0) {
                return NO_ARGUMENTS_DESCRIPTOR;
            }
            String[] argNames = parameterNameDiscoverer.getParameterNames(method);

            BitSet lpParameters = getMethodParameters(LogParam.class);
            if (lpParameters.isEmpty()) {
                lpParameters.set(0, argumentCount);
            }

            return new ArgumentDescriptor(lpParameters, argNames);
        }

        private <T> BitSet getMethodParameters(Class<T> annotationMarker) {
            Annotation[][] annotations = method.getParameterAnnotations();
            BitSet result = new BitSet(annotations.length);
            for (int i = 0; i < annotations.length; i++) {
                Annotation[] paramAnnotations = annotations[i];
                for (Annotation currAnnotation : paramAnnotations) {
                    if (currAnnotation.annotationType().equals(annotationMarker)) {
                        result.set(i);
                    }
                }
            }
            return result;
        }

    }

}
