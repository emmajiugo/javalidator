package io.github.emmajiugo.javalidator.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents the result of a validation operation.
 *
 * <p>Contains a boolean flag indicating whether validation passed or failed,
 * along with a list of validation errors (empty if validation passed).
 *
 * @param valid  true if validation passed, false otherwise
 * @param errors list of validation errors (empty if valid is true)
 */
public record ValidationResponse(boolean valid, List<ValidationError> errors) {

    /**
     * Creates a successful validation response with no errors.
     *
     * @return a ValidationResponse indicating successful validation
     */
    public static ValidationResponse success() {
        return new ValidationResponse(true, List.of());
    }

    /**
     * Creates a failed validation response with the given errors.
     *
     * @param errors the list of validation errors
     * @return a ValidationResponse indicating failed validation
     */
    public static ValidationResponse failure(List<ValidationError> errors) {
        return new ValidationResponse(false, errors);
    }

    /**
     * Converts validation errors to a map of field names to message lists.
     *
     * <p>If multiple {@link ValidationError} objects share the same field name,
     * their messages are merged into a single list.
     *
     * @return a map where keys are field names and values are lists of error messages
     */
    public Map<String, List<String>> toMap() {
        Map<String, List<String>> map = new LinkedHashMap<>();
        for (ValidationError error : errors) {
            map.merge(error.field(), error.messages(), (existing, incoming) -> {
                List<String> merged = new java.util.ArrayList<>(existing);
                merged.addAll(incoming);
                return merged;
            });
        }
        return map;
    }

    /**
     * Converts validation errors to a flat map of field names to first error message.
     *
     * <p>Only the first error message per field is included. Useful for simple
     * error display scenarios where one message per field is sufficient.
     *
     * @return a map where keys are field names and values are the first error message
     */
    public Map<String, String> toFlatMap() {
        Map<String, String> map = new LinkedHashMap<>();
        for (ValidationError error : errors) {
            if (!map.containsKey(error.field()) && !error.messages().isEmpty()) {
                map.put(error.field(), error.messages().get(0));
            }
        }
        return map;
    }
}