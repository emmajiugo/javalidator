# Spring Boot Integration

This guide shows how to integrate Javalidator with Spring Boot using AOP for automatic validation.

## Overview

With Spring Boot AOP, you can automatically validate DTOs in your controllers without manually calling `Validator.validate()`. This approach uses:
- **AspectJ** for method interception
- **@Validate annotation** from javalidator-core to mark parameters for validation
- **Optimized pointcut** that only intercepts REST controller endpoints
- **Exception handler** for consistent error responses

## Prerequisites

Add these dependencies to your `pom.xml`:

```xml
<!-- Javalidator Core -->
<dependency>
    <groupId>me.emmajiugo</groupId>
    <artifactId>javalidator-core</artifactId>
    <version>1.0.0</version>
</dependency>

<!-- Spring Boot AOP -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>

<!-- Spring Boot Web (if not already included) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

## Step 1: Create Validation Aspect

Create an AOP aspect to intercept REST controller methods and validate parameters marked with `@Validate`:

```java
package com.example.validation;

import me.emmajiugo.javalidator.Validator;
import me.emmajiugo.javalidator.annotations.Validate;
import me.emmajiugo.javalidator.exception.ValidationException;
import me.emmajiugo.javalidator.model.ValidationResponse;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;

/**
 * AOP aspect that intercepts REST controller endpoint methods with @Validate annotated parameters
 * and automatically validates them using Javalidator.
 *
 * <p>This aspect only intercepts methods in @RestController classes that are annotated with
 * @PostMapping, @PutMapping, or @PatchMapping for optimal performance.
 */
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
            if (hasValidateAnnotation(parameters[i])) {
                Object arg = args[i];
                if (arg != null) {
                    ValidationResponse response = Validator.validate(arg);
                    if (!response.valid()) {
                        throw new ValidationException(response.errors());
                    }
                }
            }
        }
    }

    private boolean hasValidateAnnotation(Parameter parameter) {
        for (Annotation annotation : parameter.getAnnotations()) {
            if (annotation instanceof Validate) {
                return true;
            }
        }
        return false;
    }
}
```

**Key Features:**
- ✅ **Optimized Performance**: Only intercepts `@RestController` methods with `@PostMapping`, `@PutMapping`, or `@PatchMapping`
- ✅ **No Custom Annotations**: Uses the built-in `@Validate` annotation from javalidator-core
- ✅ **Selective Validation**: Validates only parameters marked with `@Validate`, allowing fine-grained control

## Step 3: Create Exception Handler

Create a global exception handler to return consistent error responses:

```java
package com.example.validation;

import me.emmajiugo.javalidator.exception.ValidationException;
import me.emmajiugo.javalidator.model.ValidationError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Global exception handler for validation errors.
 */
@RestControllerAdvice
public class ValidationExceptionHandler {

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(ValidationException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Validation Failed");
        response.put("errors", formatErrors(ex.getErrors()));

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    private Map<String, List<String>> formatErrors(List<ValidationError> errors) {
        Map<String, List<String>> formattedErrors = new HashMap<>();
        for (ValidationError error : errors) {
            formattedErrors.put(error.field(), error.messages());
        }
        return formattedErrors;
    }
}
```

## Step 2: Use in Your Controllers

Now you can use `@Validate` from javalidator-core to automatically validate request bodies:

```java
package com.example.controller;

import com.example.dto.UserDTO;
import me.emmajiugo.javalidator.annotations.Validate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @PostMapping
    public ResponseEntity<String> createUser(@Validate @RequestBody UserDTO dto) {
        // Validation happens automatically before this line
        // If validation fails, ValidationException is thrown and handled by exception handler

        // Your business logic here
        return ResponseEntity.ok("User created successfully");
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateUser(
            @PathVariable Long id,
            @Validate @RequestBody UserDTO dto) {
        // Automatic validation
        return ResponseEntity.ok("User updated successfully");
    }
}
```

**Note**: The aspect only validates methods with `@PostMapping`, `@PutMapping`, or `@PatchMapping`. GET requests are not intercepted for performance.

## Example DTO

Define your DTO with Javalidator annotations:

```java
package com.example.dto;

import me.emmajiugo.javalidator.annotations.Rule;

public record UserDTO(
    @Rule(value = "required", message = "Username is required")
    @Rule(value = "min:3", message = "Username must be at least 3 characters")
    @Rule(value = "max:20", message = "Username must not exceed 20 characters")
    String username,

    @Rule(value = "required", message = "Email is required")
    @Rule(value = "email", message = "Invalid email format")
    String email,

    @Rule("required|gte:18|lte:100")
    Integer age
) {}
```

## Error Response Format

When validation fails, the API returns:

```json
{
  "timestamp": "2025-11-22T00:50:00",
  "status": 400,
  "error": "Validation Failed",
  "errors": {
    "username": [
      "Username must be at least 3 characters"
    ],
    "email": [
      "Invalid email format"
    ]
  }
}
```

## Configuration (Optional)

Enable AspectJ auto-proxying in your main application class if not already enabled:

```java
package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

## Testing

Test your validation with a simple curl command:

```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"username":"jo","email":"invalid","age":15}'
```

Expected response:
```json
{
  "timestamp": "2025-11-22T00:50:00",
  "status": 400,
  "error": "Validation Failed",
  "errors": {
    "username": ["Username must be at least 3 characters"],
    "email": ["Invalid email format"],
    "age": ["The age must be at least 18."]
  }
}
```

## Customization

### Custom Error Response Format

Modify the `ValidationExceptionHandler` to return your preferred format:

```java
@ExceptionHandler(ValidationException.class)
public ResponseEntity<CustomErrorResponse> handleValidationException(ValidationException ex) {
    CustomErrorResponse response = new CustomErrorResponse(
        "VALIDATION_ERROR",
        "Request validation failed",
        ex.getErrors()
    );
    return ResponseEntity.badRequest().body(response);
}
```

### Per-Controller Exception Handling

Override global handler in specific controllers:

```java
@RestController
public class SpecialController {

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(ValidationException ex) {
        // Custom handling for this controller only
        return ResponseEntity.badRequest().body(Map.of("errors", ex.getErrors()));
    }
}
```

## Troubleshooting

**Problem**: Aspect not triggered
**Solution**: Ensure `spring-boot-starter-aop` is in dependencies. The aspect only intercepts `@RestController` methods with `@PostMapping`, `@PutMapping`, or `@PatchMapping`

**Problem**: Validation not working on GET requests
**Solution**: By design, GET requests are not intercepted for performance. Only POST/PUT/PATCH methods trigger validation

**Problem**: Custom messages not showing
**Solution**: Check that `message` parameter is set in `@Rule` annotation

## Next Steps

- [Quarkus Integration](quarkus.md)
- [Jakarta EE Integration](jakarta-ee.md)
- [Plain Java Integration](plain-java.md)