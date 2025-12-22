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

### 5. Configuration Error Handling (Strict Mode)

- Controls how configuration errors are handled at runtime
- **Strict mode OFF (default)**: Configuration errors become validation errors, preventing service crashes
- **Strict mode ON**: Configuration errors throw exceptions immediately for fail-fast behavior
- Helps catch misconfigured rules during development/testing

**When to use:**
- **Production**: Keep strict mode OFF (default) to prevent crashes from configuration errors
- **Development/Testing**: Enable strict mode to catch misconfigured rules early

**Example Configuration Errors:**
- Missing rule parameters: `@Rule("min")` instead of `@Rule("min:3")`
- Invalid rule names: `@Rule("unknown_rule")`
- Invalid parameters: `@Rule("digits:abc")` instead of `@Rule("digits:4")`

## Security Configuration

Configure security settings using `ValidationConfig`:

```java
import io.github.emmajiugo.javalidator.config.ValidationConfig;
import io.github.emmajiugo.javalidator.Validator;

// Use strict preset for development/testing (enables strict mode)
ValidationConfig config = ValidationConfig.strict();
Validator.setConfig(config);

// Or use defaults for production (strict mode OFF)
ValidationConfig prodConfig = ValidationConfig.defaults();
Validator.setConfig(prodConfig);

// Or customize all settings
ValidationConfig custom = ValidationConfig.builder()
    .maxClassHierarchyDepth(10)           // Limit inheritance traversal
    .validateFieldNames(true)             // Validate field names in conditional rules
    .fieldNamePattern("^[a-zA-Z_][a-zA-Z0-9_]*$")  // Pattern for valid field names
    .strictMode(false)                     // Handle config errors gracefully (production-safe)
    .build();
Validator.setConfig(custom);
```

## Configuration Presets

| Preset | Strict Mode | Validate Field Names | Description |
|--------|-------------|---------------------|-------------|
| `ValidationConfig.defaults()` | OFF | ON | **Recommended for production** - Graceful error handling, all security features |
| `ValidationConfig.strict()` | ON | ON | **Recommended for development/testing** - Fail-fast on config errors |
| `ValidationConfig.permissive()` | OFF | OFF | Minimal checks for development/testing |

## Security Best Practices

1. **Use `ValidationConfig.defaults()` in production** for graceful error handling
2. **Use `ValidationConfig.strict()` in development/testing** to catch configuration errors early
3. Always use the type-safe `enumClass` parameter for enum validation
4. Validate input at system boundaries (controllers, APIs)
5. Keep field names simple and follow Java naming conventions
6. Review custom validation rules for security implications

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