package io.github.emmajiugo.javalidator.spring.handler;

import java.time.Instant;
import java.util.Map;

/**
 * Structured error response for validation failures.
 *
 * @param status    the error status (always "error")
 * @param message   the error message
 * @param errors    map of field names to their error messages
 * @param path      the request path (optional, based on configuration)
 * @param timestamp the timestamp of the error (optional, based on configuration)
 */
public record ValidationErrorResponse(
        String status,
        String message,
        Map<String, Object> errors,
        String path,
        Instant timestamp
) {

    /**
     * Builder for ValidationErrorResponse.
     */
    public static class Builder {
        private String status = "error";
        private String message = "Validation failed";
        private Map<String, Object> errors;
        private String path;
        private Instant timestamp;

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder errors(Map<String, Object> errors) {
            this.errors = errors;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public ValidationErrorResponse build() {
            return new ValidationErrorResponse(status, message, errors, path, timestamp);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}