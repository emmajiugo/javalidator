package io.github.emmajiugo.javalidator.rules;

import io.github.emmajiugo.javalidator.ValidationRule;

/**
 * Validation rule that checks if a string starts with one of the specified prefixes.
 *
 * <p>Usage: {@code @Rule("starts_with:http,https")}
 */
public class StartsWithRule implements ValidationRule {

    @Override
    public String validate(String fieldName, Object value, String parameter) {
        if (value == null) {
            return null;
        }

        if (parameter == null || parameter.isEmpty()) {
            throw new IllegalArgumentException("Starts_with rule requires a parameter (e.g., 'starts_with:http,https')");
        }

        String strValue = String.valueOf(value);
        String[] prefixes = parameter.split(",");

        for (String prefix : prefixes) {
            if (strValue.startsWith(prefix.trim())) {
                return null;
            }
        }

        return "The " + fieldName + " must start with one of: " + parameter + ".";
    }

    @Override
    public String getName() {
        return "starts_with";
    }
}
