package me.emmajiugo.javalidator.rules;

import me.emmajiugo.javalidator.ValidationRule;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Validation rule that checks if a string matches a regular expression pattern.
 *
 * <p>Usage: {@code @Rule("regex:^[A-Z]{2}\\d{4}$")}
 *
 * <p><b>Performance Note:</b> Regex patterns are compiled once and cached for reuse.
 * This significantly improves performance for repeated validations.
 *
 * <p><b>Security Note:</b> Regex patterns are defined by developers at compile-time
 * via annotations, not by users at runtime. Therefore, ReDoS (Regular Expression Denial
 * of Service) is not a security concern - if a developer writes a slow regex, it's a
 * code quality issue that should be caught during development and testing.
 */
public class RegexRule implements ValidationRule {

    // Cache compiled patterns for performance
    private static final Map<String, Pattern> PATTERN_CACHE = new ConcurrentHashMap<>();

    @Override
    public String validate(String fieldName, Object value, String parameter) {
        if (parameter == null || parameter.isEmpty()) {
            throw new IllegalArgumentException("Regex rule requires a parameter (e.g., 'regex:^[A-Z]+$')");
        }

        if (value instanceof String s) {
            try {
                // Get cached pattern or compile and cache new one
                Pattern pattern = PATTERN_CACHE.computeIfAbsent(parameter, Pattern::compile);

                if (!pattern.matcher(s).matches()) {
                    return "The " + fieldName + " format is invalid.";
                }
            } catch (PatternSyntaxException e) {
                throw new IllegalArgumentException("Invalid regex pattern: " + parameter, e);
            }
        }
        return null;
    }

    /**
     * Clears the pattern cache. Useful for testing or memory management.
     * This method is package-private for testing purposes.
     */
    static void clearCache() {
        PATTERN_CACHE.clear();
    }

    /**
     * Gets the current cache size. Useful for monitoring.
     * This method is package-private for testing purposes.
     */
    static int getCacheSize() {
        return PATTERN_CACHE.size();
    }
}
