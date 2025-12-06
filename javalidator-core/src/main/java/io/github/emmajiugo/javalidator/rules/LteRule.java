package io.github.emmajiugo.javalidator.rules;

import io.github.emmajiugo.javalidator.ValidationRule;

/**
 * Validation rule that checks if a number is less than or equal to a specified value.
 *
 * <p>Usage: {@code @Rule("lte:100")}
 */
public class LteRule implements ValidationRule {

    @Override
    public String validate(String fieldName, Object value, String parameter) {
        if (parameter == null || parameter.isEmpty()) {
            throw new IllegalArgumentException("Lte rule requires a parameter (e.g., 'lte:100')");
        }

        int max = Integer.parseInt(parameter);
        if (value instanceof Number n && n.intValue() > max) {
            return "The " + fieldName + " must not exceed " + max + ".";
        }
        return null;
    }
}