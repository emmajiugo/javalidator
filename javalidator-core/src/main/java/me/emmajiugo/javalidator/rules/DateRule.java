package me.emmajiugo.javalidator.rules;

import me.emmajiugo.javalidator.ValidationRule;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Validation rule that checks if a value is a valid date type or matches a date format.
 *
 * <p>Usage:
 * <ul>
 *     <li>{@code @Rule("date")} - Validates date types (LocalDate, LocalDateTime, Date)</li>
 *     <li>{@code @Rule("date:dd-MM-yyyy")} - Validates String matches format</li>
 *     <li>{@code @Rule("date:yyyy-MM-dd'T'HH:mm:ss")} - Validates DateTime format</li>
 * </ul>
 *
 * <p>Supports LocalDate, LocalDateTime, and java.util.Date types for type checking.
 * <p>For String values, validates against the provided format pattern.
 */
public class DateRule implements ValidationRule {

    @Override
    public String validate(String fieldName, Object value, String parameter) {
        // Allow null values (let 'required' rule handle nulls)
        if (value == null) {
            return null;
        }

        // If value is a String and format parameter is provided
        if (value instanceof String dateString && parameter != null && !parameter.isEmpty()) {
            return validateStringWithFormat(fieldName, dateString, parameter);
        }

        // Type checking for actual date types
        if (!(value instanceof LocalDate)
            && !(value instanceof LocalDateTime)
            && !(value instanceof java.util.Date)) {
            return "The " + fieldName + " must be a valid date.";
        }

        return null;
    }

    private String validateStringWithFormat(String fieldName, String dateString, String format) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);

            // Try parsing as LocalDate first
            try {
                LocalDate.parse(dateString, formatter);
                return null; // Valid date
            } catch (DateTimeParseException e) {
                // Try parsing as LocalDateTime
                LocalDateTime.parse(dateString, formatter);
                return null; // Valid datetime
            }
        } catch (IllegalArgumentException e) {
            return "The " + fieldName + " has an invalid date format pattern: " + format;
        } catch (DateTimeParseException e) {
            return "The " + fieldName + " must be a valid date in format: " + format;
        }
    }
}