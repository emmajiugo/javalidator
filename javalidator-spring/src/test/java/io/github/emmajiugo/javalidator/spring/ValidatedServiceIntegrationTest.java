package io.github.emmajiugo.javalidator.spring;

import io.github.emmajiugo.javalidator.annotations.Rule;
import io.github.emmajiugo.javalidator.annotations.Valid;
import io.github.emmajiugo.javalidator.annotations.Validated;
import io.github.emmajiugo.javalidator.exception.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for @Validated service layer validation.
 */
@SpringBootTest(classes = ValidatedServiceIntegrationTest.TestConfig.class)
@DisplayName("@Validated Service Layer Validation")
class ValidatedServiceIntegrationTest {

    @Autowired
    private TestUserService userService;

    @Nested
    @DisplayName("@Valid cascaded validation")
    class ValidCascadedValidation {

        @Test
        @DisplayName("should pass when DTO is valid")
        void shouldPassWhenDtoIsValid() {
            TestUserDTO dto = new TestUserDTO("johndoe", "john@example.com", 25);

            String result = userService.createUser(dto);

            assertThat(result).isEqualTo("Created: johndoe");
        }

        @Test
        @DisplayName("should throw ValidationException when DTO fields are invalid")
        void shouldThrowWhenDtoFieldsInvalid() {
            TestUserDTO dto = new TestUserDTO("ab", "invalid-email", 15);

            assertThatThrownBy(() -> userService.createUser(dto))
                    .isInstanceOf(ValidationException.class)
                    .satisfies(ex -> {
                        ValidationException ve = (ValidationException) ex;
                        assertThat(ve.getErrors()).isNotEmpty();
                    });
        }

        @Test
        @DisplayName("should throw ValidationException when required field is null")
        void shouldThrowWhenRequiredFieldIsNull() {
            TestUserDTO dto = new TestUserDTO(null, "john@example.com", 25);

            assertThatThrownBy(() -> userService.createUser(dto))
                    .isInstanceOf(ValidationException.class);
        }
    }

    @Nested
    @DisplayName("@Rule parameter validation")
    class RuleParameterValidation {

        @Test
        @DisplayName("should pass when parameter meets rule constraints")
        void shouldPassWhenParameterValid() {
            String result = userService.findById(1L);

            assertThat(result).isEqualTo("Found user: 1");
        }

        @Test
        @DisplayName("should throw ValidationException when parameter violates rule")
        void shouldThrowWhenParameterViolatesRule() {
            assertThatThrownBy(() -> userService.findById(0L))
                    .isInstanceOf(ValidationException.class)
                    .satisfies(ex -> {
                        ValidationException ve = (ValidationException) ex;
                        assertThat(ve.getErrors()).hasSize(1);
                        assertThat(ve.getErrors().get(0).field()).isEqualTo("id");
                    });
        }

        @Test
        @DisplayName("should throw ValidationException when parameter is negative")
        void shouldThrowWhenParameterNegative() {
            assertThatThrownBy(() -> userService.findById(-5L))
                    .isInstanceOf(ValidationException.class);
        }
    }

    @Nested
    @DisplayName("Combined @Valid and @Rule validation")
    class CombinedValidation {

        @Test
        @DisplayName("should pass when both id and DTO are valid")
        void shouldPassWhenBothValid() {
            TestUserDTO dto = new TestUserDTO("johndoe", "john@example.com", 25);

            String result = userService.updateUser(1L, dto);

            assertThat(result).isEqualTo("Updated user 1: johndoe");
        }

        @Test
        @DisplayName("should throw when id is invalid")
        void shouldThrowWhenIdInvalid() {
            TestUserDTO dto = new TestUserDTO("johndoe", "john@example.com", 25);

            assertThatThrownBy(() -> userService.updateUser(0L, dto))
                    .isInstanceOf(ValidationException.class);
        }

        @Test
        @DisplayName("should throw when DTO is invalid")
        void shouldThrowWhenDtoInvalid() {
            TestUserDTO dto = new TestUserDTO("ab", "invalid", 10);

            assertThatThrownBy(() -> userService.updateUser(1L, dto))
                    .isInstanceOf(ValidationException.class);
        }

        @Test
        @DisplayName("should collect all validation errors")
        void shouldCollectAllErrors() {
            TestUserDTO dto = new TestUserDTO("ab", "invalid", 10);

            assertThatThrownBy(() -> userService.updateUser(0L, dto))
                    .isInstanceOf(ValidationException.class)
                    .satisfies(ex -> {
                        ValidationException ve = (ValidationException) ex;
                        // Should have errors for both id and DTO fields
                        assertThat(ve.getErrors().size()).isGreaterThanOrEqualTo(1);
                    });
        }
    }

    @Nested
    @DisplayName("Multiple @Rule parameters")
    class MultipleRuleParameters {

        @Test
        @DisplayName("should pass when all parameters are valid")
        void shouldPassWhenAllParametersValid() {
            String result = userService.search("test query", 1, 10);

            assertThat(result).isEqualTo("Searching: test query (page 1, size 10)");
        }

        @Test
        @DisplayName("should throw when query is too short")
        void shouldThrowWhenQueryTooShort() {
            assertThatThrownBy(() -> userService.search("a", 1, 10))
                    .isInstanceOf(ValidationException.class);
        }

        @Test
        @DisplayName("should throw when page is invalid")
        void shouldThrowWhenPageInvalid() {
            assertThatThrownBy(() -> userService.search("test query", -1, 10))
                    .isInstanceOf(ValidationException.class);
        }

        @Test
        @DisplayName("should throw when pageSize is too large")
        void shouldThrowWhenPageSizeTooLarge() {
            assertThatThrownBy(() -> userService.search("test query", 1, 200))
                    .isInstanceOf(ValidationException.class);
        }
    }

    // Test Configuration
    @Configuration
    @EnableAutoConfiguration
    static class TestConfig {

        @Bean
        TestUserService testUserService() {
            return new TestUserService();
        }
    }

    // Test DTO
    public record TestUserDTO(
            @Rule("required|min:3|max:20")
            String username,

            @Rule("required|email")
            String email,

            @Rule("required|gte:18|lte:100")
            Integer age
    ) {}

    // Test Service
    @Validated
    @Service
    static class TestUserService {

        public String createUser(@Valid TestUserDTO dto) {
            return "Created: " + dto.username();
        }

        public String findById(@Rule("gte:1") Long id) {
            return "Found user: " + id;
        }

        public String updateUser(@Rule("gte:1") Long id, @Valid TestUserDTO dto) {
            return "Updated user " + id + ": " + dto.username();
        }

        public String search(
                @Rule("min:2|max:100") String query,
                @Rule("gte:0") Integer page,
                @Rule("gte:1|lte:100") Integer pageSize
        ) {
            return "Searching: " + query + " (page " + page + ", size " + pageSize + ")";
        }
    }
}