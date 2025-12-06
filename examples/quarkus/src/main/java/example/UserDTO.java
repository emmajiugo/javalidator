package example;

import io.github.emmajiugo.javalidator.annotations.Rule;

public record UserDTO(
        @Rule("required|min:3|max:20")
        String username,

        @Rule("required|email")
        String email,

        @Rule("required|gte:18")
        Integer age
) {}