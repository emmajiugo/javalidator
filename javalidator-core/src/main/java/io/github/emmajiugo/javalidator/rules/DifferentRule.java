package io.github.emmajiugo.javalidator.rules;

import io.github.emmajiugo.javalidator.ConditionalValidationRule;
import io.github.emmajiugo.javalidator.util.ReflectionUtils;

/**
 * Validation rule that checks if a field's value is different from another field's value.
 *
 * <p>Usage: {@code @Rule("different:otherFieldName")}
 *
 * <p>This rule compares the value of the current field with the value of another field
 * in the same object. The fields must have different values for validation to pass.
 *
 * <p>Example: {@code @Rule("different:oldPassword")} ensures the current field has a different value than the "oldPassword" field.
 *
 * <p>Common use cases:
 * <ul>
 *   <li>New password different from old: {@code @Rule("different:oldPassword")}</li>
 *   <li>Alternative email different from primary: {@code @Rule("different:primaryEmail")}</li>
 *   <li>Shipping address different from billing: {@code @Rule("different:billingAddress")}</li>
 * </ul>
 */
public class DifferentRule implements ConditionalValidationRule {

    @Override
    public String validateWithContext(String fieldName, Object value, String parameter, Object dto) {
        // Skip validation if value is null (let 'required' rule handle nulls)
        if (value == null) {
            return null;
        }

        // Parameter is required - must specify which field to compare with
        if (parameter == null || parameter.isEmpty()) {
            throw new IllegalArgumentException("The 'different' rule requires a field name parameter (e.g., 'different:oldPassword')");
        }

        String targetFieldName = parameter.trim();

        // Validate field name to prevent injection attacks
        if (!ReflectionUtils.isValidFieldName(targetFieldName)) {
            throw new IllegalArgumentException("Invalid field name in different rule: " + targetFieldName);
        }

        // Get the value of the target field
        Object targetValue = ReflectionUtils.getFieldValueByName(dto, targetFieldName);

        // Check if values are different
        if (value.equals(targetValue)) {
            return "The " + fieldName + " must be different from " + targetFieldName + ".";
        }

        return null; // Validation passes
    }

    @Override
    public String getName() {
        return "different";
    }
}