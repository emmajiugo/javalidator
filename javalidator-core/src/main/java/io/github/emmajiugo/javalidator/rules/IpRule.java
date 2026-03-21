package io.github.emmajiugo.javalidator.rules;

import io.github.emmajiugo.javalidator.ValidationRule;

import java.util.regex.Pattern;

/**
 * Validation rule that checks if a value is a valid IP address.
 *
 * <p>Usage:
 * <ul>
 *   <li>{@code @Rule("ip")} — accepts both IPv4 and IPv6</li>
 *   <li>{@code @Rule("ip:v4")} — accepts IPv4 only</li>
 *   <li>{@code @Rule("ip:v6")} — accepts IPv6 only</li>
 * </ul>
 */
public class IpRule implements ValidationRule {

    private static final String IPV4_REGEX =
            "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}" +
            "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";

    private static final String IPV6_REGEX =
            "^(" +
            "([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}|" +
            "([0-9a-fA-F]{1,4}:){1,7}:|" +
            "([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|" +
            "([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|" +
            "([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|" +
            "([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|" +
            "([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|" +
            "[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})|" +
            ":((:[0-9a-fA-F]{1,4}){1,7}|:)|" +
            "fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]+|" +
            "::(ffff(:0{1,4})?:)?((25[0-5]|(2[0-4]|1?[0-9])?[0-9])\\.){3}" +
            "(25[0-5]|(2[0-4]|1?[0-9])?[0-9])" +
            ")$";

    private static final Pattern IPV4_PATTERN = Pattern.compile(IPV4_REGEX);
    private static final Pattern IPV6_PATTERN = Pattern.compile(IPV6_REGEX);

    @Override
    public String validate(String fieldName, Object value, String parameter) {
        if (value == null) {
            return null; // Let 'required' rule handle nulls
        }

        if (value instanceof String ip) {
            boolean valid = switch (parameter == null ? "" : parameter.trim().toLowerCase()) {
                case "v4" -> IPV4_PATTERN.matcher(ip).matches();
                case "v6" -> IPV6_PATTERN.matcher(ip).matches();
                default  -> IPV4_PATTERN.matcher(ip).matches() || IPV6_PATTERN.matcher(ip).matches();
            };
            if (valid) {
                return null;
            }
            return "The " + fieldName + " must be a valid IP address.";
        }

        return "The " + fieldName + " must be a valid IP address.";
    }
}
