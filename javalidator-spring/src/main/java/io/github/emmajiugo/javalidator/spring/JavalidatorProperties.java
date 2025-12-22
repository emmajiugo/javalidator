package io.github.emmajiugo.javalidator.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Javalidator auto-configuration.
 *
 * <p>Properties can be configured in application.properties or application.yml:
 * <pre>
 * javalidator.enabled=true
 * javalidator.max-class-hierarchy-depth=10
 * javalidator.validate-field-names=true
 * javalidator.field-name-pattern=^[a-zA-Z_][a-zA-Z0-9_]*$
 * javalidator.strict-mode=false
 * javalidator.aspect.enabled=true
 * javalidator.exception-handler.enabled=true
 * javalidator.exception-handler.include-path=true
 * javalidator.exception-handler.include-timestamp=true
 * </pre>
 */
@ConfigurationProperties(prefix = "javalidator")
public class JavalidatorProperties {

    /**
     * Whether to enable Javalidator auto-configuration.
     */
    private boolean enabled = true;

    /**
     * Maximum depth when traversing class hierarchy for fields.
     * Prevents memory exhaustion attacks via deeply nested classes.
     */
    private int maxClassHierarchyDepth = 10;

    /**
     * Whether to validate field names used in conditional rules.
     */
    private boolean validateFieldNames = true;

    /**
     * Regex pattern for valid field names.
     */
    private String fieldNamePattern = "^[a-zA-Z_][a-zA-Z0-9_]*$";

    /**
     * Controls how configuration errors are handled.
     * When true: throws IllegalArgumentException for configuration errors (fail-fast for development)
     * When false: converts configuration errors to validation errors (production-safe)
     */
    private boolean strictMode = false;

    /**
     * AOP aspect configuration.
     */
    private final Aspect aspect = new Aspect();

    /**
     * Exception handler configuration.
     */
    private final ExceptionHandler exceptionHandler = new ExceptionHandler();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getMaxClassHierarchyDepth() {
        return maxClassHierarchyDepth;
    }

    public void setMaxClassHierarchyDepth(int maxClassHierarchyDepth) {
        this.maxClassHierarchyDepth = maxClassHierarchyDepth;
    }

    public boolean isValidateFieldNames() {
        return validateFieldNames;
    }

    public void setValidateFieldNames(boolean validateFieldNames) {
        this.validateFieldNames = validateFieldNames;
    }

    public String getFieldNamePattern() {
        return fieldNamePattern;
    }

    public void setFieldNamePattern(String fieldNamePattern) {
        this.fieldNamePattern = fieldNamePattern;
    }

    public boolean isStrictMode() {
        return strictMode;
    }

    public void setStrictMode(boolean strictMode) {
        this.strictMode = strictMode;
    }

    public Aspect getAspect() {
        return aspect;
    }

    public ExceptionHandler getExceptionHandler() {
        return exceptionHandler;
    }

    /**
     * AOP aspect configuration.
     */
    public static class Aspect {

        /**
         * Whether to enable the validation AOP aspect.
         */
        private boolean enabled = true;

        /**
         * Whether to validate GET request parameters.
         * Enabled by default to support @Rule validation on @PathVariable and @RequestParam.
         */
        private boolean validateGetRequests = true;

        /**
         * Whether to validate DELETE request parameters.
         * Enabled by default to support @Rule validation on @PathVariable and @RequestParam.
         */
        private boolean validateDeleteRequests = true;

        /**
         * Whether to enable method validation in classes annotated with @Validated.
         * This includes @Service, @Component, and other Spring beans.
         * Requires the @Validated annotation on the class.
         */
        private boolean validateServices = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isValidateGetRequests() {
            return validateGetRequests;
        }

        public void setValidateGetRequests(boolean validateGetRequests) {
            this.validateGetRequests = validateGetRequests;
        }

        public boolean isValidateDeleteRequests() {
            return validateDeleteRequests;
        }

        public void setValidateDeleteRequests(boolean validateDeleteRequests) {
            this.validateDeleteRequests = validateDeleteRequests;
        }

        public boolean isValidateServices() {
            return validateServices;
        }

        public void setValidateServices(boolean validateServices) {
            this.validateServices = validateServices;
        }
    }

    /**
     * Exception handler configuration.
     */
    public static class ExceptionHandler {

        /**
         * Whether to enable the global exception handler.
         */
        private boolean enabled = true;

        /**
         * Whether to include the request path in error responses.
         */
        private boolean includePath = true;

        /**
         * Whether to include a timestamp in error responses.
         */
        private boolean includeTimestamp = true;

        /**
         * Custom error message for validation failures.
         */
        private String message = "Validation failed";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isIncludePath() {
            return includePath;
        }

        public void setIncludePath(boolean includePath) {
            this.includePath = includePath;
        }

        public boolean isIncludeTimestamp() {
            return includeTimestamp;
        }

        public void setIncludeTimestamp(boolean includeTimestamp) {
            this.includeTimestamp = includeTimestamp;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}