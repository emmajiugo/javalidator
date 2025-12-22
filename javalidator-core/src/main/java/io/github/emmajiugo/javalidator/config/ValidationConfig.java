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
 * <h2>Configuration Error Handling</h2>
 * <ul>
 *   <li><b>Strict Mode (OFF by default)</b> - Controls how configuration errors are handled:
 *     <ul>
 *       <li>When FALSE (production): Configuration errors become validation errors, preventing crashes</li>
 *       <li>When TRUE (development/testing): Configuration errors throw IllegalArgumentException immediately</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * <h2>Configuration Presets</h2>
 * <ul>
 *   <li><b>defaults()</b> - Balanced security and performance for production use (strictMode: false)</li>
 *   <li><b>strict()</b> - Maximum security for development/testing (strictMode: true)</li>
 *   <li><b>permissive()</b> - Minimal checks for development/testing environments (strictMode: false)</li>
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

    // Configuration error handling
    private final boolean strictMode;

    private ValidationConfig(Builder builder) {
        this.maxClassHierarchyDepth = builder.maxClassHierarchyDepth;
        this.validateFieldNames = builder.validateFieldNames;
        this.fieldNamePattern = builder.fieldNamePattern;
        this.strictMode = builder.strictMode;
    }

    /**
     * Creates a default configuration with recommended security settings.
     */
    public static ValidationConfig defaults() {
        return builder().build();
    }

    /**
     * Creates a strict configuration for development/testing environments.
     * Enables all security features and throws exceptions for configuration errors.
     */
    public static ValidationConfig strict() {
        return builder()
                .validateFieldNames(true)
                .strictMode(true)
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

    public boolean isStrictMode() {
        return strictMode;
    }

    public static class Builder {
        private int maxClassHierarchyDepth = 10;
        private boolean validateFieldNames = true;
        private String fieldNamePattern = "^[a-zA-Z_][a-zA-Z0-9_]*$";
        private boolean strictMode = false;

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

        /**
         * Controls how configuration errors are handled.
         * When true: throws IllegalArgumentException for configuration errors (fail-fast for development)
         * When false: converts configuration errors to validation errors (production-safe)
         * Default: false
         */
        public Builder strictMode(boolean strictMode) {
            this.strictMode = strictMode;
            return this;
        }

        public ValidationConfig build() {
            return new ValidationConfig(this);
        }
    }
}
