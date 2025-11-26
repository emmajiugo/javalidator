package me.emmajiugo.javalidator.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark fields/parameters for cascading validation.
 *
 * <p>When applied to a field, the validator will recursively validate the nested object
 * by cascading through its structure. This works with:
 * <ul>
 *   <li>Nested objects (records, classes)</li>
 *   <li>Collections (List, Set, etc.)</li>
 *   <li>Arrays</li>
 * </ul>
 *
 * <p>Example usage:
 * <pre>
 * {@code
 * record User(
 *     @Rule("required")
 *     String name,
 *
 *     @RuleCascade
 *     Address address,
 *
 *     @RuleCascade
 *     List<PhoneNumber> phoneNumbers
 * ) {}
 *
 * record Address(
 *     @Rule("required")
 *     String street,
 *
 *     @Rule("required")
 *     String city
 * ) {}
 *
 * record PhoneNumber(
 *     @Rule("required|digits:10")
 *     String number
 * ) {}
 * }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
public @interface RuleCascade {
}