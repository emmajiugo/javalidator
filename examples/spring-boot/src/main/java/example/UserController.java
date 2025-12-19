package example;

import io.github.emmajiugo.javalidator.annotations.Rule;
import io.github.emmajiugo.javalidator.annotations.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @PostMapping
    public ResponseEntity<String> createUser(@Valid @RequestBody UserDTO userDTO) {
        // Validation happens automatically via ValidationAspect
        // If we reach here, validation passed
        return ResponseEntity.ok("User created: " + userDTO.username());
    }

    /**
     * Example: @Rule on @PathVariable - id must be >= 1
     */
    @GetMapping("/{id}")
    public ResponseEntity<String> getUser(@PathVariable @Rule("gte:1") Long id) {
        // If we reach here, id is valid (>= 1)
        return ResponseEntity.ok("Found user with id: " + id);
    }

    /**
     * Example: @Rule on @RequestParam - validates query parameters
     */
    @GetMapping("/search")
    public ResponseEntity<String> searchUsers(
            @RequestParam @Rule("min:2|max:100") String query,
            @RequestParam(defaultValue = "0") @Rule("gte:0") Integer page,
            @RequestParam(defaultValue = "10") @Rule("gte:1|lte:100") Integer size
    ) {
        return ResponseEntity.ok("Searching for: " + query + " (page " + page + ", size " + size + ")");
    }

    /**
     * Example: Combined @Rule on @PathVariable and @Valid on @RequestBody
     */
    @PutMapping("/{id}")
    public ResponseEntity<String> updateUser(
            @PathVariable @Rule("gte:1") Long id,
            @Valid @RequestBody UserDTO userDTO
    ) {
        return ResponseEntity.ok("Updated user " + id + ": " + userDTO.username());
    }
}