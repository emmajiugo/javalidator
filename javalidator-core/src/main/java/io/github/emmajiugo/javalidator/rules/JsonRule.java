package io.github.emmajiugo.javalidator.rules;

import io.github.emmajiugo.javalidator.ValidationRule;

/**
 * Validation rule that checks if a value is valid JSON.
 *
 * <p>Usage: {@code @Rule("json")}
 *
 * <p>Validates basic JSON syntax (objects, arrays, strings, numbers, booleans, null).
 * <p>Note: This is a basic implementation. For complex JSON validation, consider using a JSON library.
 */
public class JsonRule implements ValidationRule {

    @Override
    public String validate(String fieldName, Object value, String parameter) {
        if (value == null) {
            return null; // Let 'required' rule handle nulls
        }

        if (value instanceof String json) {
            if (isValidJson(json.trim())) {
                return null; // Valid JSON
            }
            return "The " + fieldName + " must be valid JSON.";
        }

        return "The " + fieldName + " must be valid JSON.";
    }

    private boolean isValidJson(String json) {
        if (json.isEmpty()) {
            return false;
        }

        // Check if it's a JSON object
        if (json.startsWith("{") && json.endsWith("}")) {
            // Basic validation: must have balanced braces and proper structure
            if (!isBalanced(json, '{', '}')) {
                return false;
            }
            // Must contain at least one quote (for keys/values) or be empty object
            return json.length() <= 2 || json.contains("\"");
        }

        // Check if it's a JSON array
        if (json.startsWith("[") && json.endsWith("]")) {
            return isBalanced(json, '[', ']');
        }

        // Check if it's a JSON string
        if (json.startsWith("\"") && json.endsWith("\"") && json.length() >= 2) {
            return true;
        }

        // Check if it's a JSON number
        if (json.matches("-?\\d+(\\.\\d+)?([eE][+-]?\\d+)?")) {
            return true;
        }

        // Check if it's a JSON boolean or null
        return json.equals("true") || json.equals("false") || json.equals("null");
    }

    private boolean isBalanced(String json, char open, char close) {
        int count = 0;
        boolean inString = false;
        boolean escape = false;

        for (char c : json.toCharArray()) {
            if (escape) {
                escape = false;
                continue;
            }

            if (c == '\\') {
                escape = true;
                continue;
            }

            if (c == '"') {
                inString = !inString;
                continue;
            }

            if (!inString) {
                if (c == open) {
                    count++;
                } else if (c == close) {
                    count--;
                    if (count < 0) {
                        return false;
                    }
                }
            }
        }

        return count == 0;
    }
}
