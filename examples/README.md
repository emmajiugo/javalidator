# Javalidator Examples

Framework-specific examples showing **DRY integration patterns** - add one class, use `@Validate` everywhere.

## Examples

### 📦 Plain Java (`plain-java/`)
Basic usage without any framework.

```bash
cd plain-java && mvn exec:java
```

---

### 🌱 Spring Boot (`spring-boot/`)
Uses Spring AOP with `@Aspect` for automatic validation on REST endpoints.

**Run:**
```bash
cd spring-boot && mvn spring-boot:run

# Test with invalid data
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"username":"jo","email":"invalid","age":15}'

# Test with valid data
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"username":"john_doe","email":"john@example.com","age":25}'
```

**Integration (add once):**
```java
// ValidationAspect.java - intercepts REST controller methods with @Validate parameters
@Aspect
@Component
public class ValidationAspect {

    // Only intercepts @RestController methods with @PostMapping/@PutMapping/@PatchMapping
    @Before("@within(org.springframework.web.bind.annotation.RestController) && " +
            "(@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PutMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PatchMapping))")
    public void validateParameters(JoinPoint joinPoint) {
        // Validates @Validate parameters automatically
    }
}

// pom.xml - add AOP dependency
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>
```

**Usage (across all controllers):**
```java
@PostMapping
public ResponseEntity<String> createUser(@Validate @RequestBody UserDTO userDTO) {
    // If we reach here, validation passed
    return ResponseEntity.ok("User created: " + userDTO.username());
}
```

**Performance Note:** The aspect only intercepts REST controller endpoints with POST/PUT/PATCH mappings, not all methods in your application.

---

### ☕ Jakarta EE (`jakarta-ee/`)
Uses CDI Interceptor for automatic validation.

**Build:**
```bash
cd jakarta-ee && mvn package
# Deploy jakarta-ee-example.war to your server
```

**Integration (add once):**
```java
// ValidationInterceptor.java
@Interceptor
@ValidateBinding
public class ValidationInterceptor {
    @AroundInvoke
    public Object validateParameters(InvocationContext context) {
        // Validates @Validate parameters automatically
    }
}

// beans.xml - enable interceptor
<interceptors>
    <class>example.ValidationInterceptor</class>
</interceptors>
```

**Usage (across all resources):**
```java
@POST
@ValidateBinding
public Response createUser(@Validate UserDTO userDTO) {
    // If we reach here, validation passed
    return Response.ok().entity("{\"message\": \"User created\"}").build();
}
```

---

### ⚡ Quarkus (`quarkus/`)
Uses CDI Interceptor (same pattern as Jakarta EE).

**Run:**
```bash
cd quarkus && mvn quarkus:dev

# Test
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"username":"john","email":"john@example.com","age":25}'
```

**Integration (add once):**
```java
// ValidationInterceptor.java
@Interceptor
@ValidateBinding
public class ValidationInterceptor {
    @AroundInvoke
    public Object validateParameters(InvocationContext context) {
        // Validates @Validate parameters automatically
    }
}
```

**Usage (across all resources):**
```java
@POST
@ValidateBinding
public String createUser(@Validate UserDTO userDTO) {
    // If we reach here, validation passed
    return "{\"message\": \"User created: " + userDTO.username() + "\"}";
}
```

---

## Key Benefits

✅ **DRY** - Add validation logic **once**, use across 12+ controllers
✅ **Declarative** - Just use `@Validate` annotation
✅ **Framework-native** - Uses each framework's standard extension points
✅ **Zero repetition** - No manual validation code in endpoints

## Integration Pattern

### 1. Define DTO with rules
```java
public record UserDTO(
    @Rule("required|min:3|max:20") String username,
    @Rule("required|email") String email,
    @Rule("required|gte:18") Integer age
) {}
```

### 2. Add framework integration (once)
- **Spring Boot**: Spring AOP `@Aspect`
- **Jakarta EE/Quarkus**: CDI `@Interceptor`

### 3. Use in all endpoints
```java
public Response createUser(@Validate UserDTO dto) {
    // Validation happens automatically
}
```

### 4. Handle errors globally
All frameworks throw `ValidationException` → caught by global handler

## Error Response

All examples return HTTP 400 with:
```json
{
  "status": "error",
  "message": "Validation failed",
  "errors": {
    "username": ["The username must be at least 3 characters."],
    "email": ["The email must be a valid email address."],
    "age": ["The age must be at least 18."]
  }
}
```