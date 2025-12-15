# Security

Javalidator is designed with security in mind, providing several protections against common attacks.

## Built-in Security Features

### 1. Type-Safe Enum Validation

- Enum validation requires a compile-time `Class<?>` parameter
- Prevents arbitrary class loading attacks
- No runtime class name parsing

```java
public enum Status { ACTIVE, INACTIVE, PENDING }

public record UserDTO(
    @Rule(value = "enum", enumClass = Status.class)
    String status
) {}
```

### 2. Pattern Caching (No ReDoS Risk)

- Regex patterns are defined at compile-time by developers, not by users
- Patterns are cached for performance
- ReDoS (Regular Expression Denial of Service) is not a security concern

### 3. Field Name Validation

- Conditional rules validate field names against configured patterns
- Prevents field name injection attacks
- Default pattern: `^[a-zA-Z_][a-zA-Z0-9_]*$`

### 4. Reflection Depth Limiting

- Configurable maximum class hierarchy depth
- Prevents memory exhaustion via deeply nested class hierarchies
- Default limit: 10 levels

## Security Configuration

Configure security settings using `ValidationConfig`:

```java
import io.github.emmajiugo.javalidator.config.ValidationConfig;
import io.github.emmajiugo.javalidator.Validator;

// Use strict security preset
ValidationConfig config = ValidationConfig.strict();
Validator.setConfig(config);

// Or customize security settings
ValidationConfig custom = ValidationConfig.builder()
    .maxClassHierarchyDepth(10)           // Limit inheritance traversal
    .validateFieldNames(true)             // Validate field names in conditional rules
    .fieldNamePattern("^[a-zA-Z_][a-zA-Z0-9_]*$")  // Pattern for valid field names
    .build();
Validator.setConfig(custom);
```

## Configuration Presets

| Preset | Description |
|--------|-------------|
| `ValidationConfig.defaults()` | Balanced security and performance for production |
| `ValidationConfig.strict()` | Maximum security with all protections enabled |
| `ValidationConfig.permissive()` | Minimal checks for development/testing |

## Security Best Practices

1. Use `ValidationConfig.strict()` in production environments
2. Always use the type-safe `enumClass` parameter for enum validation
3. Validate input at system boundaries (controllers, APIs)
4. Keep field names simple and follow Java naming conventions
5. Review custom validation rules for security implications

## Threat Model

**Protected Against:**
- Arbitrary class loading via enum validation
- Field name injection in conditional rules
- Memory exhaustion via unbounded reflection

**Not Applicable:**
- ReDoS (developers control regex patterns at compile-time)
- SQL injection (validation library doesn't interact with databases)
- XSS (validation library doesn't generate HTML)

---

[← Back to README](../README.md) | [Supported Rules →](supported-rules.md)