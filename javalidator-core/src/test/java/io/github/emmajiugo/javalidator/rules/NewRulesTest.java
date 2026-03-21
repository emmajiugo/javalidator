package io.github.emmajiugo.javalidator.rules;

import io.github.emmajiugo.javalidator.annotations.Rule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.github.emmajiugo.javalidator.rules.ValidationTestHelper.assertValidation;

/**
 * Tests for new validation rules: not_in, starts_with, ends_with
 */
@DisplayName("New Validation Rules")
class NewRulesTest {

    @Nested
    @DisplayName("NotIn Rule")
    class NotInRuleTests {

        record RoleField(
                @Rule("not_in:admin,superuser,root")
                String role
        ) {}

        @Test
        @DisplayName("should pass when value is not in the list")
        void shouldPassWhenValueNotInList() {
            assertValidation(new RoleField("user"))
                    .isValid();
            assertValidation(new RoleField("moderator"))
                    .isValid();
        }

        @Test
        @DisplayName("should fail when value is in the list")
        void shouldFailWhenValueIsInList() {
            assertValidation(new RoleField("admin"))
                    .hasSingleError()
                    .hasErrorOn("role")
                    .withMessageContaining("must not be one of");
        }

        @Test
        @DisplayName("should fail for each disallowed value")
        void shouldFailForEachDisallowedValue() {
            assertValidation(new RoleField("superuser"))
                    .hasSingleError()
                    .hasErrorOn("role")
                    .withMessageContaining("must not be one of");

            assertValidation(new RoleField("root"))
                    .hasSingleError()
                    .hasErrorOn("role")
                    .withMessageContaining("must not be one of");
        }

        @Test
        @DisplayName("should pass with null value")
        void shouldPassWithNullValue() {
            assertValidation(new RoleField(null))
                    .isValid();
        }
    }

    @Nested
    @DisplayName("StartsWith Rule")
    class StartsWithRuleTests {

        record UrlField(
                @Rule("starts_with:http,https")
                String url
        ) {}

        @Test
        @DisplayName("should pass when value starts with one of the prefixes")
        void shouldPassWhenValueStartsWithPrefix() {
            assertValidation(new UrlField("https://example.com"))
                    .isValid();
            assertValidation(new UrlField("http://example.com"))
                    .isValid();
        }

        @Test
        @DisplayName("should fail when value does not start with any prefix")
        void shouldFailWhenValueDoesNotStartWithAnyPrefix() {
            assertValidation(new UrlField("ftp://example.com"))
                    .hasSingleError()
                    .hasErrorOn("url")
                    .withMessageContaining("must start with one of");
        }

        @Test
        @DisplayName("should fail with non-matching value")
        void shouldFailWithNonMatchingValue() {
            assertValidation(new UrlField("example.com"))
                    .hasSingleError()
                    .hasErrorOn("url")
                    .withMessageContaining("must start with one of");
        }

        @Test
        @DisplayName("should pass with null value")
        void shouldPassWithNullValue() {
            assertValidation(new UrlField(null))
                    .isValid();
        }
    }

    @Nested
    @DisplayName("EndsWith Rule")
    class EndsWithRuleTests {

        record DomainField(
                @Rule("ends_with:.com,.org,.net")
                String domain
        ) {}

        @Test
        @DisplayName("should pass when value ends with one of the suffixes")
        void shouldPassWhenValueEndsWithSuffix() {
            assertValidation(new DomainField("example.com"))
                    .isValid();
            assertValidation(new DomainField("example.org"))
                    .isValid();
            assertValidation(new DomainField("example.net"))
                    .isValid();
        }

        @Test
        @DisplayName("should fail when value does not end with any suffix")
        void shouldFailWhenValueDoesNotEndWithAnySuffix() {
            assertValidation(new DomainField("example.io"))
                    .hasSingleError()
                    .hasErrorOn("domain")
                    .withMessageContaining("must end with one of");
        }

        @Test
        @DisplayName("should fail with non-matching value")
        void shouldFailWithNonMatchingValue() {
            assertValidation(new DomainField("not-a-domain"))
                    .hasSingleError()
                    .hasErrorOn("domain")
                    .withMessageContaining("must end with one of");
        }

        @Test
        @DisplayName("should pass with null value")
        void shouldPassWithNullValue() {
            assertValidation(new DomainField(null))
                    .isValid();
        }
    }
}
