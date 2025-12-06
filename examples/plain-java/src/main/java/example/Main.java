package example;

import io.github.emmajiugo.javalidator.Validator;
import io.github.emmajiugo.javalidator.annotations.Rule;
import io.github.emmajiugo.javalidator.model.ValidationResponse;

/**
 * Simple plain Java example showing basic Javalidator usage.
 */
public class Main {

    public record UserDTO(
            @Rule("required|min:3|max:20")
            String username,

            @Rule("required|email")
            String email,

            @Rule("required|gte:18")
            Integer age
    ) {}

    public static void main(String[] args) {
        // Valid user
        UserDTO user = new UserDTO("john", "john@example.com", 25);
        ValidationResponse response = Validator.validate(user);

        if (response.valid()) {
            System.out.println("✓ User is valid!");
        } else {
            System.out.println("✗ Validation errors:");
            response.errors().forEach(error ->
                    System.out.println("  " + error.field() + ": " + error.messages())
            );
        }
    }
}