package io.github.emmajiugo.javalidator.rules;

import io.github.emmajiugo.javalidator.ConditionalValidationRule;
import io.github.emmajiugo.javalidator.util.ReflectionUtils;

/**
 * Validation rule that makes a field required unless another field has a specific value.
 *
 * <p>Usage: {@code @Rule("required_unless:otherField,value")}
 *
 * <p>Example: {@code @Rule("required_unless:payment,cash")} makes field required unless payment is "cash".
 *
 * <p>The rule checks if the specified field does NOT have the expected value, and if so,
 * validates that the current field is not null or blank.
 */
public class RequiredUnlessRule implements ConditionalValidationRule {

    @Override
    public String validateWithContext(String fieldName, Object value, String parameter, Object dto) {
        if (parameter == null || parameter.isEmpty()) {
            return "The required_unless rule requires parameters (field,value).";
        }

        // Parse parameter: "otherField,expectedValue"
        String[] parts = parameter.split(",", 2);
        if (parts.length != 2) {
            return "The required_unless rule requires two parameters: field and value.";
        }

        String otherFieldName = parts[0].trim();
        String expectedValue = parts[1].trim();

        // Validate field name to prevent injection attacks
        if (!ReflectionUtils.isValidFieldName(otherFieldName)) {
            throw new IllegalArgumentException("Invalid field name in required_unless rule: " + otherFieldName);
        }

        // Get the value of the other field
        Object otherFieldValue = ReflectionUtils.getFieldValueByName(dto, otherFieldName);

        // Check if the condition is met (opposite of required_if)
        boolean conditionMet = true; // Default to true (field is required)
        if (otherFieldValue != null) {
            conditionMet = !otherFieldValue.toString().equals(expectedValue);
        }

        // If condition is met (other field is NOT the expected value), the current field is required
        if (conditionMet) {
            if (value == null || (value instanceof String s && s.isBlank())) {
                return "The " + fieldName + " field is required unless " + otherFieldName + " is " + expectedValue + ".";
            }
        }

        return null; // Validation passes
    }

    @Override
    public String getName() {
        return "required_unless";
    }
}
