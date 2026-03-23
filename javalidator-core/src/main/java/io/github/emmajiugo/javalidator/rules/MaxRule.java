package io.github.emmajiugo.javalidator.rules;

import io.github.emmajiugo.javalidator.ValidationRule;

/**
 * Validation rule that checks if a string does not exceed a maximum length, or a collection/array does not have more than a maximum number of items.
 *
 * <p>Usage: {@code @Rule("max:20")}
 */
public class MaxRule implements ValidationRule {

    @Override
    public String validate(String fieldName, Object value, String parameter) {
        if (value == null) return null;

        if (parameter == null || parameter.isEmpty()) {
            throw new IllegalArgumentException("Max rule requires a parameter (e.g., 'max:20')");
        }

        int max = Integer.parseInt(parameter);
        if (value instanceof String s) {
            if (s.length() > max) return "The " + fieldName + " must not exceed " + max + " characters.";
        } else if (value instanceof java.util.Collection<?> c) {
            if (c.size() > max) return "The " + fieldName + " must not have more than " + max + " items.";
        } else if (value.getClass().isArray()) {
            int length = java.lang.reflect.Array.getLength(value);
            if (length > max) return "The " + fieldName + " must not have more than " + max + " items.";
        } else if (value instanceof Number) {
            return "The " + fieldName + " is a number. Use 'lte' for numeric maximum validation.";
        }
        return null;
    }
}