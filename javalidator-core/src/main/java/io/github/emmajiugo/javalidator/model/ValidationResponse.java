package io.github.emmajiugo.javalidator.model;

import java.util.List;

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
}