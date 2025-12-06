package io.github.emmajiugo.javalidator.rules;

import io.github.emmajiugo.javalidator.ConditionalValidationRule;
import io.github.emmajiugo.javalidator.util.ReflectionUtils;

/**
 * Validation rule that checks if a field's value matches another field's value.
 *
 * <p>Usage: {@code @Rule("same:otherFieldName")}
 *
 * <p>This rule compares the value of the current field with the value of another field
 * in the same object. Both fields must have equal values for validation to pass.
 *
 * <p>Example: {@code @Rule("same:email")} ensures the current field has the same value as the "email" field.
 *
 * <p>Common use cases:
 * <ul>
 *   <li>Email confirmation: {@code @Rule("same:email")}</li>
 *   <li>Password confirmation: {@code @Rule("same:password")}</li>
 *   <li>Any field that requires confirmation</li>
 * </ul>
 */
public class SameRule implements ConditionalValidationRule {

    @Override
    public String validateWithContext(String fieldName, Object value, String parameter, Object dto) {
        // Skip validation if value is null (let 'required' rule handle nulls)
        if (value == null) {
            return null;
        }

        // Parameter is required - must specify which field to compare with
        if (parameter == null || parameter.isEmpty()) {
            throw new IllegalArgumentException("The 'same' rule requires a field name parameter (e.g., 'same:email')");
        }

        String targetFieldName = parameter.trim();

        // Validate field name to prevent injection attacks
        if (!ReflectionUtils.isValidFieldName(targetFieldName)) {
            throw new IllegalArgumentException("Invalid field name in same rule: " + targetFieldName);
        }

        // Get the value of the target field
        Object targetValue = ReflectionUtils.getFieldValueByName(dto, targetFieldName);

        // Check if values match
        if (!value.equals(targetValue)) {
            return "The " + fieldName + " must match " + targetFieldName + ".";
        }

        return null; // Validation passes
    }

    @Override
    public String getName() {
        return "same";
    }
}