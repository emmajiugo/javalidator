package example;

import me.emmajiugo.javalidator.annotations.Rule;

public record AddressDTO(
        @Rule("required")
        String street,

        @Rule("required")
        String city,

        @Rule("required|digits:5")
        String zipCode
) {}