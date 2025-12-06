package io.github.emmajiugo.javalidator.rules;

import io.github.emmajiugo.javalidator.annotations.Rule;
import io.github.emmajiugo.javalidator.annotations.RuleCascade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static io.github.emmajiugo.javalidator.rules.ValidationTestHelper.assertValidation;

/**
 * Tests for collection validation using @RuleCascade annotation
 */
@DisplayName("Collection Validation")
class CollectionValidationTest {

    @Nested
    @DisplayName("List Validation")
    class ListValidationTests {

        record PhoneNumber(
                @Rule("required|digits:10")
                String number
        ) {}

        record User(
                @Rule("required")
                String name,

                @RuleCascade
                List<PhoneNumber> phoneNumbers
        ) {}

        @Test
        @DisplayName("should pass with valid list of objects")
        void shouldPassWithValidListOfObjects() {
            var user = new User(
                    "John Doe",
                    List.of(
                            new PhoneNumber("5551234567"),
                            new PhoneNumber("5559876543")
                    )
            );

            assertValidation(user).isValid();
        }

        @Test
        @DisplayName("should fail with invalid items in list")
        void shouldFailWithInvalidItemsInList() {
            var user = new User(
                    "John Doe",
                    List.of(
                            new PhoneNumber("123"),        // Too short
                            new PhoneNumber("ABC1234567")  // Contains letters
                    )
            );

            assertValidation(user)
                    .isInvalid()
                    .hasSingleError()
                    .hasErrorOn("phoneNumbers");
        }

        @Test
        @DisplayName("should pass with empty list")
        void shouldPassWithEmptyList() {
            var user = new User("John Doe", List.of());

            assertValidation(user).isValid();
        }

        @Test
        @DisplayName("should pass with null list")
        void shouldPassWithNullList() {
            var user = new User("John Doe", null);

            assertValidation(user).isValid();
        }

        @Test
        @DisplayName("should fail when list is required and null")
        void shouldFailWhenListIsRequiredAndNull() {
            record UserWithRequiredList(
                    @Rule("required")
                    String name,

                    @Rule("required")
                    @RuleCascade
                    List<PhoneNumber> phoneNumbers
            ) {}

            var user = new UserWithRequiredList("John Doe", null);

            assertValidation(user)
                    .hasSingleError()
                    .hasErrorOn("phoneNumbers")
                    .withMessageContaining("required");
        }
    }

    @Nested
    @DisplayName("Array Validation")
    class ArrayValidationTests {

        record Email(
                @Rule("required|email")
                String address
        ) {}

        record User(
                @Rule("required")
                String name,

                @RuleCascade
                Email[] emails
        ) {}

        @Test
        @DisplayName("should pass with valid array of objects")
        void shouldPassWithValidArrayOfObjects() {
            var user = new User(
                    "John Doe",
                    new Email[] {
                            new Email("john@example.com"),
                            new Email("john.doe@work.com")
                    }
            );

            assertValidation(user).isValid();
        }

        @Test
        @DisplayName("should fail with invalid items in array")
        void shouldFailWithInvalidItemsInArray() {
            var user = new User(
                    "John Doe",
                    new Email[] {
                            new Email("invalid-email"),
                            new Email("@example.com")
                    }
            );

            assertValidation(user)
                    .isInvalid()
                    .hasSingleError()
                    .hasErrorOn("emails");
        }

        @Test
        @DisplayName("should pass with empty array")
        void shouldPassWithEmptyArray() {
            var user = new User("John Doe", new Email[0]);

            assertValidation(user).isValid();
        }

        @Test
        @DisplayName("should pass with null array")
        void shouldPassWithNullArray() {
            var user = new User("John Doe", null);

            assertValidation(user).isValid();
        }
    }

    @Nested
    @DisplayName("Set Validation")
    class SetValidationTests {

        record Tag(
                @Rule("required|alpha_num|min:2|max:20")
                String name
        ) {}

        record Article(
                @Rule("required")
                String title,

                @RuleCascade
                Set<Tag> tags
        ) {}

        @Test
        @DisplayName("should pass with valid set of objects")
        void shouldPassWithValidSetOfObjects() {
            var article = new Article(
                    "My Article",
                    Set.of(
                            new Tag("java"),
                            new Tag("validation")
                    )
            );

            assertValidation(article).isValid();
        }

        @Test
        @DisplayName("should fail with invalid items in set")
        void shouldFailWithInvalidItemsInSet() {
            var article = new Article(
                    "My Article",
                    Set.of(
                            new Tag("a"),              // Too short
                            new Tag("way-too-long-tag-name-exceeds-limit")  // Too long
                    )
            );

            assertValidation(article)
                    .isInvalid()
                    .hasSingleError()
                    .hasErrorOn("tags");
        }
    }

    @Nested
    @DisplayName("Nested Collections")
    class NestedCollectionsTests {

        record PhoneNumber(
                @Rule("required|digits:10")
                String number,

                @Rule("required|in:mobile,home,work")
                String type
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
                List<PhoneNumber> phoneNumbers
        ) {}

        @Test
        @DisplayName("should pass with valid nested object and collection")
        void shouldPassWithValidNestedObjectAndCollection() {
            var user = new User(
                    "John Doe",
                    new Address("123 Main St", "New York"),
                    List.of(
                            new PhoneNumber("5551234567", "mobile"),
                            new PhoneNumber("5559876543", "home")
                    )
            );

            assertValidation(user).isValid();
        }

        @Test
        @DisplayName("should fail with errors in both nested object and collection")
        void shouldFailWithErrorsInBothNestedObjectAndCollection() {
            var user = new User(
                    "John Doe",
                    new Address("", ""),  // Both required fields empty
                    List.of(
                            new PhoneNumber("123", "mobile"),      // Invalid number
                            new PhoneNumber("5559876543", "other")  // Invalid type
                    )
            );

            var validation = assertValidation(user)
                    .isInvalid()
                    .hasErrorCount(2);

            validation.hasErrorOn("address");
            validation.hasErrorOn("phoneNumbers");
        }
    }

    @Nested
    @DisplayName("Complex Nested Collections")
    class ComplexNestedCollectionsTests {

        record Item(
                @Rule("required|min:3")
                String name,

                @Rule("required|gte:0")
                Double price
        ) {}

        record Order(
                @Rule("required")
                String orderId,

                @RuleCascade
                List<Item> items
        ) {}

        record Customer(
                @Rule("required|email")
                String email,

                @RuleCascade
                List<Order> orders
        ) {}

        @Test
        @DisplayName("should pass with valid deeply nested collections")
        void shouldPassWithValidDeeplyNestedCollections() {
            var customer = new Customer(
                    "customer@example.com",
                    List.of(
                            new Order(
                                    "ORD-001",
                                    List.of(
                                            new Item("Laptop", 999.99),
                                            new Item("Mouse", 29.99)
                                    )
                            ),
                            new Order(
                                    "ORD-002",
                                    List.of(
                                            new Item("Keyboard", 79.99)
                                    )
                            )
                    )
            );

            assertValidation(customer).isValid();
        }

        @Test
        @DisplayName("should fail with errors in deeply nested collections")
        void shouldFailWithErrorsInDeeplyNestedCollections() {
            var customer = new Customer(
                    "invalid-email",
                    List.of(
                            new Order(
                                    "ORD-001",
                                    List.of(
                                            new Item("AB", -10.0),  // Name too short, price negative
                                            new Item("Mouse", 29.99)
                                    )
                            )
                    )
            );

            var validation = assertValidation(customer)
                    .isInvalid()
                    .hasErrorCount(2);

            validation.hasErrorOn("email");
            validation.hasErrorOn("orders");
        }
    }

    @Nested
    @DisplayName("Mixed Collections with Null Items")
    class MixedCollectionsWithNullItemsTests {

        record Address(
                @Rule("required")
                String city
        ) {}

        record User(
                @Rule("required")
                String name,

                @RuleCascade
                List<Address> addresses
        ) {}

        @Test
        @DisplayName("should skip null items in collection")
        void shouldSkipNullItemsInCollection() {
            var user = new User(
                    "John Doe",
                    Arrays.asList(
                            new Address("New York"),
                            null,  // Null item should be skipped
                            new Address("Boston")
                    )
            );

            assertValidation(user).isValid();
        }
    }
}