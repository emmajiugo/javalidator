package io.github.emmajiugo.javalidator.rules;

import io.github.emmajiugo.javalidator.ValidationRule;

/**
 * Validation rule that checks if a value is NOT one of the specified values.
 *
 * <p>Usage: {@code @Rule("not_in:admin,superuser,root")}
 */
public class NotInRule implements ValidationRule {

    @Override
    public String validate(String fieldName, Object value, String parameter) {
        if (value == null) {
            return null;
        }

        if (parameter == null || parameter.isEmpty()) {
            throw new IllegalArgumentException("Not_in rule requires a parameter (e.g., 'not_in:value1,value2')");
        }

        String[] disallowedValues = parameter.split(",");
        String strValue = String.valueOf(value);

        for (String disallowed : disallowedValues) {
            if (disallowed.trim().equals(strValue)) {
                return "The " + fieldName + " must not be one of: " + parameter + ".";
            }
        }

        return null;
    }

    @Override
    public String getName() {
        return "not_in";
    }
}
