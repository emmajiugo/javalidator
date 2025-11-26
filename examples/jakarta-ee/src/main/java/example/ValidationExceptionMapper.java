package example;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import me.emmajiugo.javalidator.exception.ValidationException;
import me.emmajiugo.javalidator.model.ValidationError;

import java.util.HashMap;
import java.util.Map;

@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ValidationException> {

    @Override
    public Response toResponse(ValidationException exception) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", "error");
        errorResponse.put("message", "Validation failed");

        Map<String, Object> errors = new HashMap<>();
        for (ValidationError validationError : exception.getErrors()) {
            errors.put(validationError.field(), validationError.messages());
        }
        errorResponse.put("errors", errors);

        return Response.status(Response.Status.BAD_REQUEST)
                .entity(errorResponse)
                .build();
    }
}