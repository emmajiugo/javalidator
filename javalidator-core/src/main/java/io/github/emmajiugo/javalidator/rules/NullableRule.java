package io.github.emmajiugo.javalidator.rules;

import io.github.emmajiugo.javalidator.ValidationRule;

/**
 * Validation rule that explicitly allows null values.
 *
 * <p>Usage: {@code @Rule("nullable")}
 *
 * <p>This rule serves as a marker to indicate that a field is allowed to be null,
 * which can be useful for documentation purposes and making validation intent explicit.
 *
 * <p>By default, most validation rules skip validation when a field is null,
 * and only the "required" rule enforces non-null values. The "nullable" rule
 * makes this behavior explicit and can be used in combination with other rules.
 *
 * <p>Example use cases:
 * <ul>
 *   <li>Optional email: {@code @Rule("nullable") @Rule("email")}</li>
 *   <li>Optional numeric field: {@code @Rule("nullable") @Rule("numeric")}</li>
 *   <li>Making intent clear: {@code @Rule("nullable") @Rule("min:10")}</li>
 * </ul>
 *
 * <p>Note: This rule always passes validation. Its primary purpose is to
 * document that null values are explicitly allowed for a field.
 */
public class NullableRule implements ValidationRule {

    @Override
    public String validate(String fieldName, Object value, String parameter) {
        // This rule always passes - it's a marker to indicate null is allowed
        return null;
    }

    @Override
    public String getName() {
        return "nullable";
    }
}