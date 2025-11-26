# Supported Validation Rules

Javalidator comes with **33 built-in validation rules**. All rules can be combined using the pipe `|` separator for complex validations.

## Quick Reference Tables

### Basic Validation Rules

| Rule | Syntax | Description | Example |
|------|--------|-------------|---------|
| **required** | `required` | Field must not be null or blank | `@Rule("required")` |
| **min** | `min:n` | String must have at least n characters | `@Rule("min:3")` |
| **max** | `max:n` | String must not exceed n characters | `@Rule("max:50")` |
| **email** | `email` | Must be a valid email format | `@Rule("email")` |
| **numeric** | `numeric` | Must be a number type | `@Rule("numeric")` |
| **gt** | `gt:n` | Number must be greater than n | `@Rule("gt:0")` |
| **lt** | `lt:n` | Number must be less than n | `@Rule("lt:100")` |
| **gte** | `gte:n` | Number must be ≥ n | `@Rule("gte:18")` |
| **lte** | `lte:n` | Number must be ≤ n | `@Rule("lte:65")` |
| **between** | `between:min,max` | Number must be between min and max | `@Rule("between:18,65")` |
| **regex** | `regex:pattern` | Must match regex pattern | `@Rule("regex:^[A-Z]{2}\\d{4}$")` |
| **in** | `in:val1,val2` | Must be one of specified values | `@Rule("in:admin,user,guest")` |
| **size** | `size:n` | Must be exactly n characters/items | `@Rule("size:5")` |

### String Format Rules

| Rule | Syntax | Description | Example |
|------|--------|-------------|---------|
| **alpha** | `alpha` | Only alphabetic characters | `@Rule("alpha")` |
| **alpha_num** | `alpha_num` | Only alphanumeric characters | `@Rule("alpha_num")` |
| **url** | `url` | Must be a valid URL | `@Rule("url")` |
| **ip** | `ip` | Must be a valid IPv4 address | `@Rule("ip")` |
| **uuid** | `uuid` | Must be a valid UUID | `@Rule("uuid")` |
| **json** | `json` | Must be valid JSON | `@Rule("json")` |
| **enum** | `enum` with `enumClass` param | String must match enum constant | `@Rule(value="enum", enumClass=Status.class)` |

### Date/Time Rules

| Rule | Syntax | Description | Example |
|------|--------|-------------|---------|
| **date** | `date` or `date:format` | Must be a date type or match format | `@Rule("date")` or `@Rule("date:dd-MM-yyyy")` |
| **before** | `before:yyyy-MM-dd` | Date must be before specified date | `@Rule("before:2025-12-31")` |
| **after** | `after:yyyy-MM-dd` | Date must be after specified date | `@Rule("after:2024-01-01")` |
| **future** | `future` | Date must be in the future | `@Rule("future")` |
| **past** | `past` | Date must be in the past | `@Rule("past")` |

### Conditional Rules

| Rule | Syntax | Description | Example |
|------|--------|-------------|---------|
| **confirmed** | `confirmed` or `confirmed:fieldName` | Field must match confirmation field | `@Rule("confirmed")` |
| **required_if** | `required_if:field,value` | Required if another field has value | `@Rule("required_if:country,USA")` |
| **required_unless** | `required_unless:field,value` | Required unless another field has value | `@Rule("required_unless:payment,cash")` |

### Nested Validation

| Annotation | Description | Example |
|------------|-------------|---------|
| **@RuleCascade** | Validates nested objects and collections | `@RuleCascade Address address` |

## Combining Rules

Multiple rules can be combined using the pipe `|` separator:

```java
// String validation with multiple constraints
@Rule("required|min:3|max:20")
String username;

// Email validation
@Rule("required|email|max:100")
String email;

// Numeric range validation
@Rule("required|numeric|gte:0|lte:999")
Integer quantity;

// Enum-like validation
@Rule("required|in:active,inactive,pending")
String status;

// Pattern validation
@Rule("required|regex:^[A-Z]{3}-\\d{4}$")
String productCode; // Format: ABC-1234
```

## Custom Error Messages

You can override default error messages by using multiple `@Rule` annotations:

```java
public record UserDTO(
    @Rule(value = "required", message = "Username is required")
    @Rule(value = "min:3", message = "Username must be at least 3 characters")
    @Rule(value = "max:20", message = "Username cannot exceed 20 characters")
    String username,

    @Rule(value = "required", message = "Email is required")
    @Rule(value = "email", message = "Please provide a valid email address")
    String email
) {}
```

## Detailed Rule Examples

### Date Rule with Format

The `date` rule now supports format validation for String values:

```java
// Type checking for date objects
@Rule("date")
LocalDate birthDate; // Checks if it's a LocalDate, LocalDateTime, or Date

// Format validation for strings
@Rule("date:dd-MM-yyyy")
String dateString; // Must match format: 25-12-2023

@Rule("date:MM/dd/yyyy")
String usDate; // Must match format: 12/25/2023

@Rule("date:yyyy-MM-dd")
String isoDate; // Must match format: 2023-12-25

@Rule("date:yyyy-MM-dd'T'HH:mm:ss")
String dateTime; // Must match format: 2023-12-25T14:30:00
```

### Enum Rule

The `enum` rule validates that a String matches a valid enum constant. Requires the `enumClass` parameter:

```java
// Define your enum
public enum OrderStatus {
    DRAFT, PROCESSING, SHIPPED, DELIVERED, CANCELLED
}

// Validate String against enum
public record OrderDTO(
    @Rule(value = "enum", enumClass = OrderStatus.class)
    String status // Must be one of: DRAFT, PROCESSING, SHIPPED, DELIVERED, CANCELLED
) {}

// For actual enum fields, just use 'required'
public record OrderDTO2(
    @Rule("required")
    OrderStatus status // Java's type system handles validation
) {}
```

**Note**: The `enum` rule requires the `enumClass` parameter to be set to the enum class reference (not a string). This provides type-safe validation without arbitrary class loading.

## Common Validation Patterns

### User Registration

```java
public record RegistrationDTO(
    @Rule("required|min:3|max:20")
    String username,

    @Rule("required|email|max:100")
    String email,

    @Rule("required|min:8|max:50")
    String password,

    @Rule("required|gte:18|lte:100")
    Integer age
) {}
```

### Product Information

```java
public record ProductDTO(
    @Rule("required|min:3|max:100")
    String name,

    @Rule("min:10|max:500")
    String description,

    @Rule("required|numeric|gte:0")
    Double price,

    @Rule("required|numeric|gte:0")
    Integer stock,

    @Rule("required|in:active,inactive,discontinued")
    String status
) {}
```

### Order Processing

```java
public record OrderDTO(
    @Rule("required|regex:^ORD-\\d{6}$")
    String orderNumber, // Format: ORD-123456

    @Rule("required|in:pending,processing,shipped,delivered,cancelled")
    String status,

    @Rule("required|numeric|gte:1")
    Integer quantity,

    @Rule("required|date")
    LocalDate orderDate
) {}
```

## Rule Execution Order

Rules are executed in the order they appear:

```java
// Executed in order: required → min → max
@Rule("required|min:3|max:20")
String username;

// With multiple annotations, executed top to bottom:
@Rule(value = "required", message = "Required")  // 1st
@Rule(value = "min:3", message = "Too short")    // 2nd
@Rule(value = "max:20", message = "Too long")    // 3rd
String username;
```

## Null Value Handling

- **required**: Fails if value is `null` or blank string
- **All other rules**: Skip validation if value is `null` (let `required` handle nulls)

```java
// age is optional, but if provided, must be >= 18
@Rule("gte:18")
Integer age; // null is OK, but if set, must be >= 18

// age is required AND must be >= 18
@Rule("required|gte:18")
Integer age; // null fails, value < 18 fails
```

## Adding Custom Rules

Need a rule that's not in this list? See the **[Custom Validation Rules Guide](custom-rules.md)** to learn how to create your own rules.

Common custom rules developers create:
- Phone number validation
- Credit card validation
- Social security number validation
- Business-specific logic
- Database uniqueness checks

## Conditional Rules

Conditional validation rules have access to the entire DTO object, allowing them to make validation decisions based on other field values. These rules use the `ConditionalValidationRule` interface, which extends the base `ValidationRule` interface.

### Required If Rule

Makes a field required when another field has a specific value:

```java
public record OrderDTO(
    String paymentMethod,
    @Rule("required_if:paymentMethod,card")
    String cardNumber,

    @Rule("required_if:paymentMethod,card")
    String cvv
) {}

// When paymentMethod is "card", cardNumber and cvv are required
// When paymentMethod is anything else, they're optional
```

### Required Unless Rule

Makes a field required unless another field has a specific value:

```java
public record ShippingDTO(
    String deliveryMethod,
    @Rule("required_unless:deliveryMethod,pickup")
    String address
) {}

// Address is required UNLESS deliveryMethod is "pickup"
// If deliveryMethod is "delivery", "mail", etc., address is required
```

### Confirmed Rule

Validates that a field matches its confirmation field. By default, looks for a field with `_confirmation` suffix:

```java
public record RegistrationDTO(
    @Rule("required|min:8")
    @Rule("confirmed")
    String password,

    String password_confirmation  // Must match password
) {}

// You can also specify a custom confirmation field:
public record RegistrationDTO2(
    @Rule("required|min:8")
    @Rule("confirmed:passwordConfirm")
    String password,

    String passwordConfirm  // Must match password
) {}
```

### Combining Conditional Rules with Other Rules

Conditional rules can be combined with other validation rules:

```java
public record FormDTO(
    String country,

    // When country is USA, state is required AND must be exactly 2 characters
    @Rule("required_if:country,USA|size:2|alpha")
    String state
) {}
```

## Field Comparison Rules

### `same` - Field Must Match Another Field

Validates that a field's value matches another field's value. Commonly used for email/password confirmation.

**Syntax:** `same:otherFieldName`

**Examples:**

```java
// Email confirmation
public record EmailConfirmationDTO(
    @Rule("required|email")
    String email,

    @Rule("required|same:email")
    String emailConfirmation
) {}

// Password confirmation
public record PasswordDTO(
    @Rule("required|min:8")
    String password,

    @Rule("required|same:password")
    String passwordConfirmation
) {}
```

### `different` - Field Must Differ from Another Field

Validates that a field's value is different from another field's value. Useful for password changes.

**Syntax:** `different:otherFieldName`

**Examples:**

```java
// Password change - new password must differ from old
public record PasswordChangeDTO(
    @Rule("required")
    String oldPassword,

    @Rule("required|min:8|different:oldPassword")
    String newPassword
) {}

// Alternative email must differ from primary
public record ContactDTO(
    @Rule("required|email")
    String primaryEmail,

    @Rule("email|different:primaryEmail")
    String alternativeEmail
) {}
```

## Control Flow Rules

### `nullable` - Explicit Null Handling

Marks a field as explicitly allowing null values. This is primarily a documentation marker, as most rules already skip null values by default (only `required` enforces non-null).

**Syntax:** `nullable`

**Examples:**

```java
// Optional fields
public record UserProfileDTO(
    @Rule("required|email")
    String email,

    @Rule("nullable|url")
    String website,  // Can be null, but if provided must be a valid URL

    @Rule("nullable|numeric|gte:0")
    Integer age  // Can be null, but if provided must be numeric and >= 0
) {}
```

**Note:** The `nullable` rule makes intent explicit in your code. Without `required`, fields are nullable by default, but using `nullable` makes this behavior clear to other developers.

## String Format Rules (Continued)

### `digits` - Exact Digit Count

Validates that a field contains exactly the specified number of digits, with no other characters.

**Syntax:** `digits:count`

**Examples:**

```java
// PIN code validation
public record SecurityDTO(
    @Rule("required|digits:4")
    String pin  // Must be exactly 4 digits: 1234, 0000, etc.
) {}

// US ZIP code
public record AddressDTO(
    @Rule("required")
    String street,

    @Rule("required|digits:5")
    String zipCode  // Must be exactly 5 digits
) {}

// Verification code
public record VerificationDTO(
    @Rule("required|digits:6")
    String code  // Must be exactly 6 digits
) {}

// Works with numeric types too
public record PinDTO(
    @Rule("required|digits:4")
    Integer pin  // 1234 is valid, 12345 is not
) {}
```

## Array/Collection Rules

### `distinct` - Array/Collection Uniqueness

Validates that all elements in an array or collection are unique (no duplicates).

**Syntax:** `distinct`

**Supported Types:**
- Arrays: `int[]`, `String[]`, `Integer[]`, etc.
- Collections: `List`, `ArrayList`, `LinkedList`, etc.
- Sets: Always pass (inherently unique)

**Examples:**

```java
// Unique product IDs
public record OrderDTO(
    @Rule("required|distinct")
    Integer[] productIds  // [1, 2, 3] is valid, [1, 2, 2] is not
) {}

// Unique tags
public record ArticleDTO(
    @Rule("required")
    String title,

    @Rule("distinct")
    List<String> tags  // ["java", "spring", "maven"] is valid
) {}

// Unique email list
public record InvitationDTO(
    @Rule("required|distinct")
    String[] emails  // All emails must be unique
) {}
```

**Behavior:**
- Empty arrays/collections are considered valid
- Null values are considered valid (use `required` to enforce non-null)
- Uses `.equals()` for element comparison
- Sets always pass validation (inherently unique)

## Nested Object and Collection Validation

### `@RuleCascade` - Nested Validation

Validates nested objects and collections recursively. When applied to a field, the validator will cascade validation through the nested structure.

**Annotation:** `@RuleCascade`

**Supported Types:**
- Single nested objects (records, classes)
- Collections (List, Set, etc.)
- Arrays
- Deep nesting (multiple levels)

**Examples:**

```java
import me.emmajiugo.javalidator.annotations.RuleCascade;

// Nested record validation
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

    @RuleCascade  // Validates the nested Address object
    Address address
) {}

// Validation cascades through the nested structure
User user = new User("John", "john@example.com", new Address("", "NYC", "ABC"));
ValidationResponse response = Validator.validate(user);

// Error paths show the nested field structure:
// "address.street: The street field is required."
// "address.zipCode: The zipCode must contain only digits."
```

**Collection Validation:**

```java
public record PhoneNumber(
    @Rule("required|digits:10")
    String number,

    @Rule("required|in:mobile,home,work")
    String type
) {}

public record User(
    @Rule("required")
    String name,

    @RuleCascade  // Validates each PhoneNumber in the list
    List<PhoneNumber> phoneNumbers
) {}

// Error messages include array index:
// "phoneNumbers[0].number: The number must be exactly 10 digits."
// "phoneNumbers[1].type: The type must be one of: mobile,home,work."
```

**Deep Nesting:**

```java
public record Item(
    @Rule("required|min:3")
    String name,

    @Rule("required|gte:0")
    Double price
) {}

public record Order(
    @Rule("required")
    String orderId,

    @RuleCascade
    List<Item> items
) {}

public record Customer(
    @Rule("required|email")
    String email,

    @RuleCascade  // Validates nested list of orders
    List<Order> orders
) {}

// Error paths show the full nested structure:
// "orders[0].items[1].price: The price must be at least 0."
```

**Combining with Other Rules:**

```java
public record User(
    @Rule("required")
    String name,

    @Rule("required")  // Address field itself must not be null
    @RuleCascade       // AND address fields must be valid
    Address address
) {}
```

**Behavior:**
- Null nested objects/collections are allowed by default (use `@Rule("required")` to enforce non-null)
- Null items within collections are skipped
- All nested validation errors are grouped under the parent field name
- Works with both records and traditional classes
- Supports unlimited nesting depth

Want to see a specific rule? Open an issue on GitHub!

---

[← Back to README](../README.md) | [Custom Rules Guide →](custom-rules.md)
