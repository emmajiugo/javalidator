package me.emmajiugo.javalidator.rules;

import me.emmajiugo.javalidator.ValidationRule;

import java.util.UUID;

/**
 * Validation rule that checks if a value is a valid UUID.
 *
 * <p>Usage: {@code @Rule("uuid")}
 *
 * <p>Validates UUID format (e.g., 550e8400-e29b-41d4-a716-446655440000).
 */
public class UuidRule implements ValidationRule {

    @Override
    public String validate(String fieldName, Object value, String parameter) {
        if (value == null) {
            return null; // Let 'required' rule handle nulls
        }

        if (value instanceof String uuidString) {
            try {
                UUID.fromString(uuidString);
                return null; // Valid UUID
            } catch (IllegalArgumentException e) {
                return "The " + fieldName + " must be a valid UUID.";
            }
        }

        return "The " + fieldName + " must be a valid UUID.";
    }
}
