package example;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import me.emmajiugo.javalidator.annotations.Validate;

@Path("/api/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    @POST
    @ValidateBinding
    public String createUser(@Validate UserDTO userDTO) {
        // Validation happens automatically via ValidationInterceptor
        // If we reach here, validation passed

        // Process valid user...
        return "{\"message\": \"User created: " + userDTO.username() + "\"}";
    }
}