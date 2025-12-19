package example;

import io.github.emmajiugo.javalidator.annotations.Rule;
import io.github.emmajiugo.javalidator.annotations.Valid;
import io.github.emmajiugo.javalidator.annotations.Validated;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Example service demonstrating @Validated annotation for method parameter validation.
 *
 * Key annotations:
 * - @Validated on class: Enables method parameter validation for this service
 * - @Valid on parameters: Triggers cascaded validation of DTO fields
 * - @Rule on parameters: Validates parameter values directly
 */
@Validated
@Service
public class UserService {

    /**
     * Example: Validate a DTO parameter with @Valid (cascaded validation).
     * The UserDTO fields are validated according to their @Rule annotations.
     */
    public String createUser(@Valid UserDTO user) {
        // If we reach here, validation passed
        return "User created: " + user.username();
    }

    /**
     * Example: Validate a simple parameter with @Rule (direct validation).
     * The id must be >= 1.
     */
    public String findById(@Rule("gte:1") Long id) {
        // If we reach here, id is valid (>= 1)
        return "Found user with id: " + id;
    }

    /**
     * Example: Combine @Rule and @Valid for multiple parameter validation.
     * - id: must be >= 1
     * - user: DTO fields are validated
     */
    public String updateUser(@Rule("gte:1") Long id, @Valid UserDTO user) {
        // If we reach here, both id and user are valid
        return "Updated user " + id + ": " + user.username();
    }

    /**
     * Example: Multiple @Rule parameters for search functionality.
     * - query: must be 2-100 characters
     * - page: must be >= 0
     * - pageSize: must be 1-100
     */
    public List<String> search(
            @Rule("min:2|max:100") String query,
            @Rule("gte:0") Integer page,
            @Rule("gte:1|lte:100") Integer pageSize
    ) {
        // If we reach here, all parameters are valid
        return List.of("User matching: " + query);
    }

    /**
     * Example: Validate UUID format.
     */
    public String findByUuid(@Rule("uuid") String uuid) {
        return "Found user with UUID: " + uuid;
    }

    /**
     * Example: Validate email format directly on parameter.
     */
    public boolean checkEmailAvailable(@Rule("email") String email) {
        // Check if email is available
        return !email.equals("taken@example.com");
    }
}