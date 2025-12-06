package io.github.emmajiugo.javalidator.rules;

import io.github.emmajiugo.javalidator.annotations.Rule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.github.emmajiugo.javalidator.rules.ValidationTestHelper.assertValidation;

/**
 * Tests for collection rules: distinct
 */
@DisplayName("Collection Rules")
class CollectionRulesTest {

    @Nested
    @DisplayName("Distinct Rule")
    class DistinctRuleTests {

        record UniqueIds(
                @Rule("required|distinct")
                Integer[] ids
        ) {}

        record UniqueTags(
                @Rule("required|distinct")
                List<String> tags
        ) {}

        record UniqueEmails(
                @Rule("required|distinct")
                String[] emails
        ) {}

        record OptionalIds(
                @Rule("distinct")
                Integer[] ids
        ) {}

        @Test
        @DisplayName("should pass with unique array elements")
        void shouldPassWithUniqueArrayElements() {
            assertValidation(new UniqueIds(new Integer[]{1, 2, 3, 4, 5}))
                    .isValid();
        }

        @Test
        @DisplayName("should pass with unique list elements")
        void shouldPassWithUniqueListElements() {
            assertValidation(new UniqueTags(List.of("java", "spring", "maven")))
                    .isValid();
        }

        @Test
        @DisplayName("should fail with duplicate array elements")
        void shouldFailWithDuplicateArrayElements() {
            assertValidation(new UniqueIds(new Integer[]{1, 2, 3, 2, 5}))
                    .hasSingleError()
                    .hasErrorOn("ids")
                    .withMessageContaining("distinct values");
        }

        @Test
        @DisplayName("should fail with duplicate list elements")
        void shouldFailWithDuplicateListElements() {
            assertValidation(new UniqueTags(List.of("java", "spring", "java")))
                    .hasSingleError()
                    .hasErrorOn("tags")
                    .withMessageContaining("distinct values");
        }

        @Test
        @DisplayName("should pass with empty array")
        void shouldPassWithEmptyArray() {
            assertValidation(new OptionalIds(new Integer[]{}))
                    .isValid();
        }

        @Test
        @DisplayName("should pass when value is null")
        void shouldPassWhenValueIsNull() {
            assertValidation(new OptionalIds(null))
                    .isValid();
        }

        @Test
        @DisplayName("should work with string arrays")
        void shouldWorkWithStringArrays() {
            assertValidation(new UniqueEmails(new String[]{"a@test.com", "b@test.com"}))
                    .isValid();

            assertValidation(new UniqueEmails(new String[]{"a@test.com", "a@test.com"}))
                    .hasSingleError()
                    .hasErrorOn("emails")
                    .withMessageContaining("distinct values");
        }
    }
}