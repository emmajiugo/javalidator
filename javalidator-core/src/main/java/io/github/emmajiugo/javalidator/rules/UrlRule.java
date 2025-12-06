package io.github.emmajiugo.javalidator.rules;

import io.github.emmajiugo.javalidator.ValidationRule;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Validation rule that checks if a value is a valid URL.
 *
 * <p>Usage: {@code @Rule("url")}
 *
 * <p>Validates URL format using Java's URL class.
 */
public class UrlRule implements ValidationRule {

    @Override
    public String validate(String fieldName, Object value, String parameter) {
        if (value == null) {
            return null; // Let 'required' rule handle nulls
        }

        if (value instanceof String url) {
            try {
                new URL(url);
                return null; // Valid URL
            } catch (MalformedURLException e) {
                return "The " + fieldName + " must be a valid URL.";
            }
        }

        return "The " + fieldName + " must be a valid URL.";
    }
}
