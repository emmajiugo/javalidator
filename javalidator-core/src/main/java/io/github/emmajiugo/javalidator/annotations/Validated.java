package io.github.emmajiugo.javalidator.annotations;

import java.lang.annotation.*;

/**
 * Enables method-level parameter validation for annotated classes.
 *
 * <p>When applied at the class level, this annotation enables validation of
 * method parameters that are annotated with {@link Valid} or {@link Rule}.
 * This follows Spring's {@code @Validated} convention.
 *
 * <p>For {@code @RestController} classes, validation is enabled by default
 * without requiring this annotation (for backward compatibility). This
 * annotation is required for enabling validation in {@code @Service},
 * {@code @Component}, and other Spring beans.
 *
 * <p>Example usage:
 * <pre>
 * {@code
 * @Validated
 * @Service
 * public class UserService {
 *
 *     public User findById(@Rule("min:1") Long id) {
 *         // id is validated directly (must be >= 1)
 *         return userRepository.findById(id).orElseThrow();
 *     }
 *
 *     public User createUser(@Valid CreateUserRequest request) {
 *         // request object's fields are validated (cascaded)
 *         return userRepository.save(toEntity(request));
 *     }
 * }
 * }
 * </pre>
 *
 * @see Valid
 * @see Rule
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Validated {
}