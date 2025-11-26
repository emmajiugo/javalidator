package example;

import me.emmajiugo.javalidator.annotations.Rule;
import me.emmajiugo.javalidator.annotations.RuleCascade;
import me.emmajiugo.javalidator.annotations.Validate;

@Validate
public record UserDTO(
        @Rule("required|min:3|max:20")
        String username,

        @Rule("required|email")
        String email,

        @Rule("required|gte:18")
        Integer age,

        @RuleCascade  // Validates nested Address object
        AddressDTO address
) {}