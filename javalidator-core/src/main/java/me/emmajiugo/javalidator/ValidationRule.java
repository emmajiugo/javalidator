package me.emmajiugo.javalidator;

/**
 * Interface for validation rule implementations.
 *
 * <p>Each validation rule implements this interface to provide custom validation logic.
 * Rules are registered in the RuleRegistry and can be easily added or removed.
 *
 * <p>Example implementation:
 * <pre>
 * {@code
 * public class RequiredRule implements ValidationRule {
 *     @Override
 *     public String validate(String fieldName, Object value, String parameter) {
 *         if (value == null || (value instanceof String s && s.isBlank())) {
 *             return "The " + fieldName + " field is required.";
 *         }
 *         return null;
 *     }
 * }
 * }
 * </pre>
 */
public interface ValidationRule {

    /**
     * Validates a field value according to the rule's logic.
     *
     * @param fieldName the name of the field being validated
     * @param value     the value to validate
     * @param parameter the parameter for the rule (e.g., "3" in "min:3"), may be null
     * @return error message if validation fails, null if validation passes
     */
    String validate(String fieldName, Object value, String parameter);

    /**
     * Gets the name/key of this rule (e.g., "required", "min", "email").
     * Default implementation extracts from class name (e.g., RequiredRule -> "required").
     *
     * @return the rule name
     */
    default String getName() {
        String className = this.getClass().getSimpleName();
        if (className.endsWith("Rule")) {
            className = className.substring(0, className.length() - 4);
        }
        return className.toLowerCase();
    }
}