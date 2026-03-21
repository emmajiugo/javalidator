package io.github.emmajiugo.javalidator.rules;

import io.github.emmajiugo.javalidator.ValidationRule;

/**
 * Validation rule that checks if a number is greater than a specified value.
 *
 * <p>Usage: {@code @Rule("gt:0")}
 */
public class GtRule implements ValidationRule {

    @Override
    public String validate(String fieldName, Object value, String parameter) {
        // Allow null values - let the "required" rule handle null checks
        if (value == null) {
            return null;
        }

        if (parameter == null || parameter.isEmpty()) {
            throw new IllegalArgumentException("Gt rule requires a parameter (e.g., 'gt:0')");
        }

        double min = Double.parseDouble(parameter);

        // Convert value to double
        double numericValue;
        if (value instanceof Number n) {
            numericValue = n.doubleValue();
        } else if (value instanceof String s) {
            try {
                numericValue = Double.parseDouble(s);
            } catch (NumberFormatException e) {
                return String.format(
                        "The %s value must be a valid number to use 'gt:%s' validation.",
                        fieldName, formatNumber(min)
                );
            }
        } else {
            throw new IllegalArgumentException(
                    "Gt rule only supports Number and String types for numeric comparison."
            );
        }

        // Single validation check
        if (numericValue <= min) {
            return String.format("The %s must be greater than %s.", fieldName, formatNumber(min));
        }

        return null;
    }

    private String formatNumber(double value) {
        if (value == Math.floor(value) && !Double.isInfinite(value)) {
            return String.valueOf((long) value);
        }
        return String.valueOf(value);
    }
}
