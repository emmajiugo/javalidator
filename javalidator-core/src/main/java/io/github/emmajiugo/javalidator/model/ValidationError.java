package io.github.emmajiugo.javalidator.model;

import java.util.List;

/**
 * Represents a validation error for a specific field.
 *
 * <p>Contains the field name, a list of error messages, and a parallel list of rule names
 * associated with that field. Multiple validation rules can fail for the same field,
 * resulting in multiple messages. In the default case the {@code rules} list has a
 * one-to-one correspondence with the {@code messages} list, identifying which rule
 * produced each message.
 *
 * <p><strong>Note on custom messages:</strong> When a {@code @Rule} annotation supplies a
 * custom {@code message}, that single message is recorded once in {@code messages} while
 * every failed rule name is still appended to {@code rules}. In that situation
 * {@code rules} may be longer than {@code messages}. Consumers must not assume the two
 * lists have equal length.
 *
 * @param field    the name of the field that failed validation
 * @param messages the list of error messages for this field
 * @param rules    the list of rule names that failed; may be longer than {@code messages}
 *                 when a custom message is used
 */
public record ValidationError(String field, List<String> messages, List<String> rules) {
}