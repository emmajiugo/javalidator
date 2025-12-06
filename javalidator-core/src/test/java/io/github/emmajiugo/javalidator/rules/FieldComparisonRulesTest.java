package io.github.emmajiugo.javalidator.rules;

import io.github.emmajiugo.javalidator.annotations.Rule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.github.emmajiugo.javalidator.rules.ValidationTestHelper.assertValidation;

/**
 * Tests for field comparison rules: same, different
 */
@DisplayName("Field Comparison Rules")
class FieldComparisonRulesTest {

    @Nested
    @DisplayName("Same Rule")
    class SameRuleTests {

        record EmailConfirmation(
                @Rule("required|email")
                String email,

                @Rule("required|same:email")
                String emailConfirmation
        ) {}

        record PasswordConfirmation(
                String password,

                @Rule("same:password")
                String passwordConfirmation
        ) {}

        @Test
        @DisplayName("should pass when fields match")
        void shouldPassWhenFieldsMatch() {
            assertValidation(new EmailConfirmation("test@example.com", "test@example.com"))
                    .isValid();
        }

        @Test
        @DisplayName("should fail when fields don't match")
        void shouldFailWhenFieldsDontMatch() {
            assertValidation(new EmailConfirmation("test@example.com", "different@example.com"))
                    .hasSingleError()
                    .hasErrorOn("emailConfirmation")
                    .withMessageContaining("must match email");
        }

        @Test
        @DisplayName("should pass when value is null")
        void shouldPassWhenValueIsNull() {
            assertValidation(new PasswordConfirmation("password123", null))
                    .isValid();
        }
    }

    @Nested
    @DisplayName("Different Rule")
    class DifferentRuleTests {

        record PasswordChange(
                @Rule("required")
                String oldPassword,

                @Rule("required|different:oldPassword")
                String newPassword
        ) {}

        record AlternativeEmail(
                String primaryEmail,

                @Rule("different:primaryEmail")
                String alternativeEmail
        ) {}

        @Test
        @DisplayName("should pass when fields differ")
        void shouldPassWhenFieldsDiffer() {
            assertValidation(new PasswordChange("old123", "new456"))
                    .isValid();
        }

        @Test
        @DisplayName("should fail when fields are the same")
        void shouldFailWhenFieldsAreSame() {
            assertValidation(new PasswordChange("password123", "password123"))
                    .hasSingleError()
                    .hasErrorOn("newPassword")
                    .withMessageContaining("must be different from oldPassword");
        }

        @Test
        @DisplayName("should pass when value is null")
        void shouldPassWhenValueIsNull() {
            assertValidation(new AlternativeEmail("primary@example.com", null))
                    .isValid();
        }
    }
}