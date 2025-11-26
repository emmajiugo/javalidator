package me.emmajiugo.javalidator.rules;

import me.emmajiugo.javalidator.ValidationRule;

/**
 * Validation rule that checks if a number is less than a specified value.
 *
 * <p>Usage: {@code @Rule("lt:100")}
 */
public class LtRule implements ValidationRule {

    @Override
    public String validate(String fieldName, Object value, String parameter) {
        if (parameter == null || parameter.isEmpty()) {
            throw new IllegalArgumentException("Lt rule requires a parameter (e.g., 'lt:100')");
        }

        int max = Integer.parseInt(parameter);
        if (value instanceof Number n && n.intValue() >= max) {
            return "The " + fieldName + " must be less than " + max + ".";
        }
        return null;
    }
}