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
        // Allow null values - let the "required" rule handle null checks
        if (value == null) {
            return null;
        }

        if (parameter == null || parameter.isEmpty()) {
            throw new IllegalArgumentException("Lte rule requires a parameter (e.g., 'lte:100')");
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
                        "The %s value must be a valid number to use 'lte:%d' validation.",
                        fieldName, max
                );
            }
        } else {
            throw new IllegalArgumentException(
                    "Lte rule only supports Number and String types for numeric comparison."
            );
        }

        // Single validation check
        if (numericValue > max) {
            return String.format("The %s must not exceed %d.", fieldName, max);
        }

        return null;
    }
}