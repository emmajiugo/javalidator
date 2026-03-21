package io.github.emmajiugo.javalidator;

import io.github.emmajiugo.javalidator.exception.NotValidException;
import io.github.emmajiugo.javalidator.model.ValidationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Tests for {@link Validator#validateMap(Map, Map)} and related methods.
 */
@DisplayName("Validator.validateMap()")
class MapValidationTest {

    @BeforeEach
    void setUp() {
        Validator.reset();
    }

    @Nested
    @DisplayName("Flat Map Tests")
    class FlatMapTests {

        @Test
        @DisplayName("should pass when all flat map values satisfy their rules")
        void shouldPassWhenValidMapProvided() {
            Map<String, Object> data = Map.of(
                    "title", "Hello World",
                    "age", 25
            );
            Map<String, String> rules = Map.of(
                    "title", "required|min:3",
                    "age", "required|gte:18"
            );

            ValidationResponse response = Validator.validateMap(data, rules);

            assertThat(response.valid()).isTrue();
            assertThat(response.errors()).isEmpty();
        }

        @Test
        @DisplayName("should fail with correct errors when required fields are missing")
        void shouldFailWhenRequiredFieldsMissing() {
            Map<String, Object> data = new HashMap<>();
            // "title" key is absent entirely
            Map<String, String> rules = Map.of(
                    "title", "required",
                    "email", "required|email"
            );

            ValidationResponse response = Validator.validateMap(data, rules);

            assertThat(response.valid()).isFalse();
            assertThat(response.errors()).hasSize(2);
            assertThat(response.errors())
                    .extracting(e -> e.field())
                    .containsExactlyInAnyOrder("title", "email");
        }

        @Test
        @DisplayName("should work with Map<String, Object> typed data")
        void shouldWorkWithStringObjectMap() {
            Map<String, Object> data = new HashMap<>();
            data.put("username", "alice");
            data.put("score", 42);

            Map<String, String> rules = Map.of(
                    "username", "required|min:3",
                    "score", "required"
            );

            ValidationResponse response = Validator.validateMap(data, rules);

            assertThat(response.valid()).isTrue();
        }
    }

    @Nested
    @DisplayName("Nested Map Tests")
    class NestedMapTests {

        @Test
        @DisplayName("should pass when dot-notation nested values satisfy their rules")
        void shouldPassWithDotNotationSuccess() {
            Map<String, Object> address = Map.of(
                    "city", "Lagos",
                    "zip", "10001"
            );
            Map<String, Object> data = Map.of("address", address);

            Map<String, String> rules = Map.of(
                    "address.city", "required",
                    "address.zip", "required|digits:5"
            );

            ValidationResponse response = Validator.validateMap(data, rules);

            assertThat(response.valid()).isTrue();
        }

        @Test
        @DisplayName("should fail with dot-notation path in error field when nested value fails")
        void shouldFailWithDotNotationFailure() {
            Map<String, Object> address = Map.of(
                    "city", "Lagos"
                    // zip is absent
            );
            Map<String, Object> data = Map.of("address", address);

            Map<String, String> rules = Map.of(
                    "address.city", "required",
                    "address.zip", "required"
            );

            ValidationResponse response = Validator.validateMap(data, rules);

            assertThat(response.valid()).isFalse();
            assertThat(response.errors()).hasSize(1);
            assertThat(response.errors().get(0).field()).isEqualTo("address.zip");
        }

        @Test
        @DisplayName("should resolve three-level deep nesting correctly")
        void shouldResolveThreeLevelDeepNesting() {
            Map<String, Object> street = Map.of("name", "Main St");
            Map<String, Object> address = Map.of("street", street);
            Map<String, Object> data = Map.of("location", address);

            Map<String, String> rules = Map.of(
                    "location.street.name", "required|min:3"
            );

            ValidationResponse response = Validator.validateMap(data, rules);

            assertThat(response.valid()).isTrue();
        }

        @Test
        @DisplayName("should resolve to null when intermediate segment is not a Map")
        void shouldResolveToNullWhenIntermediateIsNotMap() {
            // "address" is a String, not a Map — "address.city" cannot be resolved
            Map<String, Object> data = Map.of("address", "flat string");

            Map<String, String> rules = Map.of(
                    "address.city", "required"
            );

            ValidationResponse response = Validator.validateMap(data, rules);

            assertThat(response.valid()).isFalse();
            assertThat(response.errors().get(0).field()).isEqualTo("address.city");
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("should return failure with descriptive error when data is null")
        void shouldReturnFailureWhenDataIsNull() {
            Map<String, String> rules = Map.of("title", "required");

            ValidationResponse response = Validator.validateMap(null, rules);

            assertThat(response.valid()).isFalse();
            assertThat(response.errors()).hasSize(1);
            assertThat(response.errors().get(0).field()).isEqualTo("data");
            assertThat(response.errors().get(0).messages().get(0)).contains("cannot be null");
        }

        @Test
        @DisplayName("should return success when rules map is null")
        void shouldReturnSuccessWhenRulesIsNull() {
            Map<String, Object> data = Map.of("title", "hello");

            ValidationResponse response = Validator.validateMap(data, null);

            assertThat(response.valid()).isTrue();
        }

        @Test
        @DisplayName("should return success when rules map is empty")
        void shouldReturnSuccessWhenRulesIsEmpty() {
            Map<String, Object> data = Map.of("title", "hello");

            ValidationResponse response = Validator.validateMap(data, Map.of());

            assertThat(response.valid()).isTrue();
        }

        @Test
        @DisplayName("should treat missing map key as null value for validation")
        void shouldTreatMissingKeyAsNull() {
            Map<String, Object> data = Map.of("other", "value");

            Map<String, String> rules = Map.of("title", "required");

            ValidationResponse response = Validator.validateMap(data, rules);

            assertThat(response.valid()).isFalse();
            assertThat(response.errors().get(0).field()).isEqualTo("title");
        }

        @Test
        @DisplayName("should skip rule entries whose key is blank")
        void shouldSkipBlankRuleKeys() {
            Map<String, Object> data = Map.of("title", "hello");
            Map<String, String> rules = new HashMap<>();
            rules.put("title", "required");
            rules.put("  ", "required"); // blank key — should be ignored

            ValidationResponse response = Validator.validateMap(data, rules);

            assertThat(response.valid()).isTrue();
        }

        @Test
        @DisplayName("validateMapOrThrow should throw NotValidException when validation fails")
        void validateMapOrThrowShouldThrowOnFailure() {
            Map<String, Object> data = Map.of();
            Map<String, String> rules = Map.of("name", "required");

            assertThatThrownBy(() -> Validator.validateMapOrThrow(data, rules))
                    .isInstanceOf(NotValidException.class)
                    .hasMessageContaining("Map validation failed");
        }

        @Test
        @DisplayName("validateMapOrThrow should not throw when validation succeeds")
        void validateMapOrThrowShouldNotThrowOnSuccess() {
            Map<String, Object> data = Map.of("name", "Alice");
            Map<String, String> rules = Map.of("name", "required|min:3");

            assertThatCode(() -> Validator.validateMapOrThrow(data, rules))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should stop after first error when bail is specified")
        void shouldStopAfterFirstErrorWithBail() {
            Map<String, Object> data = Map.of("score", "notanumber");
            // bail causes validation to stop at first failing rule for that field
            Map<String, String> rules = Map.of("score", "bail|numeric|gte:10");

            ValidationResponse response = Validator.validateMap(data, rules);

            assertThat(response.valid()).isFalse();
            // With bail, only the first failing rule's message should be present
            assertThat(response.errors().get(0).messages()).hasSize(1);
        }

        @Test
        @DisplayName("should populate the rules field in ValidationError")
        void shouldPopulateRulesFieldInError() {
            Map<String, Object> data = Map.of("email", "not-an-email");
            Map<String, String> rules = Map.of("email", "required|email");

            ValidationResponse response = Validator.validateMap(data, rules);

            assertThat(response.valid()).isFalse();
            assertThat(response.errors().get(0).rules()).isNotEmpty();
            assertThat(response.errors().get(0).rules()).contains("email");
        }
    }
}
