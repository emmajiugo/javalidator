package io.github.emmajiugo.javalidator;

import io.github.emmajiugo.javalidator.annotations.Rule;
import io.github.emmajiugo.javalidator.annotations.RuleCascade;
import io.github.emmajiugo.javalidator.config.ValidationConfig;
import io.github.emmajiugo.javalidator.exception.ValidationException;
import io.github.emmajiugo.javalidator.model.ValidationError;
import io.github.emmajiugo.javalidator.model.ValidationResponse;
import io.github.emmajiugo.javalidator.rules.EnumRule;
import io.github.emmajiugo.javalidator.util.ReflectionUtils;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Main validation entry point for validating objects using annotation-based rules.
 *
 * <p>This validator supports both Java records and regular classes. Fields are validated
 * based on {@link Rule} annotations which can specify validation rules using a
 * Laravel-inspired pipe syntax (e.g., "required|email|max:255").
 *
 * <p>Example usage:
 * <pre>{@code
 * public record UserRequest(
 *     @Rule("required|min:3|max:50")
 *     String username,
 *
 *     @Rule("required|email")
 *     String email
 * ) {}
 *
 * ValidationResponse response = Validator.validate(new UserRequest("john", "john@example.com"));
 * if (!response.valid()) {
 *     response.errors().forEach(error -> System.out.println(error.field() + ": " + error.messages()));
 * }
 * }</pre>
 *
 * @see Rule
 * @see ValidationResponse
 */
public final class Validator {

    private static volatile ValidationConfig config = ValidationConfig.defaults();
    private static volatile boolean initialized = false;

    private Validator() {
        // Private constructor to prevent instantiation
    }

    /**
     * Ensures built-in rules are registered (lazy initialization).
     */
    private static void ensureInitialized() {
        if (!initialized) {
            synchronized (Validator.class) {
                if (!initialized) {
                    BuiltInRules.registerAll();
                    initialized = true;
                }
            }
        }
    }

    /**
     * Sets the global validation configuration.
     *
     * @param config the validation configuration to use
     * @throws IllegalArgumentException if config is null
     */
    public static void setConfig(ValidationConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("ValidationConfig cannot be null");
        }
        Validator.config = config;
    }

    /**
     * Gets the current validation configuration.
     *
     * @return the current validation configuration
     */
    public static ValidationConfig getConfig() {
        return config;
    }

    /**
     * Validates an object (record or class) based on @Rule annotations.
     *
     * @param dto the object to validate
     * @return a ValidationResponse containing validation results
     * @throws IllegalArgumentException if an unknown rule is encountered
     */
    public static ValidationResponse validate(Object dto) {
        ensureInitialized();

        if (dto == null) {
            return ValidationResponse.failure(List.of(
                    new ValidationError("object", List.of("Validation object cannot be null"))
            ));
        }

        Class<?> clazz = dto.getClass();
        List<ValidationError> errors = clazz.isRecord()
                ? validateRecord(dto, clazz)
                : validateClass(dto, clazz);

        return errors.isEmpty()
                ? ValidationResponse.success()
                : ValidationResponse.failure(errors);
    }

    /**
     * Validates an object and throws an exception if validation fails.
     *
     * @param dto the object to validate
     * @throws ValidationException if validation fails
     */
    public static void validateOrThrow(Object dto) {
        ValidationResponse response = validate(dto);
        if (!response.valid()) {
            throw new ValidationException("Validation failed", response.errors());
        }
    }

    /**
     * Validates a single value against the specified rules.
     *
     * <p>This method is useful for validating method parameters directly,
     * without wrapping them in a DTO object. It is used internally by
     * framework adapters when {@link io.github.emmajiugo.javalidator.annotations.Rule}
     * is applied to method parameters.
     *
     * <p>Example:
     * <pre>{@code
     * ValidationResponse response = Validator.validateValue(userId, "required|min:1", "userId");
     * if (!response.valid()) {
     *     // Handle validation error
     * }
     * }</pre>
     *
     * <p><strong>Note:</strong> Conditional rules (like {@code required_if}, {@code same},
     * {@code different}) that require access to other fields cannot be used with this method
     * and will throw an {@link IllegalArgumentException}.
     *
     * @param value the value to validate
     * @param rules the validation rules (pipe-separated, e.g., "required|min:1")
     * @param fieldName the name to use in error messages (typically the parameter name)
     * @return a ValidationResponse containing validation results
     * @throws IllegalArgumentException if a conditional rule is used that requires DTO context
     */
    public static ValidationResponse validateValue(Object value, String rules, String fieldName) {
        ensureInitialized();

        if (rules == null || rules.isBlank()) {
            return ValidationResponse.success();
        }

        List<String> errors = new ArrayList<>();
        String[] ruleDefinitions = rules.split("\\|");

        for (String ruleDefinition : ruleDefinitions) {
            String trimmed = ruleDefinition.trim();
            if (trimmed.isEmpty()) {
                continue;
            }

            RuleDefinition parsed = RuleDefinition.parse(trimmed);
            ValidationRule rule = RuleRegistry.getRequiredRule(parsed.name());

            // Conditional rules require DTO context - not supported for single value validation
            if (rule instanceof ConditionalValidationRule) {
                throw new IllegalArgumentException(
                        "Rule '" + parsed.name() + "' requires DTO context and cannot be used " +
                        "for single value validation. Use object validation with Validator.validate() instead.");
            }

            // EnumRule requires enumClass from annotation - not supported for single value validation
            if (rule instanceof io.github.emmajiugo.javalidator.rules.EnumRule) {
                throw new IllegalArgumentException(
                        "The 'enum' rule requires an enumClass parameter from @Rule annotation " +
                        "and cannot be used with validateValue(). Use object validation instead.");
            }

            String error = rule.validate(fieldName, value, parsed.parameter());
            if (error != null) {
                errors.add(error);
            }
        }

        if (errors.isEmpty()) {
            return ValidationResponse.success();
        }

        return ValidationResponse.failure(List.of(new ValidationError(fieldName, errors)));
    }

    /**
     * Validates a single value and throws an exception if validation fails.
     *
     * <p>This is a convenience method that calls {@link #validateValue(Object, String, String)}
     * and throws a {@link ValidationException} if the value fails validation.
     *
     * @param value the value to validate
     * @param rules the validation rules (pipe-separated, e.g., "required|min:1")
     * @param fieldName the name to use in error messages (typically the parameter name)
     * @throws ValidationException if validation fails
     * @throws IllegalArgumentException if a conditional rule is used that requires DTO context
     * @see #validateValue(Object, String, String)
     */
    public static void validateValueOrThrow(Object value, String rules, String fieldName) {
        ValidationResponse response = validateValue(value, rules, fieldName);
        if (!response.valid()) {
            throw new ValidationException("Validation failed for " + fieldName, response.errors());
        }
    }

    private static List<ValidationError> validateRecord(Object dto, Class<?> clazz) {
        List<ValidationError> errors = new ArrayList<>();

        for (RecordComponent component : clazz.getRecordComponents()) {
            String fieldName = component.getName();
            Object value = ReflectionUtils.getRecordComponentValue(dto, component);

            List<String> fieldErrors = validateAnnotatedElement(fieldName, value, component, dto);
            if (!fieldErrors.isEmpty()) {
                errors.add(new ValidationError(fieldName, fieldErrors));
            }
        }

        return errors;
    }

    private static List<ValidationError> validateClass(Object dto, Class<?> clazz) {
        List<ValidationError> errors = new ArrayList<>();

        for (Field field : ReflectionUtils.getAllFields(clazz)) {
            field.setAccessible(true);

            String fieldName = field.getName();
            Object value = ReflectionUtils.getFieldValue(dto, field);

            List<String> fieldErrors = validateAnnotatedElement(fieldName, value, field, dto);
            if (!fieldErrors.isEmpty()) {
                errors.add(new ValidationError(fieldName, fieldErrors));
            }
        }

        return errors;
    }

    private static List<String> validateAnnotatedElement(
            String fieldName, Object value, AnnotatedElement element, Object dto) {

        List<String> errors = new ArrayList<>();

        // Process all @Rule annotations
        for (Rule ruleAnnotation : element.getAnnotationsByType(Rule.class)) {
            errors.addAll(processRuleAnnotation(fieldName, value, ruleAnnotation, dto));
        }

        // Process @RuleCascade for nested validation
        if (element.isAnnotationPresent(RuleCascade.class)) {
            errors.addAll(validateCascade(fieldName, value));
        }

        return errors;
    }

    private static List<String> processRuleAnnotation(
            String fieldName, Object value, Rule ruleAnnotation, Object dto) {

        List<String> errors = new ArrayList<>();
        String[] ruleDefinitions = ruleAnnotation.value().split("\\|");

        for (String ruleDefinition : ruleDefinitions) {
            String error = applyRule(fieldName, value, ruleDefinition.trim(), ruleAnnotation, dto);
            if (error != null) {
                errors.add(error);
            }
        }

        return errors;
    }

    private static String applyRule(
            String fieldName, Object value, String ruleDefinition, Rule ruleAnnotation, Object dto) {

        RuleDefinition parsed = RuleDefinition.parse(ruleDefinition);
        ValidationRule rule = RuleRegistry.getRequiredRule(parsed.name());

        String error = executeRule(rule, fieldName, value, parsed.parameter(), ruleAnnotation, dto);

        // Return custom message if provided
        if (error != null && !ruleAnnotation.message().isEmpty()) {
            return ruleAnnotation.message();
        }

        return error;
    }

    private static String executeRule(
            ValidationRule rule, String fieldName, Object value,
            String parameter, Rule ruleAnnotation, Object dto) {

        // Special handling for EnumRule
        if (rule instanceof EnumRule enumRule) {
            return enumRule.validateWithEnumClass(fieldName, value, ruleAnnotation.enumClass());
        }

        // Conditional rules need full context
        if (rule instanceof ConditionalValidationRule conditionalRule) {
            return conditionalRule.validateWithContext(fieldName, value, parameter, dto);
        }

        // Standard rule validation
        return rule.validate(fieldName, value, parameter);
    }

    private static List<String> validateCascade(String fieldName, Object value) {
        if (value == null) {
            return List.of();
        }

        List<String> errors = new ArrayList<>();

        if (value instanceof Collection<?> collection) {
            validateCollection(fieldName, collection, errors);
        } else if (value.getClass().isArray()) {
            validateArray(fieldName, (Object[]) value, errors);
        } else {
            validateNestedObject(fieldName, value, errors);
        }

        return errors;
    }

    private static void validateCollection(String fieldName, Collection<?> collection, List<String> errors) {
        int index = 0;
        for (Object item : collection) {
            if (item != null) {
                collectNestedErrors(fieldName + "[" + index + "]", item, errors);
            }
            index++;
        }
    }

    private static void validateArray(String fieldName, Object[] array, List<String> errors) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] != null) {
                collectNestedErrors(fieldName + "[" + i + "]", array[i], errors);
            }
        }
    }

    private static void validateNestedObject(String fieldName, Object value, List<String> errors) {
        collectNestedErrors(fieldName, value, errors);
    }

    private static void collectNestedErrors(String prefix, Object value, List<String> errors) {
        ValidationResponse response = validate(value);
        if (!response.valid()) {
            for (ValidationError error : response.errors()) {
                String nestedFieldName = prefix + "." + error.field();
                for (String msg : error.messages()) {
                    errors.add(nestedFieldName + ": " + msg);
                }
            }
        }
    }

    /**
     * Resets the validator to initial state (for testing).
     */
    static void reset() {
        config = ValidationConfig.defaults();
        initialized = false;
        RuleRegistry.reset();
    }
}