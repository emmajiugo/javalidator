package example;

import io.github.emmajiugo.javalidator.annotations.Rule;
import io.github.emmajiugo.javalidator.annotations.RuleCascade;
import io.github.emmajiugo.javalidator.annotations.Validate;

@Validate
public record UserDTO(
        @Rule("required|min:3|max:20|noreservedwords")
        String username,

        @Rule("required|email")
        String email,

        @Rule("required|gte:18")
        Integer age,

        @RuleCascade  // Validates nested Address object
        AddressDTO address
) {}