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
import java.util.LinkedHashMap;
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
            errors.put(validationError.field(), validationError.messages());
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