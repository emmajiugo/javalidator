# Spring Boot Integration

This guide shows how to integrate Javalidator with Spring Boot using the official Spring Boot Starter.

## Quick Start (Recommended)

The easiest way to use Javalidator with Spring Boot is with the official starter, which auto-configures everything for you.

### Step 1: Add Dependencies

```xml
<!-- Javalidator Spring Boot Starter -->
<dependency>
    <groupId>io.github.emmajiugo</groupId>
    <artifactId>javalidator-spring</artifactId>
    <version>0.3.2</version>
</dependency>

<!-- Required for AOP-based validation -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>
```

That's it! The starter automatically configures:
- **ValidationAspect** - Intercepts REST controller methods and validates parameters
- **GlobalExceptionHandler** - Returns structured HTTP 400 responses for validation failures
- **Custom Rule Registration** - Any `ValidationRule` beans are auto-registered

### Step 2: Create Your DTO

```java
import io.github.emmajiugo.javalidator.annotations.Rule;
import io.github.emmajiugo.javalidator.annotations.RuleCascade;

public record UserDTO(
        @Rule("required|min:3|max:20")
        String username,

        @Rule("required|email")
        String email,

        @Rule("required|gte:18")
        Integer age,

        @RuleCascade  // Validates nested objects
        AddressDTO address
) {}
```

### Step 3: Use in Your Controller

```java
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.github.emmajiugo.javalidator.annotations.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @PostMapping
    public ResponseEntity<String> createUser(@Valid @RequestBody UserDTO dto) {
        // Validation happens automatically via @Valid!
        // If validation fails, a 400 response is returned
        return ResponseEntity.ok("User created: " + dto.username());
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserDTO dto) {
        return ResponseEntity.ok("User updated: " + dto.username());
    }
}
```

**Note**: Use `@Valid` on method parameters to trigger cascaded validation of the DTO's fields.

## Error Response Format

When validation fails, the API returns HTTP 400 with:

```json
{
  "status": "error",
  "message": "Validation failed",
  "errors": {
    "username": [
      "The username field is required.",
      "The username must be at least 3 characters."
    ],
    "email": [
      "The email must be a valid email address."
    ],
    "age": [
      "The age must be at least 18."
    ]
  },
  "path": "/api/users",
  "timestamp": "2025-12-16T21:52:11.835557Z"
}
```

## Configuration

Configure Javalidator via `application.properties` or `application.yml`:

```yaml
javalidator:
  enabled: true                          # Enable/disable entire auto-config (default: true)
  max-class-hierarchy-depth: 10          # Security: max inheritance depth (default: 10)
  validate-field-names: true             # Security: validate field names (default: true)
  strict-mode: false                     # Fail-fast on config errors (default: false)

  aspect:
    enabled: true                        # Enable/disable AOP aspect (default: true)
    validate-get-requests: true          # Validate GET parameters (default: true)
    validate-delete-requests: true       # Validate DELETE parameters (default: true)
    validate-services: true              # Validate @Service/@Component with @Validated (default: true)

  exception-handler:
    enabled: true                        # Enable/disable exception handler (default: true)
    include-path: true                   # Include request path in response (default: true)
    include-timestamp: true              # Include timestamp in response (default: true)
    message: "Validation failed"         # Custom error message (default: "Validation failed")
```

### Strict Mode

Controls how configuration errors are handled:

**Production (strict-mode: false - default):**
- Configuration errors become validation errors
- Service never crashes due to misconfigured rules
- Errors prefixed with `[CONFIG ERROR]` in response

**Development/Testing (strict-mode: true):**
- Configuration errors throw `IllegalArgumentException`
- Fail-fast behavior catches issues early
- Use in test environments to catch misconfigured rules

**Example:**
```yaml
# application-dev.yml
javalidator:
  strict-mode: true  # Catch config errors during development

# application-prod.yml
javalidator:
  strict-mode: false  # Prevent crashes in production (default)
```

See the [Security Guide](../security.md#5-configuration-error-handling-strict-mode) for more details.

## Service Layer Validation

Javalidator supports validating method parameters in service classes, similar to Hibernate Validator. Use `@Validated` on the class and `@Valid`/`@Rule` on parameters.

### Enable Service Validation

Add `@Validated` to your service class:

```java
import io.github.emmajiugo.javalidator.annotations.Validated;
import io.github.emmajiugo.javalidator.annotations.Valid;
import io.github.emmajiugo.javalidator.annotations.Rule;
import org.springframework.stereotype.Service;

@Validated
@Service
public class UserService {

    // Validate DTO fields with @Valid (cascaded validation)
    public User createUser(@Valid CreateUserDTO request) {
        // If validation fails, ValidationException is thrown
        return userRepository.save(toEntity(request));
    }

    // Validate parameter directly with @Rule
    public User findById(@Rule("gte:1") Long id) {
        // id must be >= 1
        return userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException(id));
    }

    // Combine both
    public User updateUser(@Rule("gte:1") Long id, @Valid UpdateUserDTO request) {
        // Both id and request are validated
        User user = findById(id);
        // update logic...
        return user;
    }
}
```

### Annotations Reference

| Annotation | Target | Purpose |
|------------|--------|---------|
| `@Validated` | Class (TYPE) | Enables method parameter validation for the class |
| `@Valid` | Parameter | Triggers cascaded validation of object's fields |
| `@Rule` | Parameter/Field | Validates the value directly with specified rules |

### Method Parameter Validation with @Rule

Use `@Rule` on method parameters to validate primitive values directly:

```java
@Validated
@Service
public class ProductService {

    public List<Product> search(
            @Rule("min:2|max:100") String query,       // String length validation
            @Rule("gte:1|lte:100") Integer pageSize,   // Numeric range
            @Rule("gte:0") Integer page                // Must be >= 0
    ) {
        return productRepository.search(query, page, pageSize);
    }

    public Product getProduct(@Rule("uuid") String productId) {
        // productId must be a valid UUID format
        return productRepository.findById(productId);
    }
}
```

### Important Notes

- **RestController**: For `@RestController` classes, validation works automatically without `@Validated`
- **Service/Component**: Must add `@Validated` to enable method parameter validation
- **Conditional Rules**: Rules like `required_if`, `same`, `different` require DTO context and cannot be used on single parameters
- **Enum Rule**: The `enum` rule requires `enumClass` parameter and only works on DTO fields, not method parameters

## Custom Validation Rules

Create custom rules by implementing `ValidationRule` and annotating with `@Component`:

```java
import io.github.emmajiugo.javalidator.ValidationRule;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class NoReservedWordsRule implements ValidationRule {

    private static final Set<String> RESERVED = Set.of(
            "admin", "root", "system", "moderator"
    );

    @Override
    public String validate(String fieldName, Object value, String parameter) {
        if (value == null) return null;

        String stringValue = value.toString().toLowerCase();
        for (String reserved : RESERVED) {
            if (stringValue.contains(reserved)) {
                return "The " + fieldName + " cannot contain reserved word: " + reserved;
            }
        }
        return null;
    }

    @Override
    public String getName() {
        return "noreservedwords";
    }
}
```

Use it in your DTO:

```java
public record UserDTO(
        @Rule("required|min:3|noreservedwords")
        String username,
        // ...
) {}
```

The rule is automatically registered at startup:
```
INFO  i.g.e.j.s.JavalidatorAutoConfiguration : Registered 1 custom validation rule(s)
```

## Programmatic Customization

For advanced configuration, create a `JavalidatorCustomizer` bean:

```java
import io.github.emmajiugo.javalidator.spring.customizer.JavalidatorCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ValidationConfig {

    @Bean
    public JavalidatorCustomizer javalidatorCustomizer() {
        return builder -> builder
                .maxClassHierarchyDepth(15)
                .validateFieldNames(true)
                .fieldNamePattern("^[a-zA-Z_][a-zA-Z0-9_]*$")
                .strictMode(false);  // Graceful error handling for production
    }
}
```

## Custom Exception Handler

To customize the error response format, disable the default handler and create your own:

```yaml
javalidator:
  exception-handler:
    enabled: false
```

```java
import io.github.emmajiugo.javalidator.exception.ValidationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CustomValidationHandler {

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<CustomErrorResponse> handleValidation(ValidationException ex) {
        return ResponseEntity.badRequest().body(
            new CustomErrorResponse("VALIDATION_ERROR", ex.getErrors())
        );
    }
}
```

## Testing

Test your validation:

```bash
# Invalid request
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"username": "ad", "email": "invalid", "age": 15}'

# Response: HTTP 400
{
  "status": "error",
  "message": "Validation failed",
  "errors": {
    "username": ["The username must be at least 3 characters."],
    "email": ["The email must be a valid email address."],
    "age": ["The age must be at least 18."]
  }
}

# Valid request
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"username": "johndoe", "email": "john@example.com", "age": 25}'

# Response: HTTP 200
User created: johndoe
```

## Manual Setup (Without Starter)

If you prefer manual configuration or need more control, see the [Manual Spring Boot Setup](#manual-setup-without-starter-1) section below.

<details>
<summary><strong>Manual Setup (Without Starter)</strong></summary>

### Dependencies

```xml
<dependency>
    <groupId>io.github.emmajiugo</groupId>
    <artifactId>javalidator-core</artifactId>
    <version>0.3.2</version>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>
```

### Create Validation Aspect

```java
import io.github.emmajiugo.javalidator.Validator;
import io.github.emmajiugo.javalidator.annotations.Valid;
import io.github.emmajiugo.javalidator.exception.ValidationException;
import io.github.emmajiugo.javalidator.model.ValidationResponse;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Parameter;

@Aspect
@Component
public class ValidationAspect {

    @Before("@within(org.springframework.web.bind.annotation.RestController) && " +
            "(@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PutMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PatchMapping))")
    public void validateParameters(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Parameter[] parameters = signature.getMethod().getParameters();
        Object[] args = joinPoint.getArgs();

        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];
            Object arg = args[i];

            if (arg != null && shouldValidate(param)) {
                ValidationResponse response = Validator.validate(arg);
                if (!response.valid()) {
                    throw new ValidationException(response.errors());
                }
            }
        }
    }

    private boolean shouldValidate(Parameter param) {
        return param.isAnnotationPresent(Valid.class);
    }
}
```

### Create Exception Handler

```java
import io.github.emmajiugo.javalidator.exception.ValidationException;
import io.github.emmajiugo.javalidator.model.ValidationError;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(ValidationException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", "Validation failed");
        response.put("errors", formatErrors(ex.getErrors()));
        return ResponseEntity.badRequest().body(response);
    }

    private Map<String, List<String>> formatErrors(List<ValidationError> errors) {
        Map<String, List<String>> formatted = new HashMap<>();
        for (ValidationError error : errors) {
            formatted.put(error.field(), error.messages());
        }
        return formatted;
    }
}
```

</details>

## Troubleshooting

| Problem | Solution |
|---------|----------|
| Aspect not triggered | Ensure `spring-boot-starter-aop` is in dependencies |
| GET requests not validated | Check `javalidator.aspect.validate-get-requests` property (default: true) |
| Custom rule not registered | Ensure it's annotated with `@Component` or defined as a `@Bean` |
| Entire validation disabled | Check `javalidator.enabled` property |

## Requirements

- Spring Boot 3.5.x or later (including 4.x)
- Java 17 or later

## Next Steps

- [Supported Rules](../supported-rules.md)
- [Custom Rules](../custom-rules.md)
- [Plain Java Integration](plain-java.md)
- [Quarkus Integration](quarkus.md)
- [Jakarta EE Integration](jakarta-ee.md)