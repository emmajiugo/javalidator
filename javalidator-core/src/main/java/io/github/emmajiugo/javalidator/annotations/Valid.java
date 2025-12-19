package io.github.emmajiugo.javalidator.annotations;

import java.lang.annotation.*;

/**
 * Marks a method parameter for cascaded validation.
 *
 * <p>When applied to a method parameter, triggers validation of the object's
 * internal fields annotated with {@link Rule}. This follows the Jakarta Bean
 * Validation {@code @Valid} convention.
 *
 * <p>Example usage with Spring:
 * <pre>
 * {@code
 * @PostMapping("/users")
 * public ResponseEntity<User> createUser(@Valid @RequestBody CreateUserDTO request) {
 *     // If we reach here, validation passed
 *     return ResponseEntity.ok(userService.create(request));
 * }
 * }
 * </pre>
 *
 * <p>For manual validation without framework integration, use the
 * {@code Validator.validate()} method directly instead.
 *
 * @see Validated
 * @see Rule
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Valid {
}