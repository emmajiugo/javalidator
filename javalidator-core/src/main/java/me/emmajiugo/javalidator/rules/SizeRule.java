package me.emmajiugo.javalidator.rules;

import me.emmajiugo.javalidator.ValidationRule;

import java.util.Collection;

/**
 * Validation rule that checks if a value has an exact size.
 *
 * <p>Usage: {@code @Rule("size:n")}
 *
 * <p>For strings: checks character count.
 * <p>For collections/arrays: checks element count.
 */
public class SizeRule implements ValidationRule {

    @Override
    public String validate(String fieldName, Object value, String parameter) {
        if (value == null) {
            return null; // Let 'required' rule handle nulls
        }

        if (parameter == null || parameter.isEmpty()) {
            return "The size rule requires a numeric parameter.";
        }

        try {
            int expectedSize = Integer.parseInt(parameter);
            int actualSize;

            if (value instanceof String str) {
                actualSize = str.length();
            } else if (value instanceof Collection<?> collection) {
                actualSize = collection.size();
            } else if (value.getClass().isArray()) {
                actualSize = java.lang.reflect.Array.getLength(value);
            } else {
                return "The " + fieldName + " size cannot be determined.";
            }

            if (actualSize != expectedSize) {
                return "The " + fieldName + " must be exactly " + expectedSize + " characters/items.";
            }

            return null; // Valid

        } catch (NumberFormatException e) {
            return "The size rule parameter must be a valid number.";
        }
    }
}
