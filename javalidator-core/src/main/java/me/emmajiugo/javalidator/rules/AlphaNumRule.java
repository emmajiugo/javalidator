package me.emmajiugo.javalidator.rules;

import me.emmajiugo.javalidator.ValidationRule;

/**
 * Validation rule that checks if a value contains only alphanumeric characters.
 *
 * <p>Usage: {@code @Rule("alpha_num")}
 *
 * <p>Validates that the string contains only letters and numbers (a-z, A-Z, 0-9).
 */
public class AlphaNumRule implements ValidationRule {

    @Override
    public String validate(String fieldName, Object value, String parameter) {
        if (value == null) {
            return null; // Let 'required' rule handle nulls
        }

        if (value instanceof String str) {
            if (str.matches("^[a-zA-Z0-9]+$")) {
                return null; // Valid - only alphanumeric characters
            }
            return "The " + fieldName + " must contain only alphanumeric characters.";
        }

        return "The " + fieldName + " must contain only alphanumeric characters.";
    }

    @Override
    public String getName() {
        return "alpha_num";
    }
}
