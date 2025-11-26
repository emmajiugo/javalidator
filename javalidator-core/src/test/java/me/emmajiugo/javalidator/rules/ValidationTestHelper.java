package me.emmajiugo.javalidator.rules;

import me.emmajiugo.javalidator.Validator;
import me.emmajiugo.javalidator.model.ValidationError;
import me.emmajiugo.javalidator.model.ValidationResponse;
import org.assertj.core.api.AbstractAssert;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Helper class for validation testing with fluent assertions.
 */
public class ValidationTestHelper {

    /**
     * Validates an object and returns a fluent assertion helper.
     */
    public static ValidationAssert assertValidation(Object dto) {
        ValidationResponse response = Validator.validate(dto);
        return new ValidationAssert(response);
    }

    /**
     * Fluent assertion helper for ValidationResponse.
     */
    public static class ValidationAssert extends AbstractAssert<ValidationAssert, ValidationResponse> {

        public ValidationAssert(ValidationResponse actual) {
            super(actual, ValidationAssert.class);
        }

        /**
         * Asserts that validation passed (no errors).
         */
        public ValidationAssert isValid() {
            isNotNull();
            assertThat(actual.valid())
                    .withFailMessage("Expected validation to pass, but it failed with errors: %s", actual.errors())
                    .isTrue();
            return this;
        }

        /**
         * Asserts that validation failed (has errors).
         */
        public ValidationAssert isInvalid() {
            isNotNull();
            assertThat(actual.valid())
                    .withFailMessage("Expected validation to fail, but it passed")
                    .isFalse();
            return this;
        }

        /**
         * Asserts that validation has exactly the specified number of errors.
         */
        public ValidationAssert hasErrorCount(int count) {
            isInvalid();
            assertThat(actual.errors())
                    .withFailMessage("Expected %d errors, but got %d: %s", count, actual.errors().size(), actual.errors())
                    .hasSize(count);
            return this;
        }

        /**
         * Asserts that validation has exactly one error.
         */
        public ValidationAssert hasSingleError() {
            return hasErrorCount(1);
        }

        /**
         * Asserts that the specified field has validation errors.
         */
        public FieldErrorAssert hasErrorOn(String fieldName) {
            isInvalid();
            List<ValidationError> fieldErrors = actual.errors().stream()
                    .filter(error -> error.field().equals(fieldName))
                    .toList();

            assertThat(fieldErrors)
                    .withFailMessage("Expected errors on field '%s', but found none. All errors: %s", fieldName, actual.errors())
                    .isNotEmpty();

            return new FieldErrorAssert(fieldErrors.get(0));
        }

        /**
         * Asserts that validation has no errors on the specified field.
         */
        public ValidationAssert hasNoErrorOn(String fieldName) {
            if (!actual.valid()) {
                boolean hasFieldError = actual.errors().stream()
                        .anyMatch(error -> error.field().equals(fieldName));

                assertThat(hasFieldError)
                        .withFailMessage("Expected no errors on field '%s', but found: %s",
                                fieldName, actual.errors())
                        .isFalse();
            }
            return this;
        }
    }

    /**
     * Fluent assertion helper for field-specific validation errors.
     */
    public static class FieldErrorAssert extends AbstractAssert<FieldErrorAssert, ValidationError> {

        public FieldErrorAssert(ValidationError actual) {
            super(actual, FieldErrorAssert.class);
        }

        /**
         * Asserts that the error message contains the specified text.
         */
        public FieldErrorAssert withMessageContaining(String text) {
            isNotNull();
            assertThat(actual.messages())
                    .withFailMessage("Expected error message to contain '%s', but messages were: %s", text, actual.messages())
                    .anyMatch(msg -> msg.contains(text));
            return this;
        }

        /**
         * Asserts that the error has exactly the specified number of messages.
         */
        public FieldErrorAssert withMessageCount(int count) {
            isNotNull();
            assertThat(actual.messages())
                    .withFailMessage("Expected %d messages, but got %d: %s", count, actual.messages().size(), actual.messages())
                    .hasSize(count);
            return this;
        }

        /**
         * Asserts that the error has exactly one message.
         */
        public FieldErrorAssert withSingleMessage() {
            return withMessageCount(1);
        }

        /**
         * Asserts that the first error message matches the specified text.
         */
        public FieldErrorAssert withMessage(String expectedMessage) {
            isNotNull();
            assertThat(actual.messages())
                    .withFailMessage("Expected first message to be '%s', but was: %s", expectedMessage, actual.messages())
                    .isNotEmpty()
                    .first()
                    .isEqualTo(expectedMessage);
            return this;
        }
    }
}