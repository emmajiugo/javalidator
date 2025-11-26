package me.emmajiugo.javalidator.rules;

import me.emmajiugo.javalidator.ValidationRule;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

/**
 * Validation rule that checks if a date is after a specified date.
 *
 * <p>Usage: {@code @Rule("after:yyyy-MM-dd")}
 *
 * <p>Example: {@code @Rule("after:2024-01-01")} ensures date is after 2024-01-01.
 * <p>Supports LocalDate and LocalDateTime.
 */
public class AfterRule implements ValidationRule {

    @Override
    public String validate(String fieldName, Object value, String parameter) {
        if (value == null) {
            return null; // Let 'required' rule handle nulls
        }

        if (parameter == null || parameter.isEmpty()) {
            return "The after rule requires a date parameter (yyyy-MM-dd).";
        }

        try {
            LocalDate compareDate = LocalDate.parse(parameter);
            LocalDate valueDate;

            if (value instanceof LocalDate date) {
                valueDate = date;
            } else if (value instanceof LocalDateTime dateTime) {
                valueDate = dateTime.toLocalDate();
            } else if (value instanceof java.util.Date date) {
                valueDate = new java.sql.Date(date.getTime()).toLocalDate();
            } else {
                return "The " + fieldName + " must be a valid date type.";
            }

            if (!valueDate.isAfter(compareDate)) {
                return "The " + fieldName + " must be after " + parameter + ".";
            }

            return null; // Valid

        } catch (DateTimeParseException e) {
            return "The after rule parameter must be a valid date (yyyy-MM-dd).";
        }
    }
}
