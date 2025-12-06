package io.github.emmajiugo.javalidator.rules;

import io.github.emmajiugo.javalidator.annotations.Rule;
import io.github.emmajiugo.javalidator.ValidationRule;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Validation rule that checks if a String value matches a valid enum constant.
 *
 * <p><b>Security Note:</b> This rule requires the enum class to be specified via the
 * {@code enumClass} parameter in the {@code @Rule} annotation, providing type-safe
 * validation without arbitrary class loading.
 *
 * <p>Usage:
 * <pre>{@code
 * public enum Status {
 *     ACTIVE, INACTIVE, PENDING
 * }
 *
 * public record UserDTO(
 *     @Rule(value = "enum", enumClass = Status.class)
 *     String status  // Must be "ACTIVE", "INACTIVE", or "PENDING"
 * ) {}
 * }</pre>
 *
 * <p>For actual enum fields (not Strings), just use {@code @Rule("required")} -
 * Java's type system handles the validation.
 */
public class EnumRule implements ValidationRule {

    @Override
    public String validate(String fieldName, Object value, String parameter) {
        // This method should not be called directly for enum validation
        // The Validator class will call validateWithEnumClass instead
        throw new UnsupportedOperationException(
            "EnumRule requires enumClass parameter. Use @Rule(value=\"enum\", enumClass=YourEnum.class)"
        );
    }

    /**
     * Validates a value against an enum class.
     * This method is called by the Validator when enumClass is provided.
     *
     * @param fieldName the name of the field being validated
     * @param value the value to validate
     * @param enumClass the enum class to validate against
     * @return error message if validation fails, null if valid
     */
    public String validateWithEnumClass(String fieldName, Object value, Class<? extends Enum<?>> enumClass) {
        if (value == null) {
            return null; // Let 'required' rule handle nulls
        }

        // Check if enumClass was provided
        if (enumClass == Rule.NoEnum.class) {
            return "The enum rule requires an enumClass parameter. " +
                   "Use @Rule(value=\"enum\", enumClass=YourEnum.class)";
        }

        // For String values, validate against enum constants
        if (value instanceof String stringValue) {
            return validateStringAgainstEnum(fieldName, stringValue, enumClass);
        }

        // For actual enum types, just check it's an enum (type system handles this)
        if (value.getClass().isEnum()) {
            return null; // Valid enum
        }

        return "The " + fieldName + " must be a String or enum value.";
    }

    private String validateStringAgainstEnum(String fieldName, String stringValue,
                                            Class<? extends Enum<?>> enumClass) {
        // Get all enum constants - safe, no class loading needed
        Enum<?>[] enumConstants = enumClass.getEnumConstants();

        if (enumConstants == null) {
            // This shouldn't happen with proper type parameter, but handle gracefully
            return "Invalid enum class provided for " + fieldName + " validation.";
        }

        // Check if the string value matches any enum constant name
        for (Enum<?> enumConstant : enumConstants) {
            if (enumConstant.name().equals(stringValue)) {
                return null; // Valid enum value
            }
        }

        // Build error message with valid values
        String validValues = Arrays.stream(enumConstants)
                .map(Enum::name)
                .collect(Collectors.joining(", "));

        return "The " + fieldName + " must be one of: " + validValues + ".";
    }
}
