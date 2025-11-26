package me.emmajiugo.javalidator.util;

import me.emmajiugo.javalidator.Validator;
import me.emmajiugo.javalidator.config.ValidationConfig;

import java.lang.reflect.Field;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.ReflectPermission;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for reflection operations used in validation.
 *
 * <p>Provides helper methods for accessing fields and record components
 * from DTOs, supporting both Java records and traditional POJOs.
 *
 * <p><b>Security:</b> This class respects ValidationConfig settings:
 * <ul>
 *   <li>maxClassHierarchyDepth - Limits traversal depth to prevent memory exhaustion
 *   <li>strictReflectionMode - Checks SecurityManager permissions before setAccessible()
 * </ul>
 */
public final class ReflectionUtils {

    private ReflectionUtils() {
        // Private constructor to prevent instantiation
    }

    /**
     * Checks SecurityManager permissions if strictReflectionMode is enabled.
     * Called before setAccessible(true) operations.
     *
     * @throws SecurityException if permission is denied
     */
    private static void checkReflectionPermission() {
        ValidationConfig config = Validator.getConfig();
        if (config.isStrictReflectionMode()) {
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                sm.checkPermission(new ReflectPermission("suppressAccessChecks"));
            }
        }
    }

    /**
     * Validates a field name against the configured pattern.
     * Used by conditional validation rules to prevent field name injection attacks.
     *
     * @param fieldName the field name to validate
     * @return true if validation passes or is disabled, false if field name is invalid
     */
    public static boolean isValidFieldName(String fieldName) {
        if (fieldName == null || fieldName.isEmpty()) {
            return false;
        }

        ValidationConfig config = Validator.getConfig();
        if (!config.isValidateFieldNames()) {
            return true; // Validation disabled
        }

        String pattern = config.getFieldNamePattern();
        return fieldName.matches(pattern);
    }

    /**
     * Gets the value of a field from a DTO by field name.
     * Works with both Java records and traditional classes.
     *
     * @param dto the DTO object
     * @param fieldName the name of the field to retrieve
     * @return the field value, or null if field not found or not accessible
     */
    public static Object getFieldValueByName(Object dto, String fieldName) {
        if (dto == null || fieldName == null) {
            return null;
        }

        Class<?> clazz = dto.getClass();

        try {
            // Try record component first
            if (clazz.isRecord()) {
                for (RecordComponent component : clazz.getRecordComponents()) {
                    if (component.getName().equals(fieldName)) {
                        var accessor = component.getAccessor();
                        checkReflectionPermission();
                        accessor.setAccessible(true);
                        return accessor.invoke(dto);
                    }
                }
            } else {
                // Try regular field
                Field field = findField(clazz, fieldName);
                if (field != null) {
                    checkReflectionPermission();
                    field.setAccessible(true);
                    return field.get(dto);
                }
            }
        } catch (Exception e) {
            // Field not found or not accessible
            return null;
        }

        return null;
    }

    /**
     * Gets value from a record component.
     *
     * @param dto the record object
     * @param component the record component
     * @return the component value
     * @throws RuntimeException if the component cannot be accessed
     */
    public static Object getRecordComponentValue(Object dto, RecordComponent component) {
        try {
            var accessor = component.getAccessor();
            checkReflectionPermission();
            accessor.setAccessible(true);
            return accessor.invoke(dto);
        } catch (Exception e) {
            throw new RuntimeException("Failed to access record component: " + component.getName(), e);
        }
    }

    /**
     * Gets value from a class field.
     *
     * @param dto the object containing the field
     * @param field the field to read
     * @return the field value
     * @throws RuntimeException if the field cannot be accessed
     */
    public static Object getFieldValue(Object dto, Field field) {
        try {
            return field.get(dto);
        } catch (Exception e) {
            throw new RuntimeException("Failed to access field: " + field.getName(), e);
        }
    }

    /**
     * Finds a field by name, searching through the class hierarchy.
     * Respects maxClassHierarchyDepth from ValidationConfig to prevent unbounded traversal.
     *
     * @param clazz the class to search
     * @param fieldName the name of the field
     * @return the field, or null if not found
     */
    public static Field findField(Class<?> clazz, String fieldName) {
        ValidationConfig config = Validator.getConfig();
        int depth = 0;

        while (clazz != null && depth < config.getMaxClassHierarchyDepth()) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
                depth++;
            }
        }
        return null;
    }

    /**
     * Gets all fields from a class including inherited fields.
     * Respects maxClassHierarchyDepth from ValidationConfig to prevent memory exhaustion attacks.
     *
     * @param clazz the class to get fields from
     * @return list of all fields including inherited ones (up to configured depth limit)
     */
    public static List<Field> getAllFields(Class<?> clazz) {
        ValidationConfig config = Validator.getConfig();
        List<Field> fields = new ArrayList<>();
        int depth = 0;

        while (clazz != null && clazz != Object.class && depth < config.getMaxClassHierarchyDepth()) {
            fields.addAll(List.of(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
            depth++;
        }
        return fields;
    }
}
