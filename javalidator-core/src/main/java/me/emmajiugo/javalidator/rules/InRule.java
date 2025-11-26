package me.emmajiugo.javalidator.rules;

import me.emmajiugo.javalidator.ValidationRule;

/**
 * Validation rule that checks if a value is one of the allowed values.
 *
 * <p>Usage: {@code @Rule("in:admin,user,guest")}
 */
public class InRule implements ValidationRule {

    @Override
    public String validate(String fieldName, Object value, String parameter) {
        if (parameter == null || parameter.isEmpty()) {
            throw new IllegalArgumentException("In rule requires a parameter (e.g., 'in:value1,value2')");
        }

        String[] allowedValues = parameter.split(",");
        String strValue = String.valueOf(value);

        for (String allowed : allowedValues) {
            if (allowed.trim().equals(strValue)) {
                return null;
            }
        }

        return "The " + fieldName + " must be one of: " + parameter + ".";
    }
}