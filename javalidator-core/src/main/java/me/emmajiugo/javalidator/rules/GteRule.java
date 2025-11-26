package me.emmajiugo.javalidator.rules;

import me.emmajiugo.javalidator.ValidationRule;

/**
 * Validation rule that checks if a number is greater than or equal to a specified value.
 *
 * <p>Usage: {@code @Rule("gte:18")}
 */
public class GteRule implements ValidationRule {

    @Override
    public String validate(String fieldName, Object value, String parameter) {
        if (parameter == null || parameter.isEmpty()) {
            throw new IllegalArgumentException("Gte rule requires a parameter (e.g., 'gte:18')");
        }

        int min = Integer.parseInt(parameter);
        if (value instanceof Number n && n.intValue() < min) {
            return "The " + fieldName + " must be at least " + min + ".";
        }
        return null;
    }
}