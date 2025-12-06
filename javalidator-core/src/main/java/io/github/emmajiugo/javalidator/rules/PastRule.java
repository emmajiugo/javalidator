package io.github.emmajiugo.javalidator.rules;

import io.github.emmajiugo.javalidator.ValidationRule;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Validation rule that checks if a date is in the past.
 *
 * <p>Usage: {@code @Rule("past")}
 *
 * <p>Ensures the date is before the current date/time.
 * <p>Supports LocalDate and LocalDateTime.
 */
public class PastRule implements ValidationRule {

    @Override
    public String validate(String fieldName, Object value, String parameter) {
        if (value == null) {
            return null; // Let 'required' rule handle nulls
        }

        if (value instanceof LocalDate date) {
            if (!date.isBefore(LocalDate.now())) {
                return "The " + fieldName + " must be a past date.";
            }
            return null; // Valid
        }

        if (value instanceof LocalDateTime dateTime) {
            if (!dateTime.isBefore(LocalDateTime.now())) {
                return "The " + fieldName + " must be a past date.";
            }
            return null; // Valid
        }

        if (value instanceof java.util.Date date) {
            LocalDate localDate = new java.sql.Date(date.getTime()).toLocalDate();
            if (!localDate.isBefore(LocalDate.now())) {
                return "The " + fieldName + " must be a past date.";
            }
            return null; // Valid
        }

        return "The " + fieldName + " must be a valid date type.";
    }
}
