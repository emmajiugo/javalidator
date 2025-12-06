package io.github.emmajiugo.javalidator.model;

import java.util.List;

/**
 * Represents a validation error for a specific field.
 *
 * <p>Contains the field name and a list of error messages associated with that field.
 * Multiple validation rules can fail for the same field, resulting in multiple messages.
 *
 * @param field    the name of the field that failed validation
 * @param messages the list of error messages for this field
 */
public record ValidationError(String field, List<String> messages) {
}