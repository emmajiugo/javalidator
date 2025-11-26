package me.emmajiugo.javalidator.rules;

import me.emmajiugo.javalidator.ValidationRule;

import java.util.regex.Pattern;

/**
 * Validation rule that checks if a string is a valid email address.
 *
 * <p>Usage: {@code @Rule("email")}
 */
public class EmailRule implements ValidationRule {

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    @Override
    public String validate(String fieldName, Object value, String parameter) {
        if (value instanceof String s) {
            if (!EMAIL_PATTERN.matcher(s).matches()) {
                return "The " + fieldName + " must be a valid email address.";
            }
        }
        return null;
    }
}