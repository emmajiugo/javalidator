package me.emmajiugo.javalidator.rules;

import me.emmajiugo.javalidator.annotations.Rule;
import me.emmajiugo.javalidator.annotations.RuleCascade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static me.emmajiugo.javalidator.rules.ValidationTestHelper.assertValidation;

/**
 * Tests for nested object validation using @RuleCascade annotation
 */
@DisplayName("Nested Validation")
class NestedValidationTest {

    @Nested
    @DisplayName("Single Nested Object")
    class SingleNestedObjectTests {

        record Address(
                @Rule("required")
                String street,

                @Rule("required")
                String city,

                @Rule("required|digits:5")
                String zipCode
        ) {}

        record User(
                @Rule("required|min:3")
                String name,

                @RuleCascade
                Address address
        ) {}

        @Test
        @DisplayName("should pass with valid nested object")
        void shouldPassWithValidNestedObject() {
            var user = new User(
                    "John Doe",
                    new Address("123 Main St", "New York", "10001")
            );

            assertValidation(user).isValid();
        }

        @Test
        @DisplayName("should fail with invalid nested object")
        void shouldFailWithInvalidNestedObject() {
            var user = new User(
                    "John Doe",
                    new Address(null, "", "ABC")
            );

            assertValidation(user)
                    .isInvalid()
                    .hasSingleError()
                    .hasErrorOn("address");
        }

        @Test
        @DisplayName("should pass when nested object is null")
        void shouldPassWhenNestedObjectIsNull() {
            var user = new User("John Doe", null);

            assertValidation(user).isValid();
        }

        @Test
        @DisplayName("should fail when nested object is null and required")
        void shouldFailWhenNestedObjectIsNullAndRequired() {
            record UserWithRequiredAddress(
                    @Rule("required")
                    String name,

                    @Rule("required")
                    @RuleCascade
                    Address address
            ) {}

            var user = new UserWithRequiredAddress("John Doe", null);

            assertValidation(user)
                    .hasSingleError()
                    .hasErrorOn("address")
                    .withMessageContaining("required");
        }
    }

    @Nested
    @DisplayName("Deeply Nested Objects")
    class DeeplyNestedObjectsTests {

        record Country(
                @Rule("required|alpha")
                String name,

                @Rule("required|alpha|size:2")
                String code
        ) {}

        record Address(
                @Rule("required")
                String street,

                @Rule("required")
                String city,

                @RuleCascade
                Country country
        ) {}

        record User(
                @Rule("required")
                String name,

                @RuleCascade
                Address address
        ) {}

        @Test
        @DisplayName("should pass with all valid nested levels")
        void shouldPassWithAllValidNestedLevels() {
            var user = new User(
                    "John Doe",
                    new Address(
                            "123 Main St",
                            "New York",
                            new Country("USA", "US")
                    )
            );

            assertValidation(user).isValid();
        }

        @Test
        @DisplayName("should fail with errors in deeply nested object")
        void shouldFailWithErrorsInDeeplyNestedObject() {
            var user = new User(
                    "John Doe",
                    new Address(
                            "123 Main St",
                            "New York",
                            new Country("", "ABC")  // Invalid: empty name, code too long
                    )
            );

            assertValidation(user)
                    .isInvalid()
                    .hasSingleError()
                    .hasErrorOn("address");
        }
    }

    @Nested
    @DisplayName("Multiple Nested Objects")
    class MultipleNestedObjectsTests {

        record ContactInfo(
                @Rule("required|email")
                String email,

                @Rule("digits:10")
                String phone
        ) {}

        record Address(
                @Rule("required")
                String street,

                @Rule("required")
                String city
        ) {}

        record User(
                @Rule("required")
                String name,

                @RuleCascade
                Address address,

                @RuleCascade
                ContactInfo contact
        ) {}

        @Test
        @DisplayName("should pass with all valid nested objects")
        void shouldPassWithAllValidNestedObjects() {
            var user = new User(
                    "John Doe",
                    new Address("123 Main St", "New York"),
                    new ContactInfo("john@example.com", "5551234567")
            );

            assertValidation(user).isValid();
        }

        @Test
        @DisplayName("should fail with errors in multiple nested objects")
        void shouldFailWithErrorsInMultipleNestedObjects() {
            var user = new User(
                    "John Doe",
                    new Address("", ""),  // Both required fields empty
                    new ContactInfo("invalid-email", "ABC")  // Invalid email and phone
            );

            var validation = assertValidation(user)
                    .isInvalid()
                    .hasErrorCount(2);

            validation.hasErrorOn("address");
            validation.hasErrorOn("contact");
        }
    }

    @Nested
    @DisplayName("Nested Object with Class (not Record)")
    class NestedClassTests {

        static class Address {
            @Rule("required")
            private String street;

            @Rule("required")
            private String city;

            public Address(String street, String city) {
                this.street = street;
                this.city = city;
            }

            public String getStreet() { return street; }
            public String getCity() { return city; }
        }

        record User(
                @Rule("required")
                String name,

                @RuleCascade
                Address address
        ) {}

        @Test
        @DisplayName("should validate nested class objects")
        void shouldValidateNestedClassObjects() {
            var user = new User(
                    "John Doe",
                    new Address("123 Main St", "New York")
            );

            assertValidation(user).isValid();
        }

        @Test
        @DisplayName("should fail with invalid nested class object")
        void shouldFailWithInvalidNestedClassObject() {
            var user = new User(
                    "John Doe",
                    new Address(null, "")
            );

            assertValidation(user)
                    .isInvalid()
                    .hasSingleError()
                    .hasErrorOn("address");
        }
    }
}