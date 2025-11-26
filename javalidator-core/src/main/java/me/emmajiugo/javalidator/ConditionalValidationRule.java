package me.emmajiugo.javalidator;

/**
 * Extended validation rule interface for rules that need access to the entire DTO object.
 *
 * <p>This interface is used for conditional validation rules that need to inspect
 * other fields in the DTO to determine validation logic.
 *
 * <p>Examples:
 * <ul>
 *     <li>{@code required_if} - Field required if another field has specific value</li>
 *     <li>{@code required_unless} - Field required unless another field has specific value</li>
 *     <li>{@code confirmed} - Field must match its confirmation field</li>
 * </ul>
 *
 * <p>Example implementation:
 * <pre>
 * {@code
 * public class RequiredIfRule implements ConditionalValidationRule {
 *     @Override
 *     public String validateWithContext(String fieldName, Object value, String parameter, Object dto) {
 *         // Parse parameter to get other field name and expected value
 *         // Access other field from dto using reflection
 *         // Check condition and validate current field
 *         return null; // or error message
 *     }
 * }
 * }
 * </pre>
 */
public interface ConditionalValidationRule extends ValidationRule {

    /**
     * Validates a field value with access to the entire DTO object.
     *
     * @param fieldName the name of the field being validated
     * @param value     the value to validate
     * @param parameter the parameter for the rule (e.g., "country,USA" in "required_if:country,USA")
     * @param dto       the entire DTO object being validated
     * @return error message if validation fails, null if validation passes
     */
    String validateWithContext(String fieldName, Object value, String parameter, Object dto);

    /**
     * Default implementation that indicates this rule needs context.
     * This will never be called in practice as the Validator detects ConditionalValidationRule
     * and calls validateWithContext instead.
     */
    @Override
    default String validate(String fieldName, Object value, String parameter) {
        return "This conditional rule requires the full DTO context.";
    }
}
