package io.github.emmajiugo.javalidator.rules;

import io.github.emmajiugo.javalidator.ValidationRule;

/**
 * Validation rule that checks if a number is less than a specified value.
 *
 * <p>Usage: {@code @Rule("lt:100")}
 */
public class LtRule implements ValidationRule {

    @Override
    public String validate(String fieldName, Object value, String parameter) {
        // Allow null values - let the "required" rule handle null checks
        if (value == null) {
            return null;
        }

        if (parameter == null || parameter.isEmpty()) {
            throw new IllegalArgumentException("Lt rule requires a parameter (e.g., 'lt:100')");
        }

        int max = Integer.parseInt(parameter);

        // Convert value to integer
        int numericValue;
        if (value instanceof Number n) {
            numericValue = n.intValue();
        } else if (value instanceof String s) {
            try {
                numericValue = Integer.parseInt(s);
            } catch (NumberFormatException e) {
                return String.format(
                        "The %s value must be a valid number to use 'lt:%d' validation.",
                        fieldName, max
                );
            }
        } else {
            throw new IllegalArgumentException(
                    "Lt rule only supports Number and String types for numeric comparison."
            );
        }

        // Single validation check
        if (numericValue >= max) {
            return String.format("The %s must be less than %d.", fieldName, max);
        }

        return null;
    }
}