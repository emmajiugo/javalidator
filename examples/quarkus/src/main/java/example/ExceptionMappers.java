package example;

import jakarta.ws.rs.core.Response;
import io.github.emmajiugo.javalidator.exception.NotValidException;
import io.github.emmajiugo.javalidator.model.ValidationError;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import java.util.HashMap;
import java.util.Map;

public class ExceptionMappers {

    @ServerExceptionMapper
    public Response mapValidationException(NotValidException exception) {
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