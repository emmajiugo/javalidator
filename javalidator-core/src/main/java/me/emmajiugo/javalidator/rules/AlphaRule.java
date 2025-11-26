package me.emmajiugo.javalidator.rules;

import me.emmajiugo.javalidator.ValidationRule;

/**
 * Validation rule that checks if a value contains only alphabetic characters.
 *
 * <p>Usage: {@code @Rule("alpha")}
 *
 * <p>Validates that the string contains only letters (a-z, A-Z).
 */
public class AlphaRule implements ValidationRule {

    @Override
    public String validate(String fieldName, Object value, String parameter) {
        if (value == null) {
            return null; // Let 'required' rule handle nulls
        }

        if (value instanceof String str) {
            if (str.matches("^[a-zA-Z]+$")) {
                return null; // Valid - only alphabetic characters
            }
            return "The " + fieldName + " must contain only alphabetic characters.";
        }

        return "The " + fieldName + " must contain only alphabetic characters.";
    }
}
