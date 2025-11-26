package me.emmajiugo.javalidator.config;

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
 *   <li><b>Strict Reflection Mode</b> - Checks SecurityManager permissions before using
 *       setAccessible(), allowing integration with Java security policies</li>
 *   <li><b>Field Name Validation</b> - Validates field names in conditional rules (required_if,
 *       required_unless, confirmed) against a regex pattern to prevent field name injection</li>
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
 *     .strictReflectionMode(true)
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
    private final boolean strictReflectionMode;

    // Field name validation
    private final boolean validateFieldNames;
    private final String fieldNamePattern;

    // General security
    private final boolean strictSecurityMode;

    private ValidationConfig(Builder builder) {
        this.maxClassHierarchyDepth = builder.maxClassHierarchyDepth;
        this.strictReflectionMode = builder.strictReflectionMode;
        this.validateFieldNames = builder.validateFieldNames;
        this.fieldNamePattern = builder.fieldNamePattern;
        this.strictSecurityMode = builder.strictSecurityMode;
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
                .strictSecurityMode(true)
                .strictReflectionMode(true)
                .validateFieldNames(true)
                .build();
    }

    /**
     * Creates a permissive configuration for development environments.
     */
    public static ValidationConfig permissive() {
        return builder()
                .strictSecurityMode(false)
                .strictReflectionMode(false)
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

    public boolean isStrictReflectionMode() {
        return strictReflectionMode;
    }

    public boolean isValidateFieldNames() {
        return validateFieldNames;
    }

    public String getFieldNamePattern() {
        return fieldNamePattern;
    }

    public boolean isStrictSecurityMode() {
        return strictSecurityMode;
    }

    public static class Builder {
        private int maxClassHierarchyDepth = 10;
        private boolean strictReflectionMode = false;
        private boolean validateFieldNames = true;
        private String fieldNamePattern = "^[a-zA-Z_][a-zA-Z0-9_]*$";
        private boolean strictSecurityMode = false;

        /**
         * Sets the maximum depth when traversing class hierarchy for fields.
         * Prevents memory exhaustion attacks. Default: 10
         */
        public Builder maxClassHierarchyDepth(int maxClassHierarchyDepth) {
            this.maxClassHierarchyDepth = maxClassHierarchyDepth;
            return this;
        }

        /**
         * Enables strict reflection mode which checks SecurityManager permissions.
         * Default: false
         */
        public Builder strictReflectionMode(boolean strictReflectionMode) {
            this.strictReflectionMode = strictReflectionMode;
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

        /**
         * Enables all strict security features.
         * When true, overrides individual settings to maximum security.
         */
        public Builder strictSecurityMode(boolean strictSecurityMode) {
            this.strictSecurityMode = strictSecurityMode;
            if (strictSecurityMode) {
                this.strictReflectionMode = true;
                this.validateFieldNames = true;
            }
            return this;
        }

        public ValidationConfig build() {
            return new ValidationConfig(this);
        }
    }
}
