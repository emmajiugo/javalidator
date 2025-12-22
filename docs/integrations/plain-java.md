# Plain Java Integration

This guide shows how to use Javalidator in any Java application without a framework - plain servlets, standalone apps, or anywhere you have Java code.

## Overview

Javalidator's core library is completely framework-agnostic. You can use it:
- ✅ In plain servlets
- ✅ In standalone console applications
- ✅ In batch processing jobs
- ✅ In testing utilities
- ✅ Anywhere Java runs

## Prerequisites

Add Javalidator to your project:

### Maven

```xml
<dependency>
    <groupId>io.github.emmajiugo</groupId>
    <artifactId>javalidator-core</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle

```groovy
implementation 'io.github.emmajiugo:javalidator-core:1.0.0'
```

## Basic Usage

```java
import io.github.emmajiugo.javalidator.Validator;
import io.github.emmajiugo.javalidator.model.ValidationResponse;

public class Main {
    public static void main(String[] args) {
        // Create DTO
        UserDTO user = new UserDTO("jo", "invalid-email", 15);

        // Validate
        ValidationResponse response = Validator.validate(user);

        // Check result
        if (response.valid()) {
            System.out.println("✓ Validation passed!");
        } else {
            System.out.println("✗ Validation failed:");
            response.errors().forEach(error -> {
                System.out.println("  Field: " + error.field());
                error.messages().forEach(msg ->
                    System.out.println("    - " + msg)
                );
            });
        }
    }
}
```

Output:
```
✗ Validation failed:
  Field: username
    - Username must be at least 3 characters
  Field: email
    - Invalid email format
  Field: age
    - The age must be at least 18.
```

## Servlet Integration

### Simple Servlet Filter

Create a filter that validates all incoming requests:

```java
package com.example.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import io.github.emmajiugo.javalidator.Validator;
import io.github.emmajiugo.javalidator.model.ValidationResponse;

import java.io.IOException;

@WebFilter("/api/*")
public class ValidationFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Only validate POST/PUT requests
        if (isWriteMethod(httpRequest.getMethod())) {
            Object dto = extractDTO(httpRequest);

            if (dto != null) {
                ValidationResponse validation = Validator.validate(dto);

                if (!validation.valid()) {
                    sendValidationError(httpResponse, validation);
                    return;
                }
            }
        }

        chain.doFilter(request, response);
    }

    private boolean isWriteMethod(String method) {
        return "POST".equals(method) || "PUT".equals(method) || "PATCH".equals(method);
    }

    private Object extractDTO(HttpServletRequest request) throws IOException {
        // Read JSON from request body and parse to DTO
        // Use your preferred JSON library (Jackson, Gson, etc.)
        return null; // Implementation depends on your JSON library
    }

    private void sendValidationError(HttpServletResponse response,
                                     ValidationResponse validation) throws IOException {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("application/json");

        String json = formatErrorsAsJson(validation);
        response.getWriter().write(json);
    }

    private String formatErrorsAsJson(ValidationResponse validation) {
        // Format errors as JSON
        // Use your preferred JSON library
        return "{\"errors\": " + validation.errors() + "}";
    }
}
```

### Servlet Example

```java
package com.example.servlet;

import com.example.dto.UserDTO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import io.github.emmajiugo.javalidator.Validator;
import io.github.emmajiugo.javalidator.model.ValidationResponse;

import java.io.IOException;

@WebServlet("/api/users")
public class UserServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        // Parse JSON to DTO (using your JSON library)
        UserDTO dto = parseRequestBody(request, UserDTO.class);

        // Validate
        ValidationResponse validation = Validator.validate(dto);

        if (!validation.valid()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("application/json");
            response.getWriter().write(formatErrors(validation));
            return;
        }

        // Process valid request
        createUser(dto);

        response.setStatus(HttpServletResponse.SC_CREATED);
        response.getWriter().write("{\"message\": \"User created successfully\"}");
    }

    private UserDTO parseRequestBody(HttpServletRequest request, Class<UserDTO> clazz) {
        // Use Jackson, Gson, or other JSON library
        return null;
    }

    private String formatErrors(ValidationResponse validation) {
        // Format errors as JSON
        return "{\"errors\": []}";
    }

    private void createUser(UserDTO dto) {
        // Your business logic
    }
}
```

## Console Application

### Interactive CLI

```java
package com.example.cli;

import com.example.dto.UserDTO;
import io.github.emmajiugo.javalidator.Validator;
import io.github.emmajiugo.javalidator.model.ValidationResponse;

import java.util.Scanner;

public class UserRegistrationCLI {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== User Registration ===\n");

        // Collect input
        System.out.print("Username: ");
        String username = scanner.nextLine();

        System.out.print("Email: ");
        String email = scanner.nextLine();

        System.out.print("Age: ");
        int age = scanner.nextInt();

        // Create DTO
        UserDTO user = new UserDTO(username, email, age);

        // Validate
        ValidationResponse response = Validator.validate(user);

        if (response.valid()) {
            System.out.println("\n✓ Registration successful!");
            saveUser(user);
        } else {
            System.out.println("\n✗ Registration failed:");
            response.errors().forEach(error -> {
                System.out.println("\n" + error.field() + ":");
                error.messages().forEach(msg ->
                    System.out.println("  • " + msg)
                );
            });
        }

        scanner.close();
    }

    private static void saveUser(UserDTO user) {
        // Save to database
        System.out.println("Saving user: " + user.username());
    }
}
```

### Batch Processing

```java
package com.example.batch;

import com.example.dto.UserDTO;
import io.github.emmajiugo.javalidator.Validator;
import io.github.emmajiugo.javalidator.model.ValidationResponse;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class UserImportBatch {

    public static void main(String[] args) throws IOException {
        List<UserDTO> users = readUsersFromCSV("users.csv");

        List<UserDTO> validUsers = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (int i = 0; i < users.size(); i++) {
            UserDTO user = users.get(i);
            ValidationResponse response = Validator.validate(user);

            if (response.valid()) {
                validUsers.add(user);
            } else {
                errors.add("Line " + (i + 1) + ": " + formatErrors(response));
            }
        }

        System.out.println("Valid users: " + validUsers.size());
        System.out.println("Invalid users: " + errors.size());

        if (!errors.isEmpty()) {
            System.out.println("\nErrors:");
            errors.forEach(System.out::println);
        }

        // Import valid users
        importUsers(validUsers);
    }

    private static List<UserDTO> readUsersFromCSV(String filename) {
        // Read CSV file
        return List.of();
    }

    private static String formatErrors(ValidationResponse response) {
        return response.errors().stream()
                .map(e -> e.field() + ": " + String.join(", ", e.messages()))
                .reduce((a, b) -> a + "; " + b)
                .orElse("");
    }

    private static void importUsers(List<UserDTO> users) {
        // Bulk import to database
    }
}
```

## Service Layer Pattern

For plain Java applications without frameworks:

```java
package com.example.service;

import com.example.dto.UserDTO;
import io.github.emmajiugo.javalidator.Validator;
import io.github.emmajiugo.javalidator.exception.NotValidException;
import io.github.emmajiugo.javalidator.model.ValidationResponse;

public class UserService {

    public User createUser(UserDTO dto) {
        // Validate
        ValidationResponse response = Validator.validate(dto);

        if (!response.valid()) {
            throw new NotValidException(response.errors());
        }

        // Create user
        User user = new User();
        user.setUsername(dto.username());
        user.setEmail(dto.email());
        user.setAge(dto.age());

        // Save to database
        return saveToDatabase(user);
    }

    public User updateUser(Long id, UserDTO dto) {
        // Validate
        ValidationResponse response = Validator.validate(dto);

        if (!response.valid()) {
            throw new NotValidException(response.errors());
        }

        // Update user
        User user = findById(id);
        user.setUsername(dto.username());
        user.setEmail(dto.email());

        return saveToDatabase(user);
    }

    private User findById(Long id) {
        // Database lookup
        return null;
    }

    private User saveToDatabase(User user) {
        // Database save
        return user;
    }
}
```

## Utility Class Pattern

Create a validation utility:

```java
package com.example.util;

import io.github.emmajiugo.javalidator.Validator;
import io.github.emmajiugo.javalidator.exception.NotValidException;
import io.github.emmajiugo.javalidator.model.ValidationResponse;

public class ValidationUtils {

    /**
     * Validate and throw exception if invalid.
     */
    public static void validateOrThrow(Object dto) {
        ValidationResponse response = Validator.validate(dto);

        if (!response.valid()) {
            throw new NotValidException(response.errors());
        }
    }

    /**
     * Validate and return result for custom handling.
     */
    public static ValidationResponse validate(Object dto) {
        return Validator.validate(dto);
    }

    /**
     * Check if object is valid.
     */
    public static boolean isValid(Object dto) {
        return Validator.validate(dto).valid();
    }
}
```

Usage:

```java
import static com.example.util.ValidationUtils.*;

public class MyService {

    public void processUser(UserDTO dto) {
        // Simple validation with exception
        validateOrThrow(dto);

        // Process user...
    }

    public boolean canProcessUser(UserDTO dto) {
        // Check without throwing
        return isValid(dto);
    }
}
```

## Testing Utility

Use in tests to validate test data:

```java
package com.example.test;

import com.example.dto.UserDTO;
import io.github.emmajiugo.javalidator.Validator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserDTOTest {

    @Test
    void testValidUser() {
        UserDTO user = new UserDTO("johndoe", "john@example.com", 25);

        assertTrue(Validator.validate(user).valid());
    }

    @Test
    void testInvalidUsername() {
        UserDTO user = new UserDTO("jo", "john@example.com", 25);

        var response = Validator.validate(user);

        assertFalse(response.valid());
        assertEquals(1, response.errors().size());
        assertEquals("username", response.errors().get(0).field());
    }

    @Test
    void testMultipleErrors() {
        UserDTO user = new UserDTO("", "invalid", 15);

        var response = Validator.validate(user);

        assertFalse(response.valid());
        assertEquals(3, response.errors().size());
    }
}
```

## Custom Validation Rules

Extend Javalidator with your own rules:

```java
package com.example.validation;

import io.github.emmajiugo.javalidator.ValidationRule;
import io.github.emmajiugo.javalidator.RuleRegistry;

public class PhoneRule implements ValidationRule {

    @Override
    public String validate(String fieldName, Object value, String parameter) {
        if (value instanceof String phone) {
            if (!phone.matches("^\\+?[1-9]\\d{1,14}$")) {
                return "The " + fieldName + " must be a valid phone number.";
            }
        }
        return null;
    }

    @Override
    public String getName() {
        return "phone";
    }
}
```

Register your custom rule:

```java
public class Main {
    public static void main(String[] args) {
        // Register custom rule
        RuleRegistry.register(new PhoneRule());

        // Now you can use it
        // @Rule("required|phone")
    }
}
```

## Summary

✅ **Zero Dependencies** - No framework required
✅ **Flexible** - Use anywhere Java runs
✅ **Simple API** - Just call `Validator.validate()`
✅ **Extensible** - Add custom rules easily
✅ **Testable** - Perfect for unit tests

## Best Practices

1. **Validate at boundaries** - API endpoints, file imports, user input
2. **Fail fast** - Validate before processing
3. **Use records** - Cleaner, immutable DTOs
4. **Custom rules** - Don't duplicate validation logic
5. **Test validation** - Include validation in unit tests

## Next Steps

- [Spring Boot Integration](spring-boot.md)
- [Quarkus Integration](quarkus.md)
- [Jakarta EE Integration](jakarta-ee.md)