package io.github.emmajiugo.javalidator.spring.handler;

import io.github.emmajiugo.javalidator.exception.NotValidException;
import io.github.emmajiugo.javalidator.model.ValidationError;
import io.github.emmajiugo.javalidator.spring.JavalidatorProperties;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Global exception handler for Javalidator {@link NotValidException}.
 *
 * <p>Converts validation errors into structured HTTP 400 responses.
 * Response format can be customized via properties.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private final JavalidatorProperties properties;

    public GlobalExceptionHandler(JavalidatorProperties properties) {
        this.properties = properties;
    }

    @ResponseBody
    @ExceptionHandler(NotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationException(
            NotValidException ex,
            HttpServletRequest request
    ) {
        Map<String, Object> errors = new LinkedHashMap<>();
        for (ValidationError validationError : ex.getErrors()) {
            String field = validationError.field();
            if (errors.containsKey(field)) {
                @SuppressWarnings("unchecked")
                Map<String, Object> existing = (Map<String, Object>) errors.get(field);
                @SuppressWarnings("unchecked")
                List<String> existingMessages = (List<String>) existing.get("messages");
                @SuppressWarnings("unchecked")
                List<String> existingRules = (List<String>) existing.get("rules");

                List<String> mergedMessages = new ArrayList<>(existingMessages);
                mergedMessages.addAll(validationError.messages());
                List<String> mergedRules = new ArrayList<>(existingRules);
                mergedRules.addAll(validationError.rules());

                existing.put("messages", mergedMessages);
                existing.put("rules", mergedRules);
            } else {
                Map<String, Object> fieldError = new LinkedHashMap<>();
                fieldError.put("messages", validationError.messages());
                fieldError.put("rules", validationError.rules());
                errors.put(field, fieldError);
            }
        }

        JavalidatorProperties.ExceptionHandler handlerProps = properties.getExceptionHandler();

        ValidationErrorResponse.Builder builder = ValidationErrorResponse.builder()
                .status("error")
                .message(handlerProps.getMessage())
                .errors(errors);

        if (handlerProps.isIncludePath()) {
            builder.path(request.getRequestURI());
        }

        if (handlerProps.isIncludeTimestamp()) {
            builder.timestamp(Instant.now());
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(builder.build());
    }
}