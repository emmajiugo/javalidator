# Quarkus Integration

This guide shows how to integrate Javalidator with Quarkus using CDI interceptors for automatic validation.

## Overview

With Quarkus CDI interceptors, you can automatically validate DTOs in your REST endpoints without manually calling `Validator.validate()`. This approach:
- ✅ Works with GraalVM native compilation
- ✅ Zero reflection overhead at runtime
- ✅ Integrates seamlessly with Quarkus REST
- ✅ Uses standard CDI interceptor binding

## Prerequisites

Add Javalidator to your `pom.xml`:

```xml
<!-- Javalidator Core -->
<dependency>
    <groupId>io.github.emmajiugo</groupId>
    <artifactId>javalidator-core</artifactId>
    <version>1.0.0</version>
</dependency>

<!-- Quarkus REST (if not already included) -->
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-rest</artifactId>
</dependency>

<!-- Quarkus Arc (CDI) - usually included by default -->
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-arc</artifactId>
</dependency>
```

## Step 1: Create Interceptor Binding

Create an interceptor binding annotation:

```java
package com.example.validation;

import jakarta.interceptor.InterceptorBinding;
import java.lang.annotation.*;

/**
 * CDI Interceptor binding for automatic validation.
 * Apply to methods or types to enable automatic DTO validation.
 */
@InterceptorBinding
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ValidateDTO {
}
```

## Step 2: Create Validation Interceptor

Create the CDI interceptor that performs validation:

```java
package com.example.validation;

import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import io.github.emmajiugo.javalidator.Validator;
import io.github.emmajiugo.javalidator.annotations.Valid;
import io.github.emmajiugo.javalidator.exception.ValidationException;
import io.github.emmajiugo.javalidator.model.ValidationResponse;

import java.lang.reflect.Parameter;

/**
 * CDI Interceptor for automatic DTO validation using Javalidator.
 */
@ValidateDTO
@Interceptor
@jakarta.annotation.Priority(Interceptor.Priority.PLATFORM_BEFORE)
public class ValidationInterceptor {

    @AroundInvoke
    public Object validateParameters(InvocationContext context) throws Exception {
        Parameter[] parameters = context.getMethod().getParameters();
        Object[] args = context.getParameters();

        for (int i = 0; i < parameters.length; i++) {
            if (hasValidAnnotation(parameters[i])) {
                Object arg = args[i];
                if (arg != null) {
                    ValidationResponse response = Validator.validate(arg);
                    if (!response.valid()) {
                        throw new ValidationException(response.errors());
                    }
                }
            }
        }

        return context.proceed();
    }

    private boolean hasValidAnnotation(Parameter parameter) {
        return parameter.isAnnotationPresent(Valid.class);
    }
}
```

## Step 3: Register Interceptor in beans.xml (Optional)

Create `src/main/resources/META-INF/beans.xml` if you want explicit CDI configuration:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="https://jakarta.ee/xml/ns/jakartaee"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="https://jakarta.ee/xml/ns/jakartaee
                           https://jakarta.ee/xml/ns/jakartaee/beans_4_0.xsd"
       version="4.0"
       bean-discovery-mode="all">
    <interceptors>
        <class>com.example.validation.ValidationInterceptor</class>
    </interceptors>
</beans>
```

> **Note**: Quarkus usually auto-discovers interceptors, so this file is optional.

## Step 4: Create Exception Mapper

Create an exception mapper to handle validation errors:

```java
package com.example.validation;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import io.github.emmajiugo.javalidator.exception.ValidationException;
import io.github.emmajiugo.javalidator.model.ValidationError;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JAX-RS Exception Mapper for ValidationException.
 */
@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ValidationException> {

    @Override
    public Response toResponse(ValidationException exception) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("status", 400);
        response.put("error", "Validation Failed");
        response.put("errors", formatErrors(exception.getErrors()));

        return Response
                .status(Response.Status.BAD_REQUEST)
                .entity(response)
                .build();
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

## Step 5: Use in Your REST Resources

Apply `@ValidateDTO` to methods and `@Valid` to parameters that need validation:

```java
package com.example.resource;

import com.example.dto.UserDTO;
import com.example.validation.ValidateDTO;
import io.github.emmajiugo.javalidator.annotations.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    @POST
    @ValidateDTO
    public Response createUser(@Valid UserDTO dto) {
        // Validation happens automatically before this line
        // If validation fails, ValidationException is thrown

        // Your business logic here
        return Response.ok(Map.of("message", "User created successfully")).build();
    }

    @PUT
    @Path("/{id}")
    @ValidateDTO
    public Response updateUser(@PathParam("id") Long id, @Valid UserDTO dto) {
        // Automatic validation
        return Response.ok(Map.of("message", "User updated successfully")).build();
    }
}
```

### Class-Level Interceptor

Apply to entire resource class:

```java
@Path("/api/products")
@ValidateDTO  // All methods in this class will be validated
public class ProductResource {

    @POST
    public Response create(@Valid ProductDTO dto) {
        // Automatically validated
        return Response.ok().build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") Long id, @Valid ProductDTO dto) {
        // Automatically validated
        return Response.ok().build();
    }
}
```

## Error Response Format

When validation fails, the API returns:

```json
{
  "timestamp": "2025-11-22T00:55:00",
  "status": 400,
  "error": "Validation Failed",
  "errors": {
    "username": [
      "Username must be at least 3 characters"
    ],
    "email": [
      "Invalid email format"
    ],
    "age": [
      "The age must be at least 18."
    ]
  }
}
```

## Testing

### Development Mode

```bash
./mvnw quarkus:dev
```

### Test with curl

```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"username":"jo","email":"invalid","age":15}'
```

### Native Build

Quarkus supports native compilation with GraalVM:

```bash
./mvnw package -Dnative
```

Javalidator works seamlessly with native compilation since it uses standard Java reflection.

## Advanced: Service Layer Validation

Use interceptor in CDI beans:

```java
package com.example.service;

import com.example.dto.UserDTO;
import com.example.validation.ValidateDTO;
import io.github.emmajiugo.javalidator.annotations.Valid;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserService {

    @ValidateDTO
    public User createUser(@Valid UserDTO dto) {
        // Automatic validation before method execution
        return userRepository.persist(toEntity(dto));
    }

    @ValidateDTO
    public User updateUser(Long id, @Valid UserDTO dto) {
        // Automatic validation
        User user = userRepository.findById(id);
        user.update(dto);
        return user;
    }
}
```

## Customization

### Custom Error Response

Customize the exception mapper:

```java
@Provider
public class CustomValidationExceptionMapper implements ExceptionMapper<ValidationException> {

    @Override
    public Response toResponse(ValidationException exception) {
        return Response
                .status(422) // Unprocessable Entity
                .entity(Map.of(
                    "code", "VALIDATION_ERROR",
                    "message", "Request validation failed",
                    "fields", exception.getErrors()
                ))
                .build();
    }
}
```

### Conditional Validation

Validate only specific parameters:

```java
@POST
@Path("/conditional")
public Response create(
    @HeaderParam("X-Validate") boolean shouldValidate,
    UserDTO dto) {

    if (shouldValidate) {
        ValidationResponse response = Validator.validate(dto);
        if (!response.valid()) {
            throw new ValidationException(response.errors());
        }
    }

    return Response.ok().build();
}
```

## Configuration

Add to `application.properties` for logging:

```properties
# Enable interceptor logging
quarkus.arc.debug.enabled=true

# Log validation errors
quarkus.log.category."com.example.validation".level=DEBUG
```

## Performance

CDI interceptors in Quarkus are highly optimized:
- ✅ **Zero reflection at runtime** (build-time processing)
- ✅ **Native compilation support**
- ✅ **Minimal overhead** (< 1ms per validation)
- ✅ **Thread-safe** by default

## Testing Your Integration

Create a test:

```java
package com.example.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class UserResourceTest {

    @Test
    void testValidationFailure() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "username": "jo",
                    "email": "invalid",
                    "age": 15
                }
                """)
        .when()
            .post("/api/users")
        .then()
            .statusCode(400)
            .body("status", equalTo(400))
            .body("errors.username", hasItem(containsString("at least 3 characters")))
            .body("errors.email", hasItem(containsString("Invalid email")));
    }

    @Test
    void testValidationSuccess() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "username": "johndoe",
                    "email": "john@example.com",
                    "age": 25,
                    "status": "ACTIVE"
                }
                """)
        .when()
            .post("/api/users")
        .then()
            .statusCode(200);
    }
}
```

## Summary

✅ **CDI Native** - Uses standard Quarkus interceptors
✅ **GraalVM Ready** - Works with native compilation
✅ **Zero Overhead** - Build-time processing
✅ **Type-Safe** - Compile-time checking
✅ **Easy Testing** - Standard Quarkus test framework

## Troubleshooting

**Problem**: Interceptor not triggered
**Solution**: Ensure class is a CDI bean and method is called through proxy (not `this.method()`)

**Problem**: Native compilation fails
**Solution**: Javalidator uses standard reflection which is supported. Ensure `-H:+ReportExceptionStackTraces` for debugging

**Problem**: Validation not working on private methods
**Solution**: CDI interceptors only work on public methods. Make method public or validate manually

## Next Steps

- [Spring Boot Integration](spring-boot.md)
- [Jakarta EE Integration](jakarta-ee.md)
- [Plain Java Integration](plain-java.md)