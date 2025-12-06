package io.github.emmajiugo.javalidator.rules;

import io.github.emmajiugo.javalidator.annotations.Rule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.github.emmajiugo.javalidator.rules.ValidationTestHelper.assertValidation;

/**
 * Tests for pattern validation rules: regex, alpha, alpha_num, url, ip, uuid, json
 */
@DisplayName("Pattern Validation Rules")
class PatternRulesTest {

    @Nested
    @DisplayName("Regex Rule")
    class RegexRuleTests {

        record ProductCode(
                @Rule("regex:^[A-Z]{2}\\d{4}$")
                String code
        ) {}

        @Test
        @DisplayName("should pass with valid pattern")
        void shouldPassWithValidPattern() {
            assertValidation(new ProductCode("AB1234"))
                    .isValid();
            assertValidation(new ProductCode("XY9999"))
                    .isValid();
        }

        @Test
        @DisplayName("should fail with invalid pattern")
        void shouldFailWithInvalidPattern() {
            assertValidation(new ProductCode("ab1234"))
                    .hasSingleError()
                    .hasErrorOn("code")
                    .withMessageContaining("format");

            assertValidation(new ProductCode("ABC123"))
                    .hasSingleError();

            assertValidation(new ProductCode("A12345"))
                    .hasSingleError();
        }

        @Test
        @DisplayName("should pass with null value")
        void shouldPassWithNullValue() {
            assertValidation(new ProductCode(null))
                    .isValid();
        }
    }

    @Nested
    @DisplayName("Alpha Rule")
    class AlphaRuleTests {

        record AlphaField(
                @Rule("alpha")
                String value
        ) {}

        @Test
        @DisplayName("should pass with alphabetic characters only")
        void shouldPassWithAlphabeticCharactersOnly() {
            assertValidation(new AlphaField("abc"))
                    .isValid();
            assertValidation(new AlphaField("XYZ"))
                    .isValid();
            assertValidation(new AlphaField("AbCdEf"))
                    .isValid();
        }

        @Test
        @DisplayName("should fail with numbers")
        void shouldFailWithNumbers() {
            assertValidation(new AlphaField("abc123"))
                    .hasSingleError()
                    .hasErrorOn("value")
                    .withMessageContaining("alphabetic");
        }

        @Test
        @DisplayName("should fail with special characters")
        void shouldFailWithSpecialCharacters() {
            assertValidation(new AlphaField("abc-def"))
                    .hasSingleError()
                    .hasErrorOn("value")
                    .withMessageContaining("alphabetic");
        }

        @Test
        @DisplayName("should pass with null value")
        void shouldPassWithNullValue() {
            assertValidation(new AlphaField(null))
                    .isValid();
        }
    }

    @Nested
    @DisplayName("Alpha Numeric Rule")
    class AlphaNumRuleTests {

        record AlphaNumField(
                @Rule("alpha_num")
                String value
        ) {}

        @Test
        @DisplayName("should pass with alphanumeric characters")
        void shouldPassWithAlphanumericCharacters() {
            assertValidation(new AlphaNumField("abc123"))
                    .isValid();
            assertValidation(new AlphaNumField("ABC"))
                    .isValid();
            assertValidation(new AlphaNumField("123"))
                    .isValid();
        }

        @Test
        @DisplayName("should fail with special characters")
        void shouldFailWithSpecialCharacters() {
            assertValidation(new AlphaNumField("abc-123"))
                    .hasSingleError()
                    .hasErrorOn("value")
                    .withMessageContaining("alphanumeric");

            assertValidation(new AlphaNumField("abc_123"))
                    .hasSingleError();
        }

        @Test
        @DisplayName("should pass with null value")
        void shouldPassWithNullValue() {
            assertValidation(new AlphaNumField(null))
                    .isValid();
        }
    }

    @Nested
    @DisplayName("URL Rule")
    class UrlRuleTests {

        record UrlField(
                @Rule("url")
                String value
        ) {}

        @Test
        @DisplayName("should pass with valid URLs")
        void shouldPassWithValidUrls() {
            assertValidation(new UrlField("http://example.com"))
                    .isValid();
            assertValidation(new UrlField("https://www.example.com"))
                    .isValid();
            assertValidation(new UrlField("https://example.com/path/to/page"))
                    .isValid();
        }

        @Test
        @DisplayName("should fail with invalid URLs")
        void shouldFailWithInvalidUrls() {
            assertValidation(new UrlField("not-a-url"))
                    .hasSingleError()
                    .hasErrorOn("value")
                    .withMessageContaining("URL");

            assertValidation(new UrlField("example.com"))
                    .hasSingleError();
        }

        @Test
        @DisplayName("should pass with null value")
        void shouldPassWithNullValue() {
            assertValidation(new UrlField(null))
                    .isValid();
        }
    }

    @Nested
    @DisplayName("IP Rule")
    class IpRuleTests {

        record IpField(
                @Rule("ip")
                String value
        ) {}

        @Test
        @DisplayName("should pass with valid IPv4 addresses")
        void shouldPassWithValidIpv4Addresses() {
            assertValidation(new IpField("192.168.1.1"))
                    .isValid();
            assertValidation(new IpField("10.0.0.1"))
                    .isValid();
            assertValidation(new IpField("255.255.255.255"))
                    .isValid();
        }

        @Test
        @DisplayName("should fail with invalid IP addresses")
        void shouldFailWithInvalidIpAddresses() {
            assertValidation(new IpField("256.1.1.1"))
                    .hasSingleError()
                    .hasErrorOn("value")
                    .withMessageContaining("IP");

            assertValidation(new IpField("192.168.1"))
                    .hasSingleError();

            assertValidation(new IpField("not-an-ip"))
                    .hasSingleError();
        }

        @Test
        @DisplayName("should pass with null value")
        void shouldPassWithNullValue() {
            assertValidation(new IpField(null))
                    .isValid();
        }
    }

    @Nested
    @DisplayName("UUID Rule")
    class UuidRuleTests {

        record UuidField(
                @Rule("uuid")
                String value
        ) {}

        @Test
        @DisplayName("should pass with valid UUIDs")
        void shouldPassWithValidUuids() {
            assertValidation(new UuidField("550e8400-e29b-41d4-a716-446655440000"))
                    .isValid();
            assertValidation(new UuidField("123e4567-e89b-12d3-a456-426614174000"))
                    .isValid();
        }

        @Test
        @DisplayName("should fail with invalid UUIDs")
        void shouldFailWithInvalidUuids() {
            assertValidation(new UuidField("not-a-uuid"))
                    .hasSingleError()
                    .hasErrorOn("value")
                    .withMessageContaining("UUID");

            assertValidation(new UuidField("123-456-789"))
                    .hasSingleError();
        }

        @Test
        @DisplayName("should pass with null value")
        void shouldPassWithNullValue() {
            assertValidation(new UuidField(null))
                    .isValid();
        }
    }

    @Nested
    @DisplayName("JSON Rule")
    class JsonRuleTests {

        record JsonField(
                @Rule("json")
                String value
        ) {}

        @Test
        @DisplayName("should pass with valid JSON")
        void shouldPassWithValidJson() {
            assertValidation(new JsonField("{\"name\":\"John\"}"))
                    .isValid();
            assertValidation(new JsonField("[1,2,3]"))
                    .isValid();
            assertValidation(new JsonField("\"string\""))
                    .isValid();
        }

        @Test
        @DisplayName("should fail with invalid JSON")
        void shouldFailWithInvalidJson() {
            assertValidation(new JsonField("{invalid}"))
                    .hasSingleError()
                    .hasErrorOn("value")
                    .withMessageContaining("JSON");

            assertValidation(new JsonField("not json"))
                    .hasSingleError();
        }

        @Test
        @DisplayName("should pass with null value")
        void shouldPassWithNullValue() {
            assertValidation(new JsonField(null))
                    .isValid();
        }
    }

    @Nested
    @DisplayName("Combined Pattern Rules")
    class CombinedPatternRulesTests {

        record Config(
                @Rule("required|alpha_num|min:3|max:20")
                String name,

                @Rule("url")
                String website,

                @Rule("required|regex:^[A-Z]{2}-\\d{3}$")
                String regionCode
        ) {}

        @Test
        @DisplayName("should pass with all valid pattern fields")
        void shouldPassWithAllValidPatternFields() {
            assertValidation(new Config(
                    "config123",
                    "https://example.com",
                    "US-001"
            )).isValid();
        }

        @Test
        @DisplayName("should fail with invalid pattern fields")
        void shouldFailWithInvalidPatternFields() {
            assertValidation(new Config(
                    "cf",              // Too short
                    "not-a-url",       // Invalid URL
                    "invalid"          // Invalid region code
            )).isInvalid()
                    .hasErrorCount(3);
        }
    }
}