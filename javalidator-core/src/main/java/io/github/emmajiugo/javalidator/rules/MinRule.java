package io.github.emmajiugo.javalidator.rules;

import io.github.emmajiugo.javalidator.ValidationRule;

/**
 * Validation rule that checks if a string has a minimum length.
 *
 * <p>Usage: {@code @Rule("min:3")}
 */
public class MinRule implements ValidationRule {

    @Override
    public String validate(String fieldName, Object value, String parameter) {
        if (parameter == null || parameter.isEmpty()) {
            throw new IllegalArgumentException("Min rule requires a parameter (e.g., 'min:3')");
        }

        int min = Integer.parseInt(parameter);
        if (value instanceof String s && s.length() < min) {
            return "The " + fieldName + " must be at least " + min + " characters.";
        }
        return null;
    }
}