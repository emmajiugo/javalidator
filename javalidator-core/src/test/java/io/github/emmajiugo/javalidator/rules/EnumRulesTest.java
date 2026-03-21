package io.github.emmajiugo.javalidator.rules;

import io.github.emmajiugo.javalidator.annotations.Rule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.github.emmajiugo.javalidator.rules.ValidationTestHelper.assertValidation;

@DisplayName("Enum Validation Rule")
class EnumRulesTest {

    enum Status { ACTIVE, INACTIVE, PENDING }

    @Nested
    @DisplayName("Valid Enum Values")
    class ValidEnumTests {
        record StatusField(@Rule(value = "enum", enumClass = Status.class) String status) {}

        @Test @DisplayName("should pass with valid enum value")
        void shouldPassWithValidEnumValue() {
            assertValidation(new StatusField("ACTIVE")).isValid();
        }

        @Test @DisplayName("should pass with all valid enum values")
        void shouldPassWithAllValidValues() {
            assertValidation(new StatusField("ACTIVE")).isValid();
            assertValidation(new StatusField("INACTIVE")).isValid();
            assertValidation(new StatusField("PENDING")).isValid();
        }

        @Test @DisplayName("should pass with null value")
        void shouldPassWithNullValue() {
            assertValidation(new StatusField(null)).isValid();
        }
    }

    @Nested
    @DisplayName("Invalid Enum Values")
    class InvalidEnumTests {
        record StatusField(@Rule(value = "enum", enumClass = Status.class) String status) {}

        @Test @DisplayName("should fail with invalid enum value")
        void shouldFailWithInvalidValue() {
            assertValidation(new StatusField("UNKNOWN"))
                    .hasSingleError().hasErrorOn("status").withMessageContaining("must be one of");
        }

        @Test @DisplayName("should fail with lowercase enum value")
        void shouldFailWithLowercaseValue() {
            assertValidation(new StatusField("active")).hasSingleError().hasErrorOn("status");
        }
    }

    @Nested
    @DisplayName("Custom Error Messages")
    class CustomMessageTests {
        record StatusField(@Rule(value = "enum", enumClass = Status.class, message = "Invalid status provided") String status) {}

        @Test @DisplayName("should use custom error message")
        void shouldUseCustomMessage() {
            assertValidation(new StatusField("UNKNOWN"))
                    .hasSingleError().hasErrorOn("status").withMessage("Invalid status provided");
        }
    }

    @Nested
    @DisplayName("Required with Enum")
    class RequiredEnumTests {
        record RequiredStatusField(
                @Rule("required")
                @Rule(value = "enum", enumClass = Status.class)
                String status
        ) {}

        @Test @DisplayName("should fail required when null")
        void shouldFailRequiredWhenNull() {
            assertValidation(new RequiredStatusField(null))
                    .hasSingleError().hasErrorOn("status").withMessageContaining("required");
        }
    }
}
