package io.github.emmajiugo.javalidator.rules;

import io.github.emmajiugo.javalidator.ValidationRule;

/**
 * Validation rule that checks if a string has a minimum length, or a collection/array has a minimum number of items.
 *
 * <p>Usage: {@code @Rule("min:3")}
 */
public class MinRule implements ValidationRule {

    @Override
    public String validate(String fieldName, Object value, String parameter) {
        if (value == null) return null;

        if (parameter == null || parameter.isEmpty()) {
            throw new IllegalArgumentException("Min rule requires a parameter (e.g., 'min:3')");
        }

        int min = Integer.parseInt(parameter);
        if (value instanceof String s) {
            if (s.length() < min) return "The " + fieldName + " must be at least " + min + " characters.";
        } else if (value instanceof java.util.Collection<?> c) {
            if (c.size() < min) return "The " + fieldName + " must have at least " + min + " items.";
        } else if (value.getClass().isArray()) {
            int length = java.lang.reflect.Array.getLength(value);
            if (length < min) return "The " + fieldName + " must have at least " + min + " items.";
        } else if (value instanceof Number) {
            return "The " + fieldName + " is a number. Use 'gte' for numeric minimum validation.";
        }
        return null;
    }
}