package io.github.emmajiugo.javalidator.model;

import java.util.List;

/**
 * Represents a validation error for a specific field.
 *
 * <p>Contains the field name, a list of error messages, and a parallel list of rule names
 * associated with that field. Multiple validation rules can fail for the same field,
 * resulting in multiple messages. The {@code rules} list has a one-to-one correspondence
 * with the {@code messages} list, identifying which rule produced each message.
 *
 * @param field    the name of the field that failed validation
 * @param messages the list of error messages for this field
 * @param rules    the list of rule names that failed, parallel to messages
 */
public record ValidationError(String field, List<String> messages, List<String> rules) {
}