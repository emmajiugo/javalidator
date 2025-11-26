package me.emmajiugo.javalidator.rules;

import me.emmajiugo.javalidator.annotations.Rule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static me.emmajiugo.javalidator.rules.ValidationTestHelper.assertValidation;

/**
 * Tests for basic validation rules: required, min, max, email, size, in
 */
@DisplayName("Basic Validation Rules")
class BasicValidationRulesTest {

    @Nested
    @DisplayName("Required Rule")
    class RequiredRuleTests {

        record RequiredField(
                @Rule("required")
                String username
        ) {}

        @Test
        @DisplayName("should pass with non-null value")
        void shouldPassWithNonNullValue() {
            assertValidation(new RequiredField("john"))
                    .isValid();
        }

        @Test
        @DisplayName("should fail with null value")
        void shouldFailWithNullValue() {
            assertValidation(new RequiredField(null))
                    .hasSingleError()
                    .hasErrorOn("username")
                    .withMessageContaining("required");
        }

        @Test
        @DisplayName("should fail with blank string")
        void shouldFailWithBlankString() {
            assertValidation(new RequiredField(""))
                    .hasSingleError()
                    .hasErrorOn("username")
                    .withMessageContaining("required");
        }
    }

    @Nested
    @DisplayName("Min Rule")
    class MinRuleTests {

        record MinLengthField(
                @Rule("min:3")
                String username
        ) {}

        @Test
        @DisplayName("should pass with valid length")
        void shouldPassWithValidLength() {
            assertValidation(new MinLengthField("john"))
                    .isValid();
        }

        @Test
        @DisplayName("should pass with exact min length")
        void shouldPassWithExactMinLength() {
            assertValidation(new MinLengthField("abc"))
                    .isValid();
        }

        @Test
        @DisplayName("should fail with short length")
        void shouldFailWithShortLength() {
            assertValidation(new MinLengthField("ab"))
                    .hasSingleError()
                    .hasErrorOn("username")
                    .withMessageContaining("at least 3");
        }

        @Test
        @DisplayName("should pass with null value")
        void shouldPassWithNullValue() {
            assertValidation(new MinLengthField(null))
                    .isValid();
        }
    }

    @Nested
    @DisplayName("Max Rule")
    class MaxRuleTests {

        record MaxLengthField(
                @Rule("max:10")
                String username
        ) {}

        @Test
        @DisplayName("should pass with valid length")
        void shouldPassWithValidLength() {
            assertValidation(new MaxLengthField("john"))
                    .isValid();
        }

        @Test
        @DisplayName("should pass with exact max length")
        void shouldPassWithExactMaxLength() {
            assertValidation(new MaxLengthField("1234567890"))
                    .isValid();
        }

        @Test
        @DisplayName("should fail with long length")
        void shouldFailWithLongLength() {
            assertValidation(new MaxLengthField("12345678901"))
                    .hasSingleError()
                    .hasErrorOn("username")
                    .withMessageContaining("not exceed 10");
        }

        @Test
        @DisplayName("should pass with null value")
        void shouldPassWithNullValue() {
            assertValidation(new MaxLengthField(null))
                    .isValid();
        }
    }

    @Nested
    @DisplayName("Email Rule")
    class EmailRuleTests {

        record EmailField(
                @Rule("email")
                String email
        ) {}

        @Test
        @DisplayName("should pass with valid email")
        void shouldPassWithValidEmail() {
            assertValidation(new EmailField("test@example.com"))
                    .isValid();
            assertValidation(new EmailField("user.name+tag@example.co.uk"))
                    .isValid();
        }

        @Test
        @DisplayName("should fail with invalid email")
        void shouldFailWithInvalidEmail() {
            assertValidation(new EmailField("invalid"))
                    .hasSingleError()
                    .hasErrorOn("email")
                    .withMessageContaining("email");

            assertValidation(new EmailField("@example.com"))
                    .hasSingleError();

            assertValidation(new EmailField("user@"))
                    .hasSingleError();
        }

        @Test
        @DisplayName("should pass with null value")
        void shouldPassWithNullValue() {
            assertValidation(new EmailField(null))
                    .isValid();
        }
    }

    @Nested
    @DisplayName("Size Rule")
    class SizeRuleTests {

        record SizeField(
                @Rule("size:5")
                String code
        ) {}

        @Test
        @DisplayName("should pass with exact size")
        void shouldPassWithExactSize() {
            assertValidation(new SizeField("12345"))
                    .isValid();
        }

        @Test
        @DisplayName("should fail with wrong size")
        void shouldFailWithWrongSize() {
            assertValidation(new SizeField("1234"))
                    .hasSingleError()
                    .hasErrorOn("code")
                    .withMessageContaining("exactly 5");

            assertValidation(new SizeField("123456"))
                    .hasSingleError();
        }

        @Test
        @DisplayName("should pass with null value")
        void shouldPassWithNullValue() {
            assertValidation(new SizeField(null))
                    .isValid();
        }
    }

    @Nested
    @DisplayName("In Rule")
    class InRuleTests {

        record StatusField(
                @Rule("in:active,inactive,pending")
                String status
        ) {}

        @Test
        @DisplayName("should pass with valid values")
        void shouldPassWithValidValues() {
            assertValidation(new StatusField("active"))
                    .isValid();
            assertValidation(new StatusField("inactive"))
                    .isValid();
            assertValidation(new StatusField("pending"))
                    .isValid();
        }

        @Test
        @DisplayName("should fail with invalid value")
        void shouldFailWithInvalidValue() {
            assertValidation(new StatusField("deleted"))
                    .hasSingleError()
                    .hasErrorOn("status")
                    .withMessageContaining("one of");
        }

        @Test
        @DisplayName("should fail with null value")
        void shouldFailWithNullValue() {
            // The 'in' rule currently validates null as invalid
            assertValidation(new StatusField(null))
                    .hasSingleError()
                    .hasErrorOn("status");
        }
    }

    @Nested
    @DisplayName("Combined Rules")
    class CombinedRulesTests {

        record User(
                @Rule("required|min:3|max:20")
                String username,

                @Rule("required|email")
                String email,

                @Rule("required|gte:18|lte:100")
                Integer age
        ) {}

        record UserWithCustomMessages(
                @Rule(value = "required", message = "Username is required")
                @Rule(value = "min:3", message = "Username is too short")
                @Rule(value = "max:20", message = "Username is too long")
                String username,

                @Rule(value = "required", message = "Email is required")
                @Rule(value = "email", message = "Please provide a valid email address")
                String email
        ) {}

        @Test
        @DisplayName("should pass with all valid fields")
        void shouldPassWithAllValidFields() {
            assertValidation(new User("john_doe", "john@example.com", 25))
                    .isValid();
        }

        @Test
        @DisplayName("should fail with multiple invalid fields")
        void shouldFailWithMultipleInvalidFields() {
            assertValidation(new User("jo", "invalid-email", 15))
                    .isInvalid()
                    .hasErrorCount(3)
                    .hasErrorOn("username")
                    .withMessageContaining("at least 3");
        }

        @Test
        @DisplayName("should use custom error messages")
        void shouldUseCustomErrorMessages() {
            assertValidation(new UserWithCustomMessages(null, "invalid"))
                    .isInvalid()
                    .hasErrorCount(2);

            assertValidation(new UserWithCustomMessages("jo", "invalid"))
                    .hasErrorOn("username")
                    .withMessage("Username is too short");

            assertValidation(new UserWithCustomMessages("john", "invalid"))
                    .hasErrorOn("email")
                    .withMessage("Please provide a valid email address");
        }
    }
}