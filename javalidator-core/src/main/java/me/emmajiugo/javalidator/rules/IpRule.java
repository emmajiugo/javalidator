package me.emmajiugo.javalidator.rules;

import me.emmajiugo.javalidator.ValidationRule;

/**
 * Validation rule that checks if a value is a valid IPv4 address.
 *
 * <p>Usage: {@code @Rule("ip")}
 *
 * <p>Validates IPv4 address format (e.g., 192.168.1.1).
 */
public class IpRule implements ValidationRule {

    private static final String IPV4_PATTERN =
            "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}" +
            "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";

    @Override
    public String validate(String fieldName, Object value, String parameter) {
        if (value == null) {
            return null; // Let 'required' rule handle nulls
        }

        if (value instanceof String ip) {
            if (ip.matches(IPV4_PATTERN)) {
                return null; // Valid IP
            }
            return "The " + fieldName + " must be a valid IP address.";
        }

        return "The " + fieldName + " must be a valid IP address.";
    }
}
