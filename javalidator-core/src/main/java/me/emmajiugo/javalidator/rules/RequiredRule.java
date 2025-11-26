package me.emmajiugo.javalidator.rules;

import me.emmajiugo.javalidator.ValidationRule;

/**
 * Validation rule that checks if a field is not null or blank.
 *
 * <p>Usage: {@code @Rule("required")}
 */
public class RequiredRule implements ValidationRule {

    @Override
    public String validate(String fieldName, Object value, String parameter) {
        if (value == null || (value instanceof String s && s.isBlank())) {
            return "The " + fieldName + " field is required.";
        }
        return null;
    }
}