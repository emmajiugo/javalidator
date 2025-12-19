package example;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import io.github.emmajiugo.javalidator.annotations.Valid;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    @POST
    @ValidateBinding
    public Response createUser(@Valid UserDTO userDTO) {
        // Validation happens automatically via ValidationInterceptor
        // If we reach here, validation passed

        // Process valid user...
        return Response.ok()
                .entity("{\"message\": \"User created: " + userDTO.username() + "\"}")
                .build();
    }
}