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
import io.github.emmajiugo.javalidator.annotations.Validate;

@Validate
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

@RestController
@RequestMapping("/api/users")
public class UserController {

    @PostMapping
    public ResponseEntity<String> createUser(@RequestBody UserDTO dto) {
        // Validation happens automatically!
        // If validation fails, a 400 response is returned
        return ResponseEntity.ok("User created: " + dto.username());
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateUser(
            @PathVariable Long id,
            @RequestBody UserDTO dto) {
        return ResponseEntity.ok("User updated: " + dto.username());
    }
}
```

**Note**: The `@Validate` annotation on the DTO class is sufficient. The aspect detects it automatically.

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

  aspect:
    enabled: true                        # Enable/disable AOP aspect (default: true)
    validate-get-requests: false         # Validate GET parameters (default: false)
    validate-delete-requests: false      # Validate DELETE parameters (default: false)

  exception-handler:
    enabled: true                        # Enable/disable exception handler (default: true)
    include-path: true                   # Include request path in response (default: true)
    include-timestamp: true              # Include timestamp in response (default: true)
    message: "Validation failed"         # Custom error message (default: "Validation failed")
```

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
@Validate
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
                .fieldNamePattern("^[a-zA-Z_][a-zA-Z0-9_]*$");
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
import io.github.emmajiugo.javalidator.annotations.Validate;
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
        return param.isAnnotationPresent(Validate.class) ||
               param.getType().isAnnotationPresent(Validate.class);
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
| GET requests not validated | By design, only POST/PUT/PATCH are validated. Enable via `javalidator.aspect.validate-get-requests=true` |
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