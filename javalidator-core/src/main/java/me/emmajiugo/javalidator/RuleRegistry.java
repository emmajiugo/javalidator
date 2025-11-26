package me.emmajiugo.javalidator;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for managing validation rules.
 *
 * <p>This class maintains a thread-safe map of rule names to their implementations,
 * allowing for easy registration and lookup of validation rules.
 *
 * <p>Built-in rules are registered automatically. Users can register custom rules
 * using {@link #register(ValidationRule)}.
 *
 * <p>Example usage:
 * <pre>{@code
 * // Register a custom rule
 * RuleRegistry.register(new MyCustomRule());
 *
 * // Check if a rule exists
 * if (RuleRegistry.hasRule("custom")) {
 *     ValidationRule rule = RuleRegistry.getRule("custom");
 * }
 * }</pre>
 */
public final class RuleRegistry {

    // Initial capacity of 32 optimizes for built-in rules; map grows automatically for custom rules
    private static final Map<String, ValidationRule> rules = new ConcurrentHashMap<>(32);

    private RuleRegistry() {
        // Private constructor to prevent instantiation
    }

    /**
     * Registers a single validation rule.
     *
     * @param rule the validation rule to register
     * @throws IllegalArgumentException if rule is null or rule name is blank
     */
    public static void register(ValidationRule rule) {
        validateRule(rule);
        rules.put(rule.getName(), rule);
    }

    /**
     * Registers multiple validation rules.
     *
     * @param rulesToRegister the validation rules to register
     * @throws IllegalArgumentException if any rule is null or has blank name
     */
    public static void register(ValidationRule... rulesToRegister) {
        if (rulesToRegister == null || rulesToRegister.length == 0) {
            return;
        }
        for (ValidationRule rule : rulesToRegister) {
            validateRule(rule);
            rules.put(rule.getName(), rule);
        }
    }

    /**
     * Gets a validation rule by name.
     *
     * @param ruleName the name of the rule
     * @return the validation rule, or null if not found
     */
    public static ValidationRule getRule(String ruleName) {
        if (ruleName == null || ruleName.isBlank()) {
            return null;
        }
        return rules.get(ruleName);
    }

    /**
     * Gets a validation rule by name, throwing if not found.
     *
     * @param ruleName the name of the rule
     * @return the validation rule
     * @throws IllegalArgumentException if rule not found
     */
    public static ValidationRule getRequiredRule(String ruleName) {
        ValidationRule rule = getRule(ruleName);
        if (rule == null) {
            throw new IllegalArgumentException("Unknown validation rule: " + ruleName);
        }
        return rule;
    }

    /**
     * Checks if a rule exists in the registry.
     *
     * @param ruleName the name of the rule
     * @return true if the rule exists, false otherwise
     */
    public static boolean hasRule(String ruleName) {
        if (ruleName == null || ruleName.isBlank()) {
            return false;
        }
        return rules.containsKey(ruleName);
    }

    /**
     * Gets an unmodifiable view of all registered rules.
     *
     * @return an unmodifiable map of all registered rules
     */
    public static Map<String, ValidationRule> getAllRules() {
        return Collections.unmodifiableMap(rules);
    }

    /**
     * Gets the count of registered rules.
     *
     * @return the number of registered rules
     */
    public static int size() {
        return rules.size();
    }

    /**
     * Validates that a rule is not null and has a valid name.
     */
    private static void validateRule(ValidationRule rule) {
        if (rule == null) {
            throw new IllegalArgumentException("Validation rule cannot be null");
        }
        if (rule.getName() == null || rule.getName().isBlank()) {
            throw new IllegalArgumentException("Validation rule name cannot be null or blank");
        }
    }

    /**
     * Clears all registered rules and re-registers built-in rules.
     * Primarily for testing purposes.
     */
    static void reset() {
        rules.clear();
        BuiltInRules.registerAll();
    }

    /**
     * Clears all registered rules including built-in rules.
     * Primarily for testing purposes.
     */
    static void clearAll() {
        rules.clear();
    }
}