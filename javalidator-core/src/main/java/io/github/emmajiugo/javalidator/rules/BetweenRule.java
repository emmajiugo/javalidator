package io.github.emmajiugo.javalidator.rules;

import io.github.emmajiugo.javalidator.ValidationRule;

/**
 * Validation rule that checks if a numeric value is between two values (inclusive).
 *
 * <p>Usage: {@code @Rule("between:min,max")}
 *
 * <p>Example: {@code @Rule("between:18,65")} ensures value is between 18 and 65 (inclusive).
 * <p>This is an alias for combining gte and lte rules.
 */
public class BetweenRule implements ValidationRule {

    @Override
    public String validate(String fieldName, Object value, String parameter) {
        if (value == null) {
            return null; // Let 'required' rule handle nulls
        }

        if (parameter == null || parameter.isEmpty()) {
            return "The between rule requires two parameters (min,max).";
        }

        String[] parts = parameter.split(",");
        if (parts.length != 2) {
            return "The between rule requires two parameters (min,max).";
        }

        try {
            double min = Double.parseDouble(parts[0].trim());
            double max = Double.parseDouble(parts[1].trim());

            if (value instanceof Number num) {
                double doubleValue = num.doubleValue();

                if (doubleValue < min || doubleValue > max) {
                    return "The " + fieldName + " must be between " + min + " and " + max + ".";
                }

                return null; // Valid
            }

            return "The " + fieldName + " must be a number.";

        } catch (NumberFormatException e) {
            return "The between rule parameters must be valid numbers.";
        }
    }
}
