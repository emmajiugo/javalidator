package io.github.emmajiugo.javalidator.annotations;

import java.lang.annotation.*;

/**
 * Container annotation for multiple {@link Rule} annotations.
 *
 * <p>This annotation is automatically used by Java when multiple {@code @Rule}
 * annotations are applied to the same element. You don't need to use this annotation
 * directly - just repeat {@code @Rule} annotations as needed.
 *
 * <p>Example (you write this):
 * <pre>{@code
 * @Rule(value = "required", message = "Username is required")
 * @Rule(value = "min:3", message = "Username is too short")
 * String username;
 * }</pre>
 *
 * <p>Java automatically handles this as:
 * <pre>{@code
 * @Rules({
 *     @Rule(value = "required", message = "Username is required"),
 *     @Rule(value = "min:3", message = "Username is too short")
 * })
 * String username;
 * }</pre>
 *
 * @see Rule
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.RECORD_COMPONENT, ElementType.PARAMETER})
public @interface Rules {
    /**
     * Array of Rule annotations.
     *
     * @return the array of rules
     */
    Rule[] value();
}