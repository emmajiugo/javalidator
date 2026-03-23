package io.github.emmajiugo.javalidator.rules;

import io.github.emmajiugo.javalidator.Validator;
import io.github.emmajiugo.javalidator.annotations.Rule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.github.emmajiugo.javalidator.rules.ValidationTestHelper.assertValidation;
import static org.assertj.core.api.Assertions.assertThat;

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
                    .withMessageContaining("required")
                    .hasRule("required");
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
                    .withMessageContaining("at least 3")
                    .hasRule("min");
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
                    .withMessageContaining("email")
                    .hasRule("email");

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

        @Test
        @DisplayName("should reject leading dot in local part")
        void shouldRejectLeadingDot() {
            assertValidation(new EmailField(".user@example.com")).hasSingleError();
        }

        @Test
        @DisplayName("should reject trailing dot in local part")
        void shouldRejectTrailingDot() {
            assertValidation(new EmailField("user.@example.com")).hasSingleError();
        }

        @Test
        @DisplayName("should reject consecutive dots in local part")
        void shouldRejectConsecutiveDots() {
            assertValidation(new EmailField("user..name@example.com")).hasSingleError();
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

        record InField(
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
        @DisplayName("should pass with null value (let required handle nulls)")
        void shouldPassWithNullValue() {
            assertValidation(new InField(null))
                    .isValid();
        }
    }

    @Nested
    @DisplayName("Min Rule — Collections")
    class MinRuleCollectionTests {

        record MinListField(
                @Rule("min:2")
                List<String> tags
        ) {}

        record MinArrayField(
                @Rule("min:2")
                String[] items
        ) {}

        record MinNumberField(
                @Rule("min:5")
                Integer count
        ) {}

        @Test
        @DisplayName("should pass when list size meets minimum")
        void shouldPassWhenListSizeMeetsMinimum() {
            assertValidation(new MinListField(List.of("a", "b")))
                    .isValid();
            assertValidation(new MinListField(List.of("a", "b", "c")))
                    .isValid();
        }

        @Test
        @DisplayName("should fail when list size is below minimum")
        void shouldFailWhenListSizeBelowMinimum() {
            assertValidation(new MinListField(List.of("a")))
                    .hasSingleError()
                    .hasErrorOn("tags")
                    .withMessageContaining("at least 2 items");
        }

        @Test
        @DisplayName("should pass when array length meets minimum")
        void shouldPassWhenArrayLengthMeetsMinimum() {
            assertValidation(new MinArrayField(new String[]{"x", "y"}))
                    .isValid();
        }

        @Test
        @DisplayName("should fail when array length is below minimum")
        void shouldFailWhenArrayLengthBelowMinimum() {
            assertValidation(new MinArrayField(new String[]{"x"}))
                    .hasSingleError()
                    .hasErrorOn("items")
                    .withMessageContaining("at least 2 items");
        }

        @Test
        @DisplayName("should return helpful message for Number type")
        void shouldReturnHelpfulMessageForNumber() {
            assertValidation(new MinNumberField(3))
                    .hasSingleError()
                    .hasErrorOn("count")
                    .withMessageContaining("gte");
        }

        @Test
        @DisplayName("should pass with null value")
        void shouldPassWithNullValue() {
            assertValidation(new MinListField(null))
                    .isValid();
        }
    }

    @Nested
    @DisplayName("Max Rule — Collections")
    class MaxRuleCollectionTests {

        record MaxListField(
                @Rule("max:3")
                List<String> tags
        ) {}

        record MaxArrayField(
                @Rule("max:3")
                String[] items
        ) {}

        record MaxNumberField(
                @Rule("max:10")
                Integer count
        ) {}

        @Test
        @DisplayName("should pass when list size is within maximum")
        void shouldPassWhenListSizeWithinMaximum() {
            assertValidation(new MaxListField(List.of("a", "b", "c")))
                    .isValid();
            assertValidation(new MaxListField(List.of("a")))
                    .isValid();
        }

        @Test
        @DisplayName("should fail when list size exceeds maximum")
        void shouldFailWhenListSizeExceedsMaximum() {
            assertValidation(new MaxListField(List.of("a", "b", "c", "d")))
                    .hasSingleError()
                    .hasErrorOn("tags")
                    .withMessageContaining("more than 3 items");
        }

        @Test
        @DisplayName("should pass when array length is within maximum")
        void shouldPassWhenArrayLengthWithinMaximum() {
            assertValidation(new MaxArrayField(new String[]{"x", "y"}))
                    .isValid();
        }

        @Test
        @DisplayName("should fail when array length exceeds maximum")
        void shouldFailWhenArrayLengthExceedsMaximum() {
            assertValidation(new MaxArrayField(new String[]{"x", "y", "z", "w"}))
                    .hasSingleError()
                    .hasErrorOn("items")
                    .withMessageContaining("more than 3 items");
        }

        @Test
        @DisplayName("should return helpful message for Number type")
        void shouldReturnHelpfulMessageForNumber() {
            assertValidation(new MaxNumberField(15))
                    .hasSingleError()
                    .hasErrorOn("count")
                    .withMessageContaining("lte");
        }

        @Test
        @DisplayName("should pass with null value")
        void shouldPassWithNullValue() {
            assertValidation(new MaxListField(null))
                    .isValid();
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

    @Nested
    @DisplayName("Rules Field Enrichment")
    class RulesFieldTests {

        record MultiRuleField(
                @Rule(value = "required|min:3", message = "Field is invalid")
                String name
        ) {}

        @Test
        @DisplayName("should populate rules even with custom message")
        void shouldPopulateRulesWithCustomMessage() {
            var response = Validator.validate(new MultiRuleField(""));
            assertThat(response.valid()).isFalse();
            var error = response.errors().get(0);
            // Custom message used once
            assertThat(error.messages()).containsExactly("Field is invalid");
            // But rules captures all failed rule names
            assertThat(error.rules()).contains("required", "min");
        }
    }
}