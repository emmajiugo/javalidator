package io.github.emmajiugo.javalidator.annotations;

import java.lang.annotation.*;

/**
 * Annotation for defining validation rules on fields or record components.
 *
 * <p>Rules can be specified using a pipe-separated format similar to Laravel's validation.
 * For example: {@code @Rule("required|min:3|max:20")}
 *
 * <p>This annotation is repeatable (Java 8+), allowing multiple rules with different
 * custom messages to be applied to the same field:
 * <pre>{@code
 * @Rule(value = "required", message = "Username is required")
 * @Rule(value = "min:3", message = "Username must be at least 3 characters")
 * String username;
 * }</pre>
 *
 * <p>For enum validation, use the type-safe enumClass parameter:
 * <pre>{@code
 * @Rule(value = "enum", enumClass = Status.class)
 * String status;
 * }</pre>
 *
 * @see Rules
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
@Repeatable(Rules.class)
public @interface Rule {
    /**
     * The validation rule(s) to apply. Multiple rules can be combined using pipe (|) separator.
     *
     * <p>Examples:
     * <ul>
     *   <li>"required" - Field must not be null or blank</li>
     *   <li>"min:3" - String must have at least 3 characters</li>
     *   <li>"required|email|max:100" - Multiple rules combined</li>
     *   <li>"enum" - Validate String against enum constants (requires enumClass parameter)</li>
     * </ul>
     *
     * @return the validation rule string
     */
    String value();

    /**
     * Custom error message to use when validation fails.
     * If not specified, a default message will be generated.
     *
     * @return the custom error message, or empty string for default message
     */
    String message() default "";

    /**
     * The enum class to validate against when using the "enum" rule.
     * This provides type-safe enum validation without arbitrary class loading.
     *
     * <p>Example:
     * <pre>{@code
     * public enum Status { ACTIVE, INACTIVE, PENDING }
     *
     * @Rule(value = "enum", enumClass = Status.class)
     * String status;  // Must be "ACTIVE", "INACTIVE", or "PENDING"
     * }</pre>
     *
     * @return the enum class, or NoEnum.class if not using enum validation
     */
    Class<? extends Enum<?>> enumClass() default NoEnum.class;

    /**
     * Marker enum to indicate no enum validation is required.
     * This is the default value for the enumClass parameter.
     */
    enum NoEnum {
        /**
         * Placeholder instance indicating no enum class was specified.
         */
        NO_ENUM
    }
}