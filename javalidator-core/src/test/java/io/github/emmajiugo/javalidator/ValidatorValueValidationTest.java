package io.github.emmajiugo.javalidator;

import io.github.emmajiugo.javalidator.exception.ValidationException;
import io.github.emmajiugo.javalidator.model.ValidationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for {@link Validator#validateValue(Object, String, String)} method.
 */
@DisplayName("Validator.validateValue()")
class ValidatorValueValidationTest {

    @BeforeEach
    void setUp() {
        Validator.reset();
    }

    @Nested
    @DisplayName("Basic Value Validation")
    class BasicValueValidation {

        @Test
        @DisplayName("should pass with valid string value")
        void shouldPassWithValidString() {
            ValidationResponse response = Validator.validateValue("hello", "required|min:3", "name");

            assertThat(response.valid()).isTrue();
            assertThat(response.errors()).isEmpty();
        }

        @Test
        @DisplayName("should fail with null value when required")
        void shouldFailWithNullValue() {
            ValidationResponse response = Validator.validateValue(null, "required", "name");

            assertThat(response.valid()).isFalse();
            assertThat(response.errors()).hasSize(1);
            assertThat(response.errors().get(0).field()).isEqualTo("name");
            assertThat(response.errors().get(0).messages()).anyMatch(msg -> msg.contains("required"));
        }

        @Test
        @DisplayName("should fail with value too short")
        void shouldFailWithValueTooShort() {
            ValidationResponse response = Validator.validateValue("ab", "min:3", "username");

            assertThat(response.valid()).isFalse();
            assertThat(response.errors()).hasSize(1);
            assertThat(response.errors().get(0).field()).isEqualTo("username");
            assertThat(response.errors().get(0).messages()).anyMatch(msg -> msg.contains("3"));
        }

        @Test
        @DisplayName("should pass with null/empty rules")
        void shouldPassWithNullOrEmptyRules() {
            assertThat(Validator.validateValue("value", null, "field").valid()).isTrue();
            assertThat(Validator.validateValue("value", "", "field").valid()).isTrue();
            assertThat(Validator.validateValue("value", "  ", "field").valid()).isTrue();
        }
    }

    @Nested
    @DisplayName("Multiple Rules Validation")
    class MultipleRulesValidation {

        @Test
        @DisplayName("should validate multiple rules on a value")
        void shouldValidateMultipleRules() {
            ValidationResponse response = Validator.validateValue("john@example.com", "required|email|max:100", "email");

            assertThat(response.valid()).isTrue();
        }

        @Test
        @DisplayName("should collect all errors from multiple rules")
        void shouldCollectAllErrors() {
            // "ab" fails min:5 (too short) and numeric (not a number)
            ValidationResponse response = Validator.validateValue("ab", "min:5|numeric", "value");

            assertThat(response.valid()).isFalse();
            assertThat(response.errors()).hasSize(1);
            // Should have 2 error messages on the same field
            assertThat(response.errors().get(0).messages()).hasSize(2);
        }

        @Test
        @DisplayName("should fail if any rule fails")
        void shouldFailIfAnyRuleFails() {
            // "hello" passes min:3 but fails email
            ValidationResponse response = Validator.validateValue("hello", "min:3|email", "field");

            assertThat(response.valid()).isFalse();
            assertThat(response.errors().get(0).messages()).anyMatch(msg -> msg.toLowerCase().contains("email"));
        }
    }

    @Nested
    @DisplayName("Numeric Value Validation")
    class NumericValueValidation {

        @Test
        @DisplayName("should validate numeric constraints on Integer")
        void shouldValidateIntegerValue() {
            ValidationResponse response = Validator.validateValue(25, "gte:18|lte:100", "age");

            assertThat(response.valid()).isTrue();
        }

        @Test
        @DisplayName("should fail when number is too small")
        void shouldFailWhenNumberTooSmall() {
            ValidationResponse response = Validator.validateValue(5, "gte:10", "count");

            assertThat(response.valid()).isFalse();
            assertThat(response.errors().get(0).field()).isEqualTo("count");
        }

        @Test
        @DisplayName("should validate Long value")
        void shouldValidateLongValue() {
            ValidationResponse response = Validator.validateValue(1L, "gte:1", "id");

            assertThat(response.valid()).isTrue();
        }

        @Test
        @DisplayName("should fail when Long value is less than minimum")
        void shouldFailWhenLongTooSmall() {
            ValidationResponse response = Validator.validateValue(0L, "gte:1", "id");

            assertThat(response.valid()).isFalse();
            assertThat(response.errors().get(0).field()).isEqualTo("id");
        }
    }

    @Nested
    @DisplayName("Conditional Rules Handling")
    class ConditionalRulesHandling {

        @Test
        @DisplayName("should throw exception for required_if rule")
        void shouldThrowForRequiredIfRule() {
            assertThatThrownBy(() -> Validator.validateValue("test", "required_if:other,value", "field"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("requires DTO context");
        }

        @Test
        @DisplayName("should throw exception for required_unless rule")
        void shouldThrowForRequiredUnlessRule() {
            assertThatThrownBy(() -> Validator.validateValue("test", "required_unless:other,value", "field"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("requires DTO context");
        }

        @Test
        @DisplayName("should throw exception for same rule")
        void shouldThrowForSameRule() {
            assertThatThrownBy(() -> Validator.validateValue("test", "same:other", "field"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("requires DTO context");
        }

        @Test
        @DisplayName("should throw exception for different rule")
        void shouldThrowForDifferentRule() {
            assertThatThrownBy(() -> Validator.validateValue("test", "different:other", "field"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("requires DTO context");
        }
    }

    @Nested
    @DisplayName("Enum Rule Handling")
    class EnumRuleHandling {

        @Test
        @DisplayName("should throw exception for enum rule")
        void shouldThrowForEnumRule() {
            assertThatThrownBy(() -> Validator.validateValue("ACTIVE", "enum", "status"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("enum")
                    .hasMessageContaining("enumClass");
        }
    }

    @Nested
    @DisplayName("validateValueOrThrow()")
    class ValidateValueOrThrow {

        @Test
        @DisplayName("should not throw when validation passes")
        void shouldNotThrowWhenValid() {
            assertThatCode(() -> Validator.validateValueOrThrow("hello", "required|min:3", "name"))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should throw ValidationException when validation fails")
        void shouldThrowWhenInvalid() {
            assertThatThrownBy(() -> Validator.validateValueOrThrow(null, "required", "name"))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("name");
        }

        @Test
        @DisplayName("should include field name in exception message")
        void shouldIncludeFieldNameInException() {
            try {
                Validator.validateValueOrThrow("ab", "min:5", "username");
                fail("Expected ValidationException");
            } catch (ValidationException e) {
                assertThat(e.getMessage()).contains("username");
                assertThat(e.getErrors()).hasSize(1);
                assertThat(e.getErrors().get(0).field()).isEqualTo("username");
            }
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("should handle empty string in rules (multiple pipes)")
        void shouldHandleEmptyRulesInPipe() {
            ValidationResponse response = Validator.validateValue("test", "required||min:2", "field");

            assertThat(response.valid()).isTrue();
        }

        @Test
        @DisplayName("should handle whitespace in rules")
        void shouldHandleWhitespaceInRules() {
            ValidationResponse response = Validator.validateValue("test", "required | min:2 | max:10", "field");

            assertThat(response.valid()).isTrue();
        }

        @Test
        @DisplayName("should validate with custom field name")
        void shouldUseCustomFieldName() {
            ValidationResponse response = Validator.validateValue(null, "required", "customFieldName");

            assertThat(response.valid()).isFalse();
            assertThat(response.errors().get(0).field()).isEqualTo("customFieldName");
            assertThat(response.errors().get(0).messages().get(0)).contains("customFieldName");
        }
    }
}