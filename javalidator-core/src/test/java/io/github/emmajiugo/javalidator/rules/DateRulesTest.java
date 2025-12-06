package io.github.emmajiugo.javalidator.rules;

import io.github.emmajiugo.javalidator.annotations.Rule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static io.github.emmajiugo.javalidator.rules.ValidationTestHelper.assertValidation;

/**
 * Tests for date validation rules: date, before, after, future, past
 */
@DisplayName("Date Validation Rules")
class DateRulesTest {

    @Nested
    @DisplayName("Date Rule")
    class DateRuleTests {

        record DateField(
                @Rule("date")
                LocalDate value
        ) {}

        record StringDateField(
                @Rule("date:yyyy-MM-dd")
                String value
        ) {}

        @Test
        @DisplayName("should pass with LocalDate")
        void shouldPassWithLocalDate() {
            assertValidation(new DateField(LocalDate.now()))
                    .isValid();
        }

        @Test
        @DisplayName("should pass with valid date string")
        void shouldPassWithValidDateString() {
            assertValidation(new StringDateField("2024-12-25"))
                    .isValid();
        }

        @Test
        @DisplayName("should fail with invalid date string")
        void shouldFailWithInvalidDateString() {
            assertValidation(new StringDateField("not-a-date"))
                    .hasSingleError()
                    .hasErrorOn("value");
        }

        @Test
        @DisplayName("should pass with null value")
        void shouldPassWithNullValue() {
            assertValidation(new DateField(null))
                    .isValid();
        }
    }

    @Nested
    @DisplayName("Before Rule")
    class BeforeRuleTests {

        record BeforeField(
                @Rule("before:2025-12-31")
                LocalDate value
        ) {}

        @Test
        @DisplayName("should pass with date before threshold")
        void shouldPassWithDateBeforeThreshold() {
            assertValidation(new BeforeField(LocalDate.of(2025, 1, 1)))
                    .isValid();
            assertValidation(new BeforeField(LocalDate.of(2024, 12, 25)))
                    .isValid();
        }

        @Test
        @DisplayName("should fail with date equal to threshold")
        void shouldFailWithDateEqualToThreshold() {
            assertValidation(new BeforeField(LocalDate.of(2025, 12, 31)))
                    .hasSingleError()
                    .hasErrorOn("value")
                    .withMessageContaining("before 2025-12-31");
        }

        @Test
        @DisplayName("should fail with date after threshold")
        void shouldFailWithDateAfterThreshold() {
            assertValidation(new BeforeField(LocalDate.of(2026, 1, 1)))
                    .hasSingleError()
                    .hasErrorOn("value")
                    .withMessageContaining("before 2025-12-31");
        }

        @Test
        @DisplayName("should pass with null value")
        void shouldPassWithNullValue() {
            assertValidation(new BeforeField(null))
                    .isValid();
        }
    }

    @Nested
    @DisplayName("After Rule")
    class AfterRuleTests {

        record AfterField(
                @Rule("after:2024-01-01")
                LocalDate value
        ) {}

        @Test
        @DisplayName("should pass with date after threshold")
        void shouldPassWithDateAfterThreshold() {
            assertValidation(new AfterField(LocalDate.of(2024, 1, 2)))
                    .isValid();
            assertValidation(new AfterField(LocalDate.of(2025, 12, 25)))
                    .isValid();
        }

        @Test
        @DisplayName("should fail with date equal to threshold")
        void shouldFailWithDateEqualToThreshold() {
            assertValidation(new AfterField(LocalDate.of(2024, 1, 1)))
                    .hasSingleError()
                    .hasErrorOn("value")
                    .withMessageContaining("after 2024-01-01");
        }

        @Test
        @DisplayName("should fail with date before threshold")
        void shouldFailWithDateBeforeThreshold() {
            assertValidation(new AfterField(LocalDate.of(2023, 12, 31)))
                    .hasSingleError()
                    .hasErrorOn("value")
                    .withMessageContaining("after 2024-01-01");
        }

        @Test
        @DisplayName("should pass with null value")
        void shouldPassWithNullValue() {
            assertValidation(new AfterField(null))
                    .isValid();
        }
    }

    @Nested
    @DisplayName("Future Rule")
    class FutureRuleTests {

        record FutureField(
                @Rule("future")
                LocalDate value
        ) {}

        @Test
        @DisplayName("should pass with future date")
        void shouldPassWithFutureDate() {
            assertValidation(new FutureField(LocalDate.now().plusDays(1)))
                    .isValid();
            assertValidation(new FutureField(LocalDate.now().plusYears(1)))
                    .isValid();
        }

        @Test
        @DisplayName("should fail with past date")
        void shouldFailWithPastDate() {
            assertValidation(new FutureField(LocalDate.now().minusDays(1)))
                    .hasSingleError()
                    .hasErrorOn("value")
                    .withMessageContaining("future");
        }

        @Test
        @DisplayName("should fail with today")
        void shouldFailWithToday() {
            assertValidation(new FutureField(LocalDate.now()))
                    .hasSingleError()
                    .hasErrorOn("value")
                    .withMessageContaining("future");
        }

        @Test
        @DisplayName("should pass with null value")
        void shouldPassWithNullValue() {
            assertValidation(new FutureField(null))
                    .isValid();
        }
    }

    @Nested
    @DisplayName("Past Rule")
    class PastRuleTests {

        record PastField(
                @Rule("past")
                LocalDate value
        ) {}

        @Test
        @DisplayName("should pass with past date")
        void shouldPassWithPastDate() {
            assertValidation(new PastField(LocalDate.now().minusDays(1)))
                    .isValid();
            assertValidation(new PastField(LocalDate.now().minusYears(1)))
                    .isValid();
        }

        @Test
        @DisplayName("should fail with future date")
        void shouldFailWithFutureDate() {
            assertValidation(new PastField(LocalDate.now().plusDays(1)))
                    .hasSingleError()
                    .hasErrorOn("value")
                    .withMessageContaining("past");
        }

        @Test
        @DisplayName("should fail with today")
        void shouldFailWithToday() {
            assertValidation(new PastField(LocalDate.now()))
                    .hasSingleError()
                    .hasErrorOn("value")
                    .withMessageContaining("past");
        }

        @Test
        @DisplayName("should pass with null value")
        void shouldPassWithNullValue() {
            assertValidation(new PastField(null))
                    .isValid();
        }
    }

    @Nested
    @DisplayName("Combined Date Rules")
    class CombinedDateRulesTests {

        record Event(
                @Rule("required")
                String name,

                @Rule("required|date|future")
                LocalDate eventDate,

                @Rule("date|past")
                LocalDate registeredDate
        ) {}

        @Test
        @DisplayName("should pass with valid date fields")
        void shouldPassWithValidDateFields() {
            assertValidation(new Event(
                    "Conference",
                    LocalDate.now().plusDays(30),
                    LocalDate.now().minusDays(5)
            )).isValid();
        }

        @Test
        @DisplayName("should fail with invalid date fields")
        void shouldFailWithInvalidDateFields() {
            assertValidation(new Event(
                    "Conference",
                    LocalDate.now().minusDays(1), // Should be future
                    LocalDate.now().plusDays(1)   // Should be past
            )).isInvalid()
                    .hasErrorCount(2);
        }
    }
}