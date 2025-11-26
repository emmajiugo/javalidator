package me.emmajiugo.javalidator.rules;

import me.emmajiugo.javalidator.ConditionalValidationRule;
import me.emmajiugo.javalidator.util.ReflectionUtils;

/**
 * Validation rule that checks if a field matches its confirmation field.
 *
 * <p>Usage: {@code @Rule("confirmed")} or {@code @Rule("confirmed:confirmationFieldName")}
 *
 * <p>By default, this rule expects a confirmation field with "_confirmation" suffix.
 * <p>Example: For field "password", it looks for "password_confirmation".
 *
 * <p>You can also specify a custom confirmation field name:
 * <p>Example: {@code @Rule("confirmed:passwordConfirm")} looks for field "passwordConfirm".
 */
public class ConfirmedRule implements ConditionalValidationRule {

    @Override
    public String validateWithContext(String fieldName, Object value, String parameter, Object dto) {
        // Skip validation if value is null (let 'required' rule handle nulls)
        if (value == null) {
            return null;
        }

        // Determine confirmation field name
        String confirmationFieldName;
        if (parameter != null && !parameter.isEmpty()) {
            // Custom confirmation field name provided
            confirmationFieldName = parameter.trim();
        } else {
            // Default: fieldName + "_confirmation"
            confirmationFieldName = fieldName + "_confirmation";
        }

        // Validate field name to prevent injection attacks
        if (!ReflectionUtils.isValidFieldName(confirmationFieldName)) {
            throw new IllegalArgumentException("Invalid field name in confirmed rule: " + confirmationFieldName);
        }

        // Get the value of the confirmation field
        Object confirmationValue = ReflectionUtils.getFieldValueByName(dto, confirmationFieldName);

        // Check if values match
        if (!value.equals(confirmationValue)) {
            return "The " + fieldName + " confirmation does not match.";
        }

        return null; // Validation passes
    }

    @Override
    public String getName() {
        return "confirmed";
    }
}
