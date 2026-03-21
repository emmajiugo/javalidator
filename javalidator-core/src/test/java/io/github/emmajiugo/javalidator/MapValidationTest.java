package io.github.emmajiugo.javalidator;

import io.github.emmajiugo.javalidator.model.ValidationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

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
}
