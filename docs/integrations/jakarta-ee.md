# Jakarta EE Integration

This guide shows how to integrate Javalidator with Jakarta EE using standard interceptors for automatic validation.

## Overview

With Jakarta EE interceptors, you can automatically validate DTOs across any Jakarta EE-compliant application server (WildFly, Payara, Open Liberty, TomEE, etc.). This approach:
- ✅ Uses Jakarta Interceptors specification
- ✅ Works with JAX-RS, CDI, EJB
- ✅ Portable across all Jakarta EE servers
- ✅ Standard enterprise pattern

## Prerequisites

Add Javalidator to your `pom.xml`:

```xml
<!-- Javalidator Core -->
<dependency>
    <groupId>io.github.emmajiugo</groupId>
    <artifactId>javalidator-core</artifactId>
    <version>1.0.0</version>
</dependency>

<!-- Jakarta EE API (usually provided by server) -->
<dependency>
    <groupId>jakarta.platform</groupId>
    <artifactId>jakarta.jakartaee-api</artifactId>
    <version>10.0.0</version>
    <scope>provided</scope>
</dependency>
```

## Step 1: Create Interceptor Binding

Create an interceptor binding annotation:

```java
package com.example.validation;

import jakarta.interceptor.InterceptorBinding;
import java.lang.annotation.*;

/**
 * Jakarta Interceptor binding for automatic validation.
 * Can be applied to methods or entire classes.
 */
@InterceptorBinding
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ValidateDTO {
}
```

## Step 2: Create Validation Interceptor

Create the interceptor that performs validation:

```java
package com.example.validation;

import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import io.github.emmajiugo.javalidator.Validator;
import io.github.emmajiugo.javalidator.annotations.Valid;
import io.github.emmajiugo.javalidator.exception.NotValidException;
import io.github.emmajiugo.javalidator.model.ValidationResponse;

import java.io.Serializable;
import java.lang.reflect.Parameter;

/**
 * Jakarta EE Interceptor for automatic DTO validation using Javalidator.
 */
@ValidateDTO
@Interceptor
@jakarta.annotation.Priority(Interceptor.Priority.APPLICATION)
public class ValidationInterceptor implements Serializable {

    private static final long serialVersionUID = 1L;

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
                        throw new NotValidException(response.errors());
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

## Step 3: Enable Interceptor in beans.xml

Create or update `src/main/webapp/WEB-INF/beans.xml`:

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

## Step 4: Create Exception Mapper

Create a JAX-RS exception mapper:

```java
package com.example.validation;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import io.github.emmajiugo.javalidator.exception.NotValidException;
import io.github.emmajiugo.javalidator.model.ValidationError;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JAX-RS Exception Mapper for NotValidException.
 */
@Provider
public class ValidationExceptionMapper implements ExceptionMapper<NotValidException> {

    @Override
    public Response toResponse(NotValidException exception) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("status", Response.Status.BAD_REQUEST.getStatusCode());
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

## Step 5: Use in JAX-RS Resources

Apply `@ValidateDTO` to REST endpoints and `@Valid` to parameters:

```java
package com.example.rest;

import com.example.dto.UserDTO;
import com.example.validation.ValidateDTO;
import io.github.emmajiugo.javalidator.annotations.Valid;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;

@Path("/api/users")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    @POST
    @ValidateDTO
    public Response createUser(@Valid UserDTO dto) {
        // Validation happens automatically before this line
        // If validation fails, NotValidExceptionis thrown

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

## Use in EJB Session Beans

Apply to EJB methods:

```java
package com.example.service;

import com.example.dto.UserDTO;
import com.example.validation.ValidateDTO;
import io.github.emmajiugo.javalidator.annotations.Valid;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Stateless
public class UserService {

    @PersistenceContext
    private EntityManager em;

    @ValidateDTO
    public User createUser(@Valid UserDTO dto) {
        // Automatic validation before method execution
        User user = new User();
        user.setUsername(dto.username());
        user.setEmail(dto.email());
        user.setAge(dto.age());

        em.persist(user);
        return user;
    }

    @ValidateDTO
    public User updateUser(Long id, @Valid UserDTO dto) {
        // Automatic validation
        User user = em.find(User.class, id);
        user.setUsername(dto.username());
        user.setEmail(dto.email());
        return em.merge(user);
    }
}
```

## Error Response Format

When validation fails:

```json
{
  "timestamp": "2025-11-22T01:00:00",
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

## Server-Specific Configuration

### WildFly / JBoss EAP

No additional configuration needed. Just deploy your WAR/EAR.

```xml
<!-- jboss-deployment-structure.xml (optional) -->
<jboss-deployment-structure>
    <deployment>
        <dependencies>
            <module name="jakarta.interceptor.api"/>
        </dependencies>
    </deployment>
</jboss-deployment-structure>
```

### Payara Server

Works out of the box. For microprofile config:

```properties
# payara-mp-config.properties
javalidator.enabled=true
```

### Open Liberty

Add features to `server.xml`:

```xml
<server>
    <featureManager>
        <feature>jakartaee-10.0</feature>
        <feature>cdi-4.0</feature>
        <feature>restfulWS-3.1</feature>
    </featureManager>
</server>
```

### Apache TomEE

TomEE Plus includes full Jakarta EE. Use standard configuration:

```xml
<!-- No special configuration needed -->
```

## Testing

### Deploy and Test

```bash
# Build
mvn clean package

# Deploy to WildFly
cp target/myapp.war $WILDFLY_HOME/standalone/deployments/

# Test
curl -X POST http://localhost:8080/myapp/api/users \
  -H "Content-Type: application/json" \
  -d '{"username":"jo","email":"invalid","age":15}'
```

### Arquillian Tests

Use Arquillian for integration testing:

```java
package com.example.test;

import com.example.dto.UserDTO;
import com.example.rest.UserResource;
import jakarta.ws.rs.core.Response;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import jakarta.inject.Inject;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ArquillianExtension.class)
public class UserResourceIT {

    @Inject
    private UserResource userResource;

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addPackages(true, "com.example")
                .addAsWebInfResource("beans.xml");
    }

    @Test
    public void testValidationFailure() {
        UserDTO dto = new UserDTO("jo", "invalid", 15);

        Response response = userResource.createUser(dto);

        assertEquals(400, response.getStatus());
    }
}
```

## Advanced Usage

### Class-Level Interceptor

Apply to entire class:

```java
@Path("/api/products")
@ValidateDTO  // All methods validated
@RequestScoped
public class ProductResource {

    @POST
    public Response create(@Valid ProductDTO dto) {
        return Response.ok().build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") Long id, @Valid ProductDTO dto) {
        return Response.ok().build();
    }
}
```

### Selective Validation

Skip validation for specific methods:

```java
@ValidateDTO
@Stateless
public class UserService {

    @ValidateDTO
    public void createUser(@Valid UserDTO dto) {
        // Validated
    }

    // No @ValidateDTO = no validation
    public void internalMethod(UserDTO dto) {
        // Not validated
    }
}
```

### Custom Validation Logic

Combine with manual validation:

```java
@POST
@ValidateDTO
public Response create(@Valid UserDTO dto) {
    // Automatic validation already passed

    // Additional business validation
    if (userExists(dto.email())) {
        return Response.status(409)
            .entity(Map.of("error", "Email already exists"))
            .build();
    }

    return Response.ok().build();
}
```

## Customization

### Custom Error Format

Modify exception mapper:

```java
@Provider
public class CustomValidationMapper implements ExceptionMapper<NotValidException> {

    @Override
    public Response toResponse(NotValidException exception) {
        // Your custom format
        return Response.status(422)
                .entity(new ErrorResponse("VALIDATION_ERROR", exception.getErrors()))
                .build();
    }
}
```

### Logging Validation Failures

Add logging to interceptor:

```java
@ValidateDTO
@Interceptor
public class ValidationInterceptor implements Serializable {

    @Inject
    private Logger logger;

    @AroundInvoke
    public Object validateParameters(InvocationContext context) throws Exception {
        // ... validation logic ...

        if (!response.valid()) {
            logger.warning("Validation failed for " + context.getMethod().getName() +
                          ": " + response.errors());
            throw new NotValidException(response.errors());
        }

        return context.proceed();
    }
}
```

## Performance Considerations

Jakarta EE interceptors are highly performant:
- ✅ **Proxy-based** - Minimal overhead
- ✅ **Thread-safe** - CDI handles concurrency
- ✅ **Pooled** - EJB instances are pooled
- ✅ **Optimized** - Application servers optimize interceptor chains

## Summary

✅ **Standard Jakarta EE** - Uses official interceptor spec
✅ **Portable** - Works on all Jakarta EE servers
✅ **Enterprise Ready** - Tested on WildFly, Payara, Liberty
✅ **EJB Compatible** - Works with session beans
✅ **JAX-RS Native** - Seamless REST integration

## Troubleshooting

**Problem**: Interceptor not fired
**Solution**: Ensure `beans.xml` exists and interceptor is registered. Check CDI is enabled.

**Problem**: Serialization errors in EJB
**Solution**: Ensure interceptor implements `Serializable`

**Problem**: ClassNotFoundException
**Solution**: Verify Javalidator JAR is in WEB-INF/lib or server lib directory

**Problem**: Validation works locally but not on server
**Solution**: Check server logs. Ensure `beans.xml` is in correct location (WEB-INF/ or META-INF/)

## Next Steps

- [Spring Boot Integration](spring-boot.md)
- [Quarkus Integration](quarkus.md)
- [Plain Java Integration](plain-java.md)

## Resources

- [Jakarta Interceptors Specification](https://jakarta.ee/specifications/interceptors/)
- [Jakarta REST Specification](https://jakarta.ee/specifications/restful-ws/)
- [Jakarta CDI Specification](https://jakarta.ee/specifications/cdi/)