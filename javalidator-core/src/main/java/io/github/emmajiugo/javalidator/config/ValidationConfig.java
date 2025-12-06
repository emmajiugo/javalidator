package io.github.emmajiugo.javalidator.config;

/**
 * Configuration class for controlling validation behavior and security settings.
 *
 * <p>This class allows customization of validation rules, performance limits,
 * and security policies to protect against potential attacks.
 *
 * <h2>Security Features</h2>
 * <ul>
 *   <li><b>Max Class Hierarchy Depth</b> - Limits how deep reflection traverses inheritance
 *       hierarchies to prevent memory exhaustion attacks via deeply nested classes</li>
 *   <li><b>Field Name Validation</b> - Validates field names in conditional rules (required_if,
 *       required_unless, same, different) against a regex pattern to prevent field name injection</li>
 *   <li><b>Type-Safe Enum Validation</b> - Enum validation requires compile-time Class<?> parameter,
 *       preventing arbitrary class loading attacks</li>
 *   <li><b>Pattern Caching</b> - RegexRule caches compiled patterns for performance without
 *       timeout overhead (ReDoS isn't a concern since developers control patterns at compile-time)</li>
 * </ul>
 *
 * <h2>Configuration Presets</h2>
 * <ul>
 *   <li><b>defaults()</b> - Balanced security and performance for production use</li>
 *   <li><b>strict()</b> - Maximum security with all protections enabled</li>
 *   <li><b>permissive()</b> - Minimal checks for development/testing environments</li>
 * </ul>
 *
 * <p>Example usage:
 * <pre>
 * {@code
 * // Use strict security for production
 * ValidationConfig config = ValidationConfig.strict();
 * Validator.setConfig(config);
 *
 * // Or customize settings
 * ValidationConfig custom = ValidationConfig.builder()
 *     .maxClassHierarchyDepth(10)
 *     .validateFieldNames(true)
 *     .fieldNamePattern("^[a-zA-Z_][a-zA-Z0-9_]*$")
 *     .build();
 * Validator.setConfig(custom);
 * }
 * </pre>
 */
public class ValidationConfig {

    // Reflection security settings
    private final int maxClassHierarchyDepth;

    // Field name validation
    private final boolean validateFieldNames;
    private final String fieldNamePattern;

    private ValidationConfig(Builder builder) {
        this.maxClassHierarchyDepth = builder.maxClassHierarchyDepth;
        this.validateFieldNames = builder.validateFieldNames;
        this.fieldNamePattern = builder.fieldNamePattern;
    }

    /**
     * Creates a default configuration with recommended security settings.
     */
    public static ValidationConfig defaults() {
        return builder().build();
    }

    /**
     * Creates a strict configuration with maximum security.
     */
    public static ValidationConfig strict() {
        return builder()
                .validateFieldNames(true)
                .build();
    }

    /**
     * Creates a permissive configuration for development environments.
     */
    public static ValidationConfig permissive() {
        return builder()
                .validateFieldNames(false)
                .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    // Getters
    public int getMaxClassHierarchyDepth() {
        return maxClassHierarchyDepth;
    }

    public boolean isValidateFieldNames() {
        return validateFieldNames;
    }

    public String getFieldNamePattern() {
        return fieldNamePattern;
    }

    public static class Builder {
        private int maxClassHierarchyDepth = 10;
        private boolean validateFieldNames = true;
        private String fieldNamePattern = "^[a-zA-Z_][a-zA-Z0-9_]*$";

        /**
         * Sets the maximum depth when traversing class hierarchy for fields.
         * Prevents memory exhaustion attacks. Default: 10
         */
        public Builder maxClassHierarchyDepth(int maxClassHierarchyDepth) {
            this.maxClassHierarchyDepth = maxClassHierarchyDepth;
            return this;
        }

        /**
         * Validates field names used in conditional rules against a pattern.
         * Default: true
         */
        public Builder validateFieldNames(boolean validateFieldNames) {
            this.validateFieldNames = validateFieldNames;
            return this;
        }

        /**
         * Sets the regex pattern for valid field names.
         * Default: ^[a-zA-Z_][a-zA-Z0-9_]*$
         */
        public Builder fieldNamePattern(String fieldNamePattern) {
            this.fieldNamePattern = fieldNamePattern;
            return this;
        }

        public ValidationConfig build() {
            return new ValidationConfig(this);
        }
    }
}
