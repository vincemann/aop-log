/***********************************************************************************
 * Copyright (c) 2013. Nickolay Gerilovich. Russia.
 *   Some Rights Reserved.
 ************************************************************************************/

package com.github.vincemann.aoplog;

import org.springframework.util.ReflectionUtils;

import javax.persistence.EntityManager;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Universal log adapter, capable to out parameter values by reflection.
 */
public abstract class UniversalLogAdapter extends AbstractLogAdapter {
    private final Set<String> excludeFieldNames;
    private final int cropThreshold;
    private final boolean skipNullFields;
    private final boolean forceReflection;

    /**
     * Constructor.
     *
     * @param skipNullFields use {@code true} to exclude fields which value is {@code null} from building the string
     * @param cropThreshold threshold value of processed elements count to stop building the string, applied only for multi-element structures
     * @param excludeFieldNames field names to exclude from building the string
     * @param forceReflection always use reflections to build string, even if toString is available
     * @throws IllegalArgumentException if <code>cropThreshold </code> is negative
     */
    public UniversalLogAdapter(boolean skipNullFields, int cropThreshold, Set<String> excludeFieldNames, boolean forceReflection) {
        this.skipNullFields = skipNullFields;
        this.forceReflection = forceReflection;
        if (cropThreshold < 0) {
            throw new IllegalArgumentException("cropThreshold is negative: " + cropThreshold);
        }
        this.cropThreshold = cropThreshold;
        this.excludeFieldNames = excludeFieldNames == null ? null : new HashSet<String>(excludeFieldNames);
    }

    /**
     * Constructor.
     * @param skipNullFields use {@code true} to exclude fields which value is {@code null} from building the string
     * @param excludeFieldNames field names to exclude from building the string
     * @param forceReflection always use reflections to build string, even if toString is available
     */
    public UniversalLogAdapter(boolean skipNullFields, Set<String> excludeFieldNames, boolean forceReflection) {
        this.skipNullFields = skipNullFields;
        this.forceReflection = forceReflection;
        this.cropThreshold = -1;
        this.excludeFieldNames = excludeFieldNames == null ? null : new HashSet<String>(excludeFieldNames);
    }


    @Override
    protected String asString(Object value) {
        if (value == null) {
            return ToString.getNull();
        }
        Class<?> clazz = value.getClass();
        if (!(value instanceof Collection<?> || value instanceof Map<?, ?>)
                && ToStringDetector.INSTANCE.hasToString(clazz)
                && !forceReflection) {
            return value.toString();
        }
        ToString builder = cropThreshold == -1 ? ToString.createDefault() : ToString.createCropInstance(cropThreshold);
        builder.addStart(value);

        if (value instanceof Collection<?>) {
            builder.addCollection((Collection<?>) value);
        } else if (value instanceof Map<?, ?>) {
            builder.addMap((Map<?, ?>) value);
        } else if (clazz.isArray()) {
            builder.addArray(value);
        } else {
            appendFieldsIn(builder, value, clazz);
        }
        builder.addEnd(value);
        return builder.toString();
    }

    private boolean reject(Field field) {
        return field.getName().indexOf('$') != -1 // Reject field from inner class.
                || Modifier.isTransient(field.getModifiers()) || Modifier.isStatic(field.getModifiers())
                || excludeFieldNames != null && excludeFieldNames.contains(field.getName());
    }

    private void appendFieldsIn(ToString builder, Object object, Class<?> clazz) {
        ReflectionUtils.doWithFields(clazz,field -> {
            ReflectionUtils.makeAccessible(field);
            if (!reject(field)) {
                String fieldName = field.getName();
                Object fieldValue;
                try {
                    // memo: it creates wrapper objects for primitive types.
                    fieldValue = field.get(object);
                } catch (IllegalAccessException ex) {
                    throw new IllegalStateException("Unexpected IllegalAccessException: " + ex.getMessage());
                }
                if (!skipNullFields || fieldValue != null) {
                    builder.addField(fieldName, fieldValue);
                }
            }
        });
    }

}
