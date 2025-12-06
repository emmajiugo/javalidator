package io.github.emmajiugo.javalidator.exception;

import io.github.emmajiugo.javalidator.model.ValidationError;

import java.util.List;

/**
 * Exception thrown when validation fails.
 *
 * <p>This exception contains the list of validation errors that occurred
 * during the validation process. It can be used in service layers or caught
 * by exception handlers in framework-specific adapters.
 *
 * <p>Example usage:
 * <pre>
 * {@code
 * ValidationResponse validation = Validator.validate(dto);
 * if (!validation.valid()) {
 *     throw new ValidationException(validation.errors());
 * }
 * }
 * </pre>
 */
public class ValidationException extends RuntimeException {
    private final List<ValidationError> errors;

    /**
     * Creates a new ValidationException with the given validation errors.
     *
     * @param errors the list of validation errors
     */
    public ValidationException(List<ValidationError> errors) {
        super("Validation failed");
        this.errors = errors;
    }

    /**
     * Creates a new ValidationException with a custom message and validation errors.
     *
     * @param message the custom exception message
     * @param errors  the list of validation errors
     */
    public ValidationException(String message, List<ValidationError> errors) {
        super(message);
        this.errors = errors;
    }

    /**
     * Gets the list of validation errors.
     *
     * @return the list of validation errors
     */
    public List<ValidationError> getErrors() {
        return errors;
    }
}