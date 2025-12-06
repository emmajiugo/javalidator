package io.github.emmajiugo.javalidator.rules;

import io.github.emmajiugo.javalidator.ConditionalValidationRule;
import io.github.emmajiugo.javalidator.util.ReflectionUtils;

/**
 * Validation rule that makes a field required if another field has a specific value.
 *
 * <p>Usage: {@code @Rule("required_if:otherField,value")}
 *
 * <p>Example: {@code @Rule("required_if:country,USA")} makes field required if country is "USA".
 *
 * <p>The rule checks if the specified field has the expected value, and if so,
 * validates that the current field is not null or blank.
 */
public class RequiredIfRule implements ConditionalValidationRule {

    @Override
    public String validateWithContext(String fieldName, Object value, String parameter, Object dto) {
        if (parameter == null || parameter.isEmpty()) {
            return "The required_if rule requires parameters (field,value).";
        }

        // Parse parameter: "otherField,expectedValue"
        String[] parts = parameter.split(",", 2);
        if (parts.length != 2) {
            return "The required_if rule requires two parameters: field and value.";
        }

        String otherFieldName = parts[0].trim();
        String expectedValue = parts[1].trim();

        // Validate field name to prevent injection attacks
        if (!ReflectionUtils.isValidFieldName(otherFieldName)) {
            throw new IllegalArgumentException("Invalid field name in required_if rule: " + otherFieldName);
        }

        // Get the value of the other field
        Object otherFieldValue = ReflectionUtils.getFieldValueByName(dto, otherFieldName);

        // Check if the condition is met
        boolean conditionMet = false;
        if (otherFieldValue != null) {
            conditionMet = otherFieldValue.toString().equals(expectedValue);
        }

        // If condition is met, the current field is required
        if (conditionMet) {
            if (value == null || (value instanceof String s && s.isBlank())) {
                return "The " + fieldName + " field is required when " + otherFieldName + " is " + expectedValue + ".";
            }
        }

        return null; // Validation passes
    }

    @Override
    public String getName() {
        return "required_if";
    }
}
