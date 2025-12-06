package io.github.emmajiugo.javalidator.rules;

import io.github.emmajiugo.javalidator.ValidationRule;

/**
 * Validation rule that checks if a value is numeric.
 *
 * <p>Usage: {@code @Rule("numeric")}
 */
public class NumericRule implements ValidationRule {

    @Override
    public String validate(String fieldName, Object value, String parameter) {
        // Skip validation if value is null (let 'required' rule handle nulls)
        if (value == null) {
            return null;
        }

        if (!(value instanceof Number)) {
            return "The " + fieldName + " must be a number.";
        }
        return null;
    }
}