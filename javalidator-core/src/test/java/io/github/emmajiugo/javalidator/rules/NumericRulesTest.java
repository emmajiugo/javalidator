package io.github.emmajiugo.javalidator.rules;

import io.github.emmajiugo.javalidator.annotations.Rule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.github.emmajiugo.javalidator.rules.ValidationTestHelper.assertValidation;

/**
 * Tests for numeric validation rules: numeric, gt, lt, gte, lte, between
 */
@DisplayName("Numeric Validation Rules")
class NumericRulesTest {

    @Nested
    @DisplayName("Numeric Rule")
    class NumericRuleTests {

        record NumericField(
                @Rule("numeric")
                Object value
        ) {}

        @Test
        @DisplayName("should pass with Integer")
        void shouldPassWithInteger() {
            assertValidation(new NumericField(42))
                    .isValid();
        }

        @Test
        @DisplayName("should pass with Double")
        void shouldPassWithDouble() {
            assertValidation(new NumericField(42.5))
                    .isValid();
        }

        @Test
        @DisplayName("should pass with Long")
        void shouldPassWithLong() {
            assertValidation(new NumericField(42L))
                    .isValid();
        }

        @Test
        @DisplayName("should fail with String")
        void shouldFailWithString() {
            assertValidation(new NumericField("not a number"))
                    .hasSingleError()
                    .hasErrorOn("value")
                    .withMessageContaining("number");
        }

        @Test
        @DisplayName("should pass with null value")
        void shouldPassWithNullValue() {
            assertValidation(new NumericField(null))
                    .isValid();
        }
    }

    @Nested
    @DisplayName("Greater Than (gt) Rule")
    class GtRuleTests {

        record GtField(
                @Rule("gt:17")
                Integer value,

                @Rule("gt:10")
                String anotherValue
        ) {}

        @Test
        @DisplayName("should pass with value greater than threshold")
        void shouldPassWithValueGreaterThanThreshold() {
            assertValidation(new GtField(18, "20"))
                    .isValid();
            assertValidation(new GtField(100, "15"))
                    .isValid();
        }

        @Test
        @DisplayName("should fail with value equal to threshold")
        void shouldFailWithValueEqualToThreshold() {
            assertValidation(new GtField(17, "10"))
                    .hasErrorCount(2)
                    .hasErrorOn("value")
                    .withMessageContaining("greater than 17");
        }

        @Test
        @DisplayName("should fail with value less than threshold")
        void shouldFailWithValueLessThanThreshold() {
            assertValidation(new GtField(16, "5"))
                    .hasErrorCount(2)
                    .hasErrorOn("value")
                    .withMessageContaining("greater than 17");
        }

        @Test
        @DisplayName("should pass with null value")
        void shouldPassWithNullValue() {
            assertValidation(new GtField(null, null))
                    .isValid();
        }
    }

    @Nested
    @DisplayName("Less Than (lt) Rule")
    class LtRuleTests {

        record LtField(
                @Rule("lt:100")
                Integer value,

                @Rule("lt:50")
                String anotherValue
        ) {}

        @Test
        @DisplayName("should pass with value less than threshold")
        void shouldPassWithValueLessThanThreshold() {
            assertValidation(new LtField(99, "30"))
                    .isValid();
            assertValidation(new LtField(50, "10"))
                    .isValid();
        }

        @Test
        @DisplayName("should fail with value equal to threshold")
        void shouldFailWithValueEqualToThreshold() {
            assertValidation(new LtField(100, "50"))
                    .hasErrorCount(2)
                    .hasErrorOn("value")
                    .withMessageContaining("less than");
        }

        @Test
        @DisplayName("should fail with value greater than threshold")
        void shouldFailWithValueGreaterThanThreshold() {
            assertValidation(new LtField(101, "60"))
                    .hasErrorCount(2)
                    .hasErrorOn("value")
                    .withMessageContaining("less than 100");
        }

        @Test
        @DisplayName("should pass with null value")
        void shouldPassWithNullValue() {
            assertValidation(new LtField(null, null))
                    .isValid();
        }
    }

    @Nested
    @DisplayName("Greater Than or Equal (gte) Rule")
    class GteRuleTests {

        record GteField(
                @Rule("gte:18")
                Integer value,

                @Rule("gte:10")
                String anotherValue
        ) {}

        @Test
        @DisplayName("should pass with value equal to threshold")
        void shouldPassWithValueEqualToThreshold() {
            assertValidation(new GteField(18, "10"))
                    .isValid();
        }

        @Test
        @DisplayName("should pass with value greater than threshold")
        void shouldPassWithValueGreaterThanThreshold() {
            assertValidation(new GteField(19, "15"))
                    .isValid();
            assertValidation(new GteField(100, "20"))
                    .isValid();
        }

        @Test
        @DisplayName("should fail with value less than threshold")
        void shouldFailWithValueLessThanThreshold() {
            assertValidation(new GteField(17, "5"))
                    .hasErrorCount(2)
                    .hasErrorOn("value")
                    .withMessageContaining("at least 18");
        }

        @Test
        @DisplayName("should pass with null value")
        void shouldPassWithNullValue() {
            assertValidation(new GteField(null, null))
                    .isValid();
        }
    }

    @Nested
    @DisplayName("Less Than or Equal (lte) Rule")
    class LteRuleTests {

        record LteField(
                @Rule("lte:100")
                Integer value,

                @Rule("lte:50")
                String anotherValue
        ) {}

        @Test
        @DisplayName("should pass with value equal to threshold")
        void shouldPassWithValueEqualToThreshold() {
            assertValidation(new LteField(100, "50"))
                    .isValid();
        }

        @Test
        @DisplayName("should pass with value less than threshold")
        void shouldPassWithValueLessThanThreshold() {
            assertValidation(new LteField(99, "30"))
                    .isValid();
            assertValidation(new LteField(50, "10"))
                    .isValid();
        }

        @Test
        @DisplayName("should fail with value greater than threshold")
        void shouldFailWithValueGreaterThanThreshold() {
            assertValidation(new LteField(101, "60"))
                    .hasErrorCount(2)
                    .hasErrorOn("value")
                    .withMessageContaining("not exceed 100");
        }

        @Test
        @DisplayName("should pass with null value")
        void shouldPassWithNullValue() {
            assertValidation(new LteField(null, null))
                    .isValid();
        }
    }

    @Nested
    @DisplayName("Between Rule")
    class BetweenRuleTests {

        record BetweenField(
                @Rule("between:18,65")
                Integer value
        ) {}

        @Test
        @DisplayName("should pass with value within range")
        void shouldPassWithValueWithinRange() {
            assertValidation(new BetweenField(18))
                    .isValid();
            assertValidation(new BetweenField(40))
                    .isValid();
            assertValidation(new BetweenField(65))
                    .isValid();
        }

        @Test
        @DisplayName("should fail with value below range")
        void shouldFailWithValueBelowRange() {
            assertValidation(new BetweenField(17))
                    .hasSingleError()
                    .hasErrorOn("value")
                    .withMessageContaining("between");
        }

        @Test
        @DisplayName("should fail with value above range")
        void shouldFailWithValueAboveRange() {
            assertValidation(new BetweenField(66))
                    .hasSingleError()
                    .hasErrorOn("value")
                    .withMessageContaining("between");
        }

        @Test
        @DisplayName("should pass with null value")
        void shouldPassWithNullValue() {
            assertValidation(new BetweenField(null))
                    .isValid();
        }
    }

    @Nested
    @DisplayName("Combined Numeric Rules")
    class CombinedNumericRulesTests {

        record Product(
                @Rule("required|min:3")
                String name,

                @Rule("required|numeric|gte:0")
                Double price,

                @Rule("required|numeric|between:1,1000")
                Integer stock
        ) {}

        @Test
        @DisplayName("should pass with all valid numeric fields")
        void shouldPassWithAllValidNumericFields() {
            assertValidation(new Product("Laptop", 999.99, 50))
                    .isValid();
        }

        @Test
        @DisplayName("should fail with invalid numeric fields")
        void shouldFailWithInvalidNumericFields() {
            assertValidation(new Product("A", -10.0, 1500))
                    .isInvalid()
                    .hasErrorCount(3);
        }
    }
}