package me.emmajiugo.javalidator.rules;

import me.emmajiugo.javalidator.annotations.Rule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static me.emmajiugo.javalidator.rules.ValidationTestHelper.assertValidation;

/**
 * Tests for conditional validation rules: required_if, required_unless, confirmed
 */
@DisplayName("Conditional Validation Rules")
class ConditionalRulesTest {

    @Nested
    @DisplayName("Required If Rule")
    class RequiredIfRuleTests {

        record Order(
                String paymentMethod,

                @Rule("required_if:paymentMethod,card")
                String cardNumber
        ) {}

        record MultipleConditions(
                String country,

                @Rule("required_if:country,USA")
                String state,

                @Rule("required_if:country,USA")
                String zipCode
        ) {}

        @Test
        @DisplayName("should require field when condition matches")
        void shouldRequireFieldWhenConditionMatches() {
            assertValidation(new Order("card", null))
                    .hasSingleError()
                    .hasErrorOn("cardNumber")
                    .withMessageContaining("required");

            assertValidation(new Order("card", ""))
                    .hasSingleError()
                    .hasErrorOn("cardNumber");
        }

        @Test
        @DisplayName("should pass when condition matches and field is provided")
        void shouldPassWhenConditionMatchesAndFieldIsProvided() {
            assertValidation(new Order("card", "1234-5678-9012-3456"))
                    .isValid();
        }

        @Test
        @DisplayName("should not require field when condition doesn't match")
        void shouldNotRequireFieldWhenConditionDoesntMatch() {
            assertValidation(new Order("cash", null))
                    .isValid();

            assertValidation(new Order("paypal", ""))
                    .isValid();
        }

        @Test
        @DisplayName("should handle multiple conditional fields")
        void shouldHandleMultipleConditionalFields() {
            var validation = assertValidation(new MultipleConditions("USA", null, null))
                    .isInvalid()
                    .hasErrorCount(2);
            validation.hasErrorOn("state");
            validation.hasErrorOn("zipCode");

            assertValidation(new MultipleConditions("Canada", null, null))
                    .isValid();
        }
    }

    @Nested
    @DisplayName("Required Unless Rule")
    class RequiredUnlessRuleTests {

        record Shipping(
                String deliveryMethod,

                @Rule("required_unless:deliveryMethod,pickup")
                String address
        ) {}

        @Test
        @DisplayName("should require field unless condition matches")
        void shouldRequireFieldUnlessConditionMatches() {
            assertValidation(new Shipping("delivery", null))
                    .hasSingleError()
                    .hasErrorOn("address")
                    .withMessageContaining("required");

            assertValidation(new Shipping("mail", ""))
                    .hasSingleError()
                    .hasErrorOn("address");
        }

        @Test
        @DisplayName("should not require field when condition matches")
        void shouldNotRequireFieldWhenConditionMatches() {
            assertValidation(new Shipping("pickup", null))
                    .isValid();

            assertValidation(new Shipping("pickup", ""))
                    .isValid();
        }

        @Test
        @DisplayName("should pass when field is provided regardless of condition")
        void shouldPassWhenFieldIsProvidedRegardlessOfCondition() {
            assertValidation(new Shipping("delivery", "123 Main St"))
                    .isValid();

            assertValidation(new Shipping("pickup", "123 Main St"))
                    .isValid();
        }
    }

    @Nested
    @DisplayName("Confirmed Rule")
    class ConfirmedRuleTests {

        record PasswordWithDefaultConfirmation(
                @Rule("required|min:8")
                @Rule("confirmed")
                String password,

                String password_confirmation
        ) {}

        record PasswordWithCustomConfirmation(
                @Rule("required|min:8")
                @Rule("confirmed:passwordConfirm")
                String password,

                String passwordConfirm
        ) {}

        record RegistrationForm(
                String accountType,

                @Rule("required_if:accountType,business")
                String companyName,

                @Rule("required_unless:accountType,business")
                String taxId
        ) {}

        @Test
        @DisplayName("should pass when password matches confirmation (default field name)")
        void shouldPassWhenPasswordMatchesConfirmationDefault() {
            assertValidation(new PasswordWithDefaultConfirmation("password123", "password123"))
                    .isValid();
        }

        @Test
        @DisplayName("should fail when password doesn't match confirmation (default field name)")
        void shouldFailWhenPasswordDoesntMatchConfirmationDefault() {
            assertValidation(new PasswordWithDefaultConfirmation("password123", "different"))
                    .hasSingleError()
                    .hasErrorOn("password")
                    .withMessageContaining("confirmation");
        }

        @Test
        @DisplayName("should pass when password matches confirmation (custom field name)")
        void shouldPassWhenPasswordMatchesConfirmationCustom() {
            assertValidation(new PasswordWithCustomConfirmation("password123", "password123"))
                    .isValid();
        }

        @Test
        @DisplayName("should fail when password doesn't match confirmation (custom field name)")
        void shouldFailWhenPasswordDoesntMatchConfirmationCustom() {
            assertValidation(new PasswordWithCustomConfirmation("password123", "different"))
                    .hasSingleError()
                    .hasErrorOn("password")
                    .withMessageContaining("confirmation");
        }

        @Test
        @DisplayName("should combine conditional rules with other validations")
        void shouldCombineConditionalRulesWithOtherValidations() {
            // Personal account with all fields
            assertValidation(new RegistrationForm("personal", null, "123456789"))
                    .isValid();

            // Business account with all fields
            assertValidation(new RegistrationForm("business", "Acme Corp", "12-3456789"))
                    .isValid();

            // Business account missing company name
            assertValidation(new RegistrationForm("business", null, "123456789"))
                    .hasSingleError()
                    .hasErrorOn("companyName");

            // Personal account with no tax ID (should fail - required unless business)
            assertValidation(new RegistrationForm("personal", null, null))
                    .hasSingleError()
                    .hasErrorOn("taxId");
        }
    }
}