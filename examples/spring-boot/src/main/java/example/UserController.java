package example;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @PostMapping
    public ResponseEntity<String> createUser(@RequestBody UserDTO userDTO) {
        // Validation happens automatically via ValidationAspect
        // If we reach here, validation passed

        // Process valid user...
        return ResponseEntity.ok("User created: " + userDTO.username());
    }
}