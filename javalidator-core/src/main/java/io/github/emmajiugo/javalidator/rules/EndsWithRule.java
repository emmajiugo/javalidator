package io.github.emmajiugo.javalidator.rules;

import io.github.emmajiugo.javalidator.ValidationRule;

/**
 * Validation rule that checks if a string ends with one of the specified suffixes.
 *
 * <p>Usage: {@code @Rule("ends_with:.com,.org,.net")}
 */
public class EndsWithRule implements ValidationRule {

    @Override
    public String validate(String fieldName, Object value, String parameter) {
        if (value == null) {
            return null;
        }

        if (parameter == null || parameter.isEmpty()) {
            throw new IllegalArgumentException("Ends_with rule requires a parameter (e.g., 'ends_with:.com,.org')");
        }

        String strValue = String.valueOf(value);
        String[] suffixes = parameter.split(",");

        for (String suffix : suffixes) {
            if (strValue.endsWith(suffix.trim())) {
                return null;
            }
        }

        return "The " + fieldName + " must end with one of: " + parameter + ".";
    }

    @Override
    public String getName() {
        return "ends_with";
    }
}
