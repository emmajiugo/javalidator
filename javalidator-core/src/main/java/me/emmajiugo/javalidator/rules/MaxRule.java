package me.emmajiugo.javalidator.rules;

import me.emmajiugo.javalidator.ValidationRule;

/**
 * Validation rule that checks if a string does not exceed a maximum length.
 *
 * <p>Usage: {@code @Rule("max:20")}
 */
public class MaxRule implements ValidationRule {

    @Override
    public String validate(String fieldName, Object value, String parameter) {
        if (parameter == null || parameter.isEmpty()) {
            throw new IllegalArgumentException("Max rule requires a parameter (e.g., 'max:20')");
        }

        int max = Integer.parseInt(parameter);
        if (value instanceof String s && s.length() > max) {
            return "The " + fieldName + " must not exceed " + max + " characters.";
        }
        return null;
    }
}