package me.emmajiugo.javalidator.rules;

import me.emmajiugo.javalidator.ValidationRule;

/**
 * Validation rule that checks if a number is greater than a specified value.
 *
 * <p>Usage: {@code @Rule("gt:0")}
 */
public class GtRule implements ValidationRule {

    @Override
    public String validate(String fieldName, Object value, String parameter) {
        if (parameter == null || parameter.isEmpty()) {
            throw new IllegalArgumentException("Gt rule requires a parameter (e.g., 'gt:0')");
        }

        int min = Integer.parseInt(parameter);
        if (value instanceof Number n && n.intValue() <= min) {
            return "The " + fieldName + " must be greater than " + min + ".";
        }
        return null;
    }
}