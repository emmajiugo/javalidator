package io.github.emmajiugo.javalidator.rules;

import io.github.emmajiugo.javalidator.ValidationRule;

/**
 * Validation rule that checks if a value has an exact number of digits.
 *
 * <p>Usage: {@code @Rule("digits:5")}
 *
 * <p>This rule validates that the field contains exactly the specified number of digits.
 * It works with String, Integer, Long, and other numeric types.
 *
 * <p>Example: {@code @Rule("digits:4")} ensures the field has exactly 4 digits (like "1234" or 1234).
 *
 * <p>Common use cases:
 * <ul>
 *   <li>PIN codes: {@code @Rule("digits:4")}</li>
 *   <li>ZIP codes: {@code @Rule("digits:5")}</li>
 *   <li>Verification codes: {@code @Rule("digits:6")}</li>
 *   <li>Year validation: {@code @Rule("digits:4")}</li>
 * </ul>
 *
 * <p>Note: This rule only counts digits (0-9). Other characters like spaces,
 * hyphens, or letters will cause validation to fail.
 */
public class DigitsRule implements ValidationRule {

    @Override
    public String validate(String fieldName, Object value, String parameter) {
        // Skip validation if value is null (let 'required' rule handle nulls)
        if (value == null) {
            return null;
        }

        // Parameter is required
        if (parameter == null || parameter.isEmpty()) {
            throw new IllegalArgumentException("The 'digits' rule requires a parameter specifying the number of digits (e.g., 'digits:4')");
        }

        // Parse the expected digit count
        int expectedDigits;
        try {
            expectedDigits = Integer.parseInt(parameter.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("The 'digits' rule parameter must be a valid integer: " + parameter);
        }

        if (expectedDigits < 1) {
            throw new IllegalArgumentException("The 'digits' rule parameter must be a positive integer: " + parameter);
        }

        // Convert value to string and extract only digits
        String valueStr = value.toString();
        String digitsOnly = valueStr.replaceAll("\\D", "");

        // Check if all characters are digits (no non-digit characters allowed)
        if (!valueStr.matches("\\d+")) {
            return "The " + fieldName + " must contain only digits.";
        }

        // Check if the number of digits matches exactly
        if (digitsOnly.length() != expectedDigits) {
            return "The " + fieldName + " must be exactly " + expectedDigits + " digits.";
        }

        return null; // Validation passes
    }

    @Override
    public String getName() {
        return "digits";
    }
}