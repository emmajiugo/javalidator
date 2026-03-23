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

        double max = Double.parseDouble(parameter);

        // Convert value to double
        double numericValue;
        if (value instanceof Number n) {
            numericValue = n.doubleValue();
        } else if (value instanceof String s) {
            try {
                numericValue = Double.parseDouble(s);
            } catch (NumberFormatException e) {
                return String.format(
                        "The %s value must be a valid number to use 'lt:%s' validation.",
                        fieldName, formatNumber(max)
                );
            }
        } else {
            throw new IllegalArgumentException(
                    "Lt rule only supports Number and String types for numeric comparison."
            );
        }

        // Single validation check
        if (numericValue >= max) {
            return String.format("The %s must be less than %s.", fieldName, formatNumber(max));
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
