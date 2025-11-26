package me.emmajiugo.javalidator.rules;

import me.emmajiugo.javalidator.annotations.Rule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static me.emmajiugo.javalidator.rules.ValidationTestHelper.assertValidation;

/**
 * Tests for string format rules: nullable, digits
 */
@DisplayName("String Format Rules")
class StringFormatRulesTest {

    @Nested
    @DisplayName("Nullable Rule")
    class NullableRuleTests {

        record OptionalFields(
                @Rule("nullable|email")
                String optionalEmail,

                @Rule("nullable|numeric")
                Integer optionalAge
        ) {}

        record OptionalEmailOnly(
                @Rule("nullable|email")
                String optionalEmail
        ) {}

        @Test
        @DisplayName("should pass when values are null")
        void shouldPassWhenValuesAreNull() {
            assertValidation(new OptionalFields(null, null))
                    .isValid();
        }

        @Test
        @DisplayName("should pass when values are valid")
        void shouldPassWhenValuesAreValid() {
            assertValidation(new OptionalEmailOnly("test@example.com"))
                    .isValid();
        }

        @Test
        @DisplayName("should fail when value is invalid")
        void shouldFailWhenValueIsInvalid() {
            assertValidation(new OptionalEmailOnly("invalid-email"))
                    .hasSingleError()
                    .hasErrorOn("optionalEmail");
        }
    }

    @Nested
    @DisplayName("Digits Rule")
    class DigitsRuleTests {

        record PinCode(
                @Rule("required|digits:4")
                String pin
        ) {}

        record NumericPin(
                @Rule("required|digits:4")
                Integer pin
        ) {}

        record ZipCode(
                @Rule("digits:5")
                String zip
        ) {}

        @Test
        @DisplayName("should pass with exact digit count - string")
        void shouldPassWithExactDigitCountString() {
            assertValidation(new PinCode("1234"))
                    .isValid();

            assertValidation(new PinCode("0000"))
                    .isValid();
        }

        @Test
        @DisplayName("should pass with exact digit count - numeric")
        void shouldPassWithExactDigitCountNumeric() {
            assertValidation(new NumericPin(1234))
                    .isValid();
        }

        @Test
        @DisplayName("should fail with wrong digit count")
        void shouldFailWithWrongDigitCount() {
            assertValidation(new PinCode("123"))
                    .hasSingleError()
                    .hasErrorOn("pin")
                    .withMessageContaining("exactly 4 digits");

            assertValidation(new PinCode("12345"))
                    .hasSingleError()
                    .hasErrorOn("pin")
                    .withMessageContaining("exactly 4 digits");
        }

        @Test
        @DisplayName("should fail with non-digit characters")
        void shouldFailWithNonDigitCharacters() {
            assertValidation(new PinCode("12a4"))
                    .hasSingleError()
                    .hasErrorOn("pin")
                    .withMessageContaining("contain only digits");
        }

        @Test
        @DisplayName("should pass when value is null")
        void shouldPassWhenValueIsNull() {
            assertValidation(new ZipCode(null))
                    .isValid();
        }
    }
}