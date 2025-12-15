# Javalidator

A framework-agnostic Java validation library with Laravel-style syntax, inspired by declarative validation patterns.

[![Java](https://img.shields.io/badge/Java-17%2B-orange)](https://www.oracle.com/java/)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.emmajiugo/javalidator-core.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/io.github.emmajiugo/javalidator-core)
[![License](https://img.shields.io/badge/License-Apache%202.0-green.svg)](LICENSE)

## Requirements

- **Java**: 17 or higher

## Features

- ✅ **Framework-Agnostic** - Core library has zero dependencies
- ✅ **Laravel-Style Syntax** - Familiar pipe-separated validation rules
- ✅ **Record & Class Support** - Works with both Java records and traditional POJOs
- ✅ **Extensible** - Easy to add custom validation rules
- ✅ **Type-Safe** - Leverages Java's type system
- ✅ **Custom Messages** - Per-rule custom error messages
- ✅ **Consistent Errors** - Standardized error response format

## Table of Contents

- [Installation](#installation)
- [Quick Start](#quick-start)
- [Usage Examples](#usage-examples)
- [Documentation](#documentation)
- [Error Handling](#error-handling)
- [Advanced Features](#advanced-features)
- [Security](#security)
- [Roadmap](#roadmap)

## Installation

> **Latest Version:** See the Maven Central badge above for the current version.

### Maven

```xml
<dependency>
    <groupId>io.github.emmajiugo</groupId>
    <artifactId>javalidator-core</artifactId>
    <version><!-- See Maven Central badge above --></version>
</dependency>
```

### Gradle

```groovy
implementation 'io.github.emmajiugo:javalidator-core:VERSION'
```

## Quick Start

### Using Java Records

```java
import io.github.emmajiugo.javalidator.annotations.Rule;
import io.github.emmajiugo.javalidator.Validator;
import io.github.emmajiugo.javalidator.model.ValidationResponse;

public record UserDTO(
    @Rule("required|min:3|max:20")
    String username,

    @Rule("required|email")
    String email,

    @Rule("required|gte:18|lte:100")
    Integer age
) {}

// Validate
UserDTO user = new UserDTO("john", "john@example.com", 25);
ValidationResponse response = Validator.validate(user);

if (response.valid()) {
    System.out.println("Validation passed!");
} else {
    response.errors().forEach(error ->
        System.out.println(error.field() + ": " + error.messages())
    );
}
```

### Using Traditional Classes

```java
public class ProductDTO {
    @Rule("required|min:3")
    private String name;

    @Rule("required|numeric|gte:0")
    private Double price;

    // Constructor, getters, setters...
}

// Validate
ProductDTO product = new ProductDTO("Laptop", 1299.99);
ValidationResponse response = Validator.validate(product);
```

## Usage Examples

### Custom Error Messages

Use multiple `@Rule` annotations for custom messages (Java 8+ repeatable annotations):

```java
public record UserDTO(
    @Rule(value = "required", message = "Username is required")
    @Rule(value = "min:3", message = "Username too short")
    @Rule(value = "max:20", message = "Username too long")
    String username,

    @Rule(value = "required", message = "Email is required")
    @Rule(value = "email", message = "Invalid email address")
    String email
) {}
```

### Error Response Format

```json
{
  "valid": false,
  "errors": [
    {
      "field": "username",
      "messages": [
        "Username too short"
      ]
    },
    {
      "field": "email",
      "messages": [
        "Invalid email address"
      ]
    }
  ]
}
```

## Documentation

### 📖 Complete Guides

Comprehensive documentation for all aspects of Javalidator:

**Core Documentation:**
- **[Supported Validation Rules](docs/supported-rules.md)** - Complete reference of all 32 built-in rules with examples
- **[Custom Validation Rules](docs/custom-rules.md)** - Guide to creating your own validation rules

**Framework Integration:**
- **[Spring Boot Integration](docs/integrations/spring-boot.md)** - AOP-based automatic validation
- **[Quarkus Integration](docs/integrations/quarkus.md)** - CDI interceptors with GraalVM support
- **[Jakarta EE Integration](docs/integrations/jakarta-ee.md)** - Standard Jakarta Interceptors for all EE servers
- **[Plain Java Integration](docs/integrations/plain-java.md)** - Use in servlets, console apps, batch processing

### Quick Reference

**32 Built-in Rules Available** - See the [Supported Rules Guide](docs/supported-rules.md) for complete documentation.

**Core Rules:** `required`, `min`, `max`, `email`, `numeric`, `gt`, `lt`, `gte`, `lte`, `regex`, `in`
**Date Rules:** `date`, `before`, `after`, `future`, `past`
**Format Rules:** `url`, `ip`, `uuid`, `json`, `alpha`, `alpha_num`, `enum`, `digits`
**Special Rules:** `between`, `size`, `nullable`
**Conditional Rules:** `required_if`, `required_unless`
**Field Comparison:** `same`, `different`
**Array/Collection:** `distinct`

**Combining Rules:**
```java
@Rule("required|email|max:100")
String email;

@Rule("required|numeric|gte:0|lte:999")
Integer quantity;
```

See the **[Supported Rules Guide](docs/supported-rules.md)** for complete documentation.

## Error Handling

### Option 1: Check ValidationResponse

```java
ValidationResponse response = Validator.validate(dto);

if (!response.valid()) {
    response.errors().forEach(error -> {
        System.out.println("Field: " + error.field());
        error.messages().forEach(msg ->
            System.out.println("  - " + msg)
        );
    });
}
```

### Option 2: Use ValidationException

```java
try {
    ValidationResponse response = Validator.validate(dto);
    if (!response.valid()) {
        throw new ValidationException(response.errors());
    }
    // Process valid data
} catch (ValidationException e) {
    e.getErrors().forEach(error -> {
        // Handle errors
    });
}
```

## Advanced Features

### Nested Object Validation with @RuleCascade

Validate nested objects and collections using `@RuleCascade`:

```java
import io.github.emmajiugo.javalidator.annotations.RuleCascade;

public record Address(
    @Rule("required")
    String street,

    @Rule("required")
    String city,

    @Rule("required|digits:5")
    String zipCode
) {}

public record User(
    @Rule("required|min:3")
    String name,

    @Rule("required|email")
    String email,

    @RuleCascade  // Validates nested Address object
    Address address,

    @RuleCascade  // Validates each PhoneNumber in the list
    List<PhoneNumber> phoneNumbers
) {}

// Validation cascades through nested structures
User user = new User("John", "john@example.com", address, phones);
ValidationResponse response = Validator.validate(user);

// Error paths show nested field structure:
// "address.street: The street field is required."
// "phoneNumbers[0].number: The number must be exactly 10 digits."
```

**Supports:** Single nested objects, collections (List, Set), arrays, deep nesting, and null-safe collection handling.

See the **[Supported Rules Guide](docs/supported-rules.md)** for detailed examples of all validation rules including regex, enum, date validation, and more.

## Security

Javalidator is designed with security in mind, providing several protections against common attacks:

### Built-in Security Features

1. **Type-Safe Enum Validation**
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

2. **Pattern Caching (No ReDoS Risk)**
   - Regex patterns are defined at compile-time by developers, not by users
   - Patterns are cached for performance
   - ReDoS (Regular Expression Denial of Service) is not a security concern

3. **Field Name Validation**
   - Conditional rules validate field names against configured patterns
   - Prevents field name injection attacks
   - Default pattern: `^[a-zA-Z_][a-zA-Z0-9_]*$`

4. **Reflection Depth Limiting**
   - Configurable maximum class hierarchy depth
   - Prevents memory exhaustion via deeply nested class hierarchies
   - Default limit: 10 levels

### Security Configuration

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

### Configuration Presets

- **`ValidationConfig.defaults()`** - Balanced security and performance for production
- **`ValidationConfig.strict()`** - Maximum security with all protections enabled
- **`ValidationConfig.permissive()`** - Minimal checks for development/testing

### Security Best Practices

1. Use `ValidationConfig.strict()` in production environments
2. Always use the type-safe `enumClass` parameter for enum validation
3. Validate input at system boundaries (controllers, APIs)
4. Keep field names simple and follow Java naming conventions
5. Review custom validation rules for security implications

### Threat Model

**Protected Against:**
- Arbitrary class loading via enum validation
- Field name injection in conditional rules
- Memory exhaustion via unbounded reflection

**Not Applicable:**
- ReDoS (developers control regex patterns at compile-time)
- SQL injection (validation library doesn't interact with databases)
- XSS (validation library doesn't generate HTML)

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## Roadmap

### 🎯 Advanced Validation
- [x] Nested object validation
- [x] Collection validation (lists, arrays)
- [ ] Internationalization (i18n) support

### 🚀 Framework Adapters
- [ ] Spring Boot starter with AOP integration
- [ ] Quarkus extension
- [ ] Jakarta EE module

> **Note**: Framework adapters will be created based on community demand. Integration guides are currently available for all major frameworks.

## Examples

See the [javalidator-examples](examples/) module for complete working examples.

## Support

For issues, questions, or contributions, please visit:
- **Issues**: [GitHub Issues](https://github.com/emmajiugo/javalidator/issues)

## Acknowledgments

Inspired by Laravel's validation approach and designed for the Java ecosystem.

---