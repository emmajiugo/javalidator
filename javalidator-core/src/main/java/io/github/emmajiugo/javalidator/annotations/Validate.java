package io.github.emmajiugo.javalidator.annotations;

import java.lang.annotation.*;

/**
 * Marker annotation for automatic validation in framework adapters.
 *
 * <p>When used with framework-specific adapters (Spring, Quarkus, etc.),
 * this annotation triggers automatic validation of annotated parameters.
 *
 * <p>Example usage with Spring:
 * <pre>
 * {@code
 * @PostMapping("/users")
 * public ResponseEntity<User> createUser(@Validate @RequestBody CreateUserDTO request) {
 *     // If we reach here, validation passed
 *     return ResponseEntity.ok(userService.create(request));
 * }
 * }
 * </pre>
 *
 * <p>For manual validation without framework integration, use the
 * {@code Validator.validate()} method directly instead.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.TYPE})
public @interface Validate {
}