# Creating Custom Validation Rules

This guide shows you how to create and register custom validation rules in Javalidator.

## Overview

Javalidator comes with its own built-in basic validation rules, 
but you can easily add your own custom rules to validate domain-specific requirements like:
- Phone numbers
- Credit cards
- Social security numbers
- Custom business logic
- Integration with external systems

## Quick Example

Here's a complete example of creating a custom phone number validator:

```java
import me.emmajiugo.javalidator.ValidationRule;
import me.emmajiugo.javalidator.RuleRegistry;

// 1. Implement ValidationRule interface
public class PhoneRule implements ValidationRule {

    @Override
    public String validate(String fieldName, Object value, String parameter) {
        if (value instanceof String phone) {
            // E.164 international phone number format
            if (!phone.matches("^\\+?[1-9]\\d{1,14}$")) {
                return "The " + fieldName + " must be a valid phone number.";
            }
        }
        return null; // null means validation passed
    }

    @Override
    public String getName() {
        return "phone"; // Optional: defaults to "phone" from class name "PhoneRule"
    }
}

// 2. Register the rule (once, at application startup)
public class Application {
    public static void main(String[] args) {
        RuleRegistry.register(new PhoneRule());

        // Now you can use it
        // @Rule("required|phone")
    }
}

// 3. Use in your DTOs
public record ContactDTO(
    @Rule("required|phone")
    String phoneNumber
) {}
```

## Step-by-Step Guide

### Step 1: Create Your Rule Class

Implement the `ValidationRule` interface:

```java
package com.example.validation.rules;

import me.emmajiugo.javalidator.ValidationRule;

public class CreditCardRule implements ValidationRule {

    @Override
    public String validate(String fieldName, Object value, String parameter) {
        if (value instanceof String cardNumber) {
            if (!isValidCreditCard(cardNumber)) {
                return "The " + fieldName + " must be a valid credit card number.";
            }
        }
        return null; // Validation passed
    }

    @Override
    public String getName() {
        return "credit_card"; // Rule name used in @Rule("credit_card")
    }

    private boolean isValidCreditCard(String number) {
        // Luhn algorithm implementation
        number = number.replaceAll("\\s+", "");
        if (!number.matches("\\d+")) return false;

        int sum = 0;
        boolean alternate = false;
        for (int i = number.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(number.substring(i, i + 1));
            if (alternate) {
                n *= 2;
                if (n > 9) n = (n % 10) + 1;
            }
            sum += n;
            alternate = !alternate;
        }
        return (sum % 10 == 0);
    }
}
```

### Step 2: Register Your Rule

Register the rule **once** at application startup:

#### Plain Java Application

```java
public class Main {
    public static void main(String[] args) {
        // Register custom rules
        RuleRegistry.register(new CreditCardRule());
        RuleRegistry.register(new PhoneRule());

        // Start your application
        // ...
    }
}
```

#### Spring Boot Application

```java
@Configuration
public class ValidationConfig {

    @PostConstruct
    public void registerCustomRules() {
        RuleRegistry.register(new CreditCardRule());
        RuleRegistry.register(new PhoneRule());
        RuleRegistry.register(new SsnRule());
    }
}
```

#### Quarkus Application

```java
@ApplicationScoped
public class ValidationRules {

    void onStart(@Observes StartupEvent event) {
        RuleRegistry.register(new CreditCardRule());
        RuleRegistry.register(new PhoneRule());
    }
}
```

### Step 3: Use Your Custom Rule

Now you can use your custom rule just like built-in rules:

```java
public record PaymentDTO(
    @Rule("required|credit_card")
    String cardNumber,

    @Rule("required|phone")
    String contactPhone,

    @Rule("required|min:3|max:100")
    String cardholderName
) {}
```

## Advanced Examples

### Rule with Parameters

Create rules that accept parameters (like `min:3` or `max:20`):

```java
public class MinAgeRule implements ValidationRule {

    @Override
    public String validate(String fieldName, Object value, String parameter) {
        if (value instanceof Integer age) {
            int minAge = Integer.parseInt(parameter); // parameter is "18" from "min_age:18"

            if (age < minAge) {
                return "The " + fieldName + " must be at least " + minAge + " years old.";
            }
        }
        return null;
    }

    @Override
    public String getName() {
        return "min_age";
    }
}

// Usage:
// @Rule("required|min_age:18")
// Integer age;
```

### Rule with Multiple Type Support

Handle different data types:

```java
public class FutureRule implements ValidationRule {

    @Override
    public String validate(String fieldName, Object value, String parameter) {
        if (value == null) return null;

        LocalDate futureDate = null;

        // Support multiple date types
        if (value instanceof LocalDate date) {
            futureDate = date;
        } else if (value instanceof LocalDateTime dateTime) {
            futureDate = dateTime.toLocalDate();
        } else if (value instanceof Date date) {
            futureDate = date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        }

        if (futureDate != null && !futureDate.isAfter(LocalDate.now())) {
            return "The " + fieldName + " must be a future date.";
        }

        return null;
    }

    @Override
    public String getName() {
        return "future";
    }
}
```

### Rule with External Validation

Integrate with external services or databases:

```java
public class UniqueEmailRule implements ValidationRule {

    private final UserRepository userRepository;

    public UniqueEmailRule(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public String validate(String fieldName, Object value, String parameter) {
        if (value instanceof String email) {
            if (userRepository.existsByEmail(email)) {
                return "The " + fieldName + " is already taken.";
            }
        }
        return null;
    }

    @Override
    public String getName() {
        return "unique_email";
    }
}

// Register with dependency
RuleRegistry.register(new UniqueEmailRule(userRepository));
```

### Conditional Rule

Rules that depend on parameters or other conditions:

```java
public class StrongPasswordRule implements ValidationRule {

    @Override
    public String validate(String fieldName, Object value, String parameter) {
        if (value instanceof String password) {
            List<String> errors = new ArrayList<>();

            if (password.length() < 8) {
                errors.add("at least 8 characters");
            }
            if (!password.matches(".*[A-Z].*")) {
                errors.add("at least one uppercase letter");
            }
            if (!password.matches(".*[a-z].*")) {
                errors.add("at least one lowercase letter");
            }
            if (!password.matches(".*\\d.*")) {
                errors.add("at least one number");
            }
            if (!password.matches(".*[@#$%^&+=!].*")) {
                errors.add("at least one special character");
            }

            if (!errors.isEmpty()) {
                return "The " + fieldName + " must contain " + String.join(", ", errors) + ".";
            }
        }
        return null;
    }

    @Override
    public String getName() {
        return "strong_password";
    }
}
```

## Best Practices

### 1. Naming Convention

Follow consistent naming patterns:

```java
// Good: Descriptive, clear names
PhoneRule          -> "phone"
CreditCardRule     -> "credit_card" (override getName())
StrongPasswordRule -> "strong_password"

// Avoid: Generic or confusing names
ValidatorRule      -> too generic
CheckRule          -> unclear purpose
```

### 2. Return null for Success

Always return `null` when validation passes:

```java
@Override
public String validate(String fieldName, Object value, String parameter) {
    if (isValid(value)) {
        return null; // ✓ Correct
    }
    return errorMessage;
}

// Don't do this:
return isValid(value) ? "" : errorMessage; // ✗ Wrong - return null, not ""
```

### 3. Handle Null Values Gracefully

Decide if your rule should validate null values:

```java
@Override
public String validate(String fieldName, Object value, String parameter) {
    // Option 1: Skip null values (let "required" rule handle nulls)
    if (value == null) return null;

    // Option 2: Treat null as invalid
    if (value == null) {
        return "The " + fieldName + " is required.";
    }

    // Your validation logic...
}
```

### 4. Type Safety

Use `instanceof` pattern matching for type safety:

```java
@Override
public String validate(String fieldName, Object value, String parameter) {
    // ✓ Good: Safe type checking
    if (value instanceof String str) {
        return validateString(str);
    }

    // ✗ Avoid: Unsafe casting
    String str = (String) value; // May throw ClassCastException
}
```

### 5. Clear Error Messages

Write user-friendly error messages:

```java
// ✓ Good: Clear, actionable
return "The " + fieldName + " must be a valid phone number in E.164 format.";

// ✗ Avoid: Technical or vague
return "Invalid format"; // Too vague
return "Regex ^\\+?[1-9]\\d{1,14}$ failed"; // Too technical
```

### 6. Performance Considerations

Cache expensive operations:

```java
public class EmailRule implements ValidationRule {
    // ✓ Good: Compile pattern once
    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    @Override
    public String validate(String fieldName, Object value, String parameter) {
        if (value instanceof String email) {
            // Reuse compiled pattern
            if (!EMAIL_PATTERN.matcher(email).matches()) {
                return "Invalid email";
            }
        }
        return null;
    }
}
```

### Alternative: Using validateOrThrow()

For cleaner controller code, use `validateOrThrow()` which throws a `ValidationException` on failure:
```java
package com.example.myapp.controller;

import com.example.myapp.dto.CreateMemberRequest;
import me.emmajiugo.javalidator.Validator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    @PostMapping
    public ResponseEntity<?> createMember(@RequestBody CreateMemberRequest request) {
        // Throws ValidationException if validation fails
        Validator.validateOrThrow(request);
        
        // Only reached if validation passes
        return ResponseEntity.ok("Member created successfully");
    }
}
```

Then handle the exception globally with `@RestControllerAdvice`:
```java
package com.example.myapp.exception;

import me.emmajiugo.javalidator.ValidationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<?> handleValidationException(ValidationException ex) {
        return ResponseEntity.badRequest().body(ex.getErrors());
    }
}
```

**When to use which approach:**

| Approach | Use When |
|----------|----------|
| `validate()` | You need custom handling, partial validation, or want to combine errors from multiple sources |
| `validateOrThrow()` | You want cleaner controller code and centralized exception handling |

## Testing Your Custom Rules

### Unit Test Example

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PhoneRuleTest {

    private final PhoneRule rule = new PhoneRule();

    @Test
    void testValidPhoneNumber() {
        String result = rule.validate("phone", "+1234567890", null);
        assertNull(result, "Valid phone should pass");
    }

    @Test
    void testInvalidPhoneNumber() {
        String result = rule.validate("phone", "invalid", null);
        assertNotNull(result, "Invalid phone should fail");
        assertTrue(result.contains("valid phone number"));
    }

    @Test
    void testNullValue() {
        String result = rule.validate("phone", null, null);
        assertNull(result, "Null should be allowed (let 'required' rule handle it)");
    }

    @Test
    void testRuleName() {
        assertEquals("phone", rule.getName());
    }
}
```

### Integration Test

```java
@Test
void testCustomRuleInDTO() {
    // Register custom rule
    RuleRegistry.register(new PhoneRule());

    // Create DTO with invalid phone
    ContactDTO dto = new ContactDTO("invalid-phone");

    // Validate
    ValidationResponse response = Validator.validate(dto);

    // Assert
    assertFalse(response.valid());
    assertEquals(1, response.errors().size());
    assertEquals("phoneNumber", response.errors().get(0).field());
}
```

## Common Use Cases

### 1. Social Security Number (SSN)

```java
public class SsnRule implements ValidationRule {

    @Override
    public String validate(String fieldName, Object value, String parameter) {
        if (value instanceof String ssn) {
            // Format: XXX-XX-XXXX
            if (!ssn.matches("^\\d{3}-\\d{2}-\\d{4}$")) {
                return "The " + fieldName + " must be in format XXX-XX-XXXX.";
            }
        }
        return null;
    }

    @Override
    public String getName() {
        return "ssn";
    }
}
```

### 2. URL Validation

```java
public class UrlRule implements ValidationRule {

    @Override
    public String validate(String fieldName, Object value, String parameter) {
        if (value instanceof String url) {
            try {
                new java.net.URL(url);
                return null; // Valid URL
            } catch (java.net.MalformedURLException e) {
                return "The " + fieldName + " must be a valid URL.";
            }
        }
        return null;
    }

    @Override
    public String getName() {
        return "url";
    }
}
```

### 3. IP Address Validation

```java
public class IpAddressRule implements ValidationRule {

    @Override
    public String validate(String fieldName, Object value, String parameter) {
        if (value instanceof String ip) {
            String ipv4Pattern =
                "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}" +
                "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";

            if (!ip.matches(ipv4Pattern)) {
                return "The " + fieldName + " must be a valid IPv4 address.";
            }
        }
        return null;
    }

    @Override
    public String getName() {
        return "ip_address";
    }
}
```

### 4. JSON Validation

```java
public class JsonRule implements ValidationRule {

    @Override
    public String validate(String fieldName, Object value, String parameter) {
        if (value instanceof String json) {
            try {
                // Use your preferred JSON library
                new com.google.gson.JsonParser().parse(json);
                return null; // Valid JSON
            } catch (Exception e) {
                return "The " + fieldName + " must be valid JSON.";
            }
        }
        return null;
    }

    @Override
    public String getName() {
        return "json";
    }
}
```

## Troubleshooting

### Rule Not Found

**Problem**: Getting "Unknown validation rule: myRule" error

**Solution**: Ensure you registered the rule before using it:

```java
// Register BEFORE validation
RuleRegistry.register(new MyRule());

// Then validate
ValidationResponse response = Validator.validate(dto);
```

### Rule Name Doesn't Match

**Problem**: Rule name doesn't match what you expect

**Solution**: Override `getName()` explicitly:

```java
@Override
public String getName() {
    return "my_custom_rule"; // Explicit name
}
```

### Rule Not Triggered

**Problem**: Custom rule seems to be ignored

**Solution**: Check that:
1. Rule is registered before validation
2. Rule name in `@Rule` matches `getName()`
3. Field type matches what rule expects

```java
// Wrong:
@Rule("phone") // Rule name doesn't match
String phoneNumber;

// Right:
@Rule("phone") // Matches PhoneRule.getName() -> "phone"
String phoneNumber;
```

## FAQ

**Q: When should I register custom rules?**
A: Register once at application startup, before any validation occurs.

**Q: Can I override built-in rules?**
A: Yes, registering a rule with the same name will replace the existing one.

**Q: Are custom rules thread-safe?**
A: Yes, as long as your rule implementation is stateless or properly synchronized.

**Q: Can I use dependency injection in custom rules?**
A: Yes! Pass dependencies through the constructor, then register the instance.

**Q: How do I test custom rules?**
A: Create unit tests that call `validate()` directly with test data.

Happy validating! 🎉