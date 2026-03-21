# Map Validation Design

**Date:** 2026-03-21
**Status:** Approved
**Scope:** Add programmatic validation for `Map<String, ?>` with dot-notation nested key support

---

## Motivation

Many Java web applications receive request bodies as `Map<String, Object>` or `Map<String, String>` rather than typed DTOs — especially with frameworks like Inertia.js, generic REST endpoints, or when working with dynamic schemas. Currently, validating Map entries requires manual checks per key. This feature brings Laravel-style `$request->validate([...])` to javalidator.

---

## Public API

Two new static methods on `Validator`, following the existing `validate`/`validateOrThrow` pattern:

```java
/**
 * Validates a Map's entries against the specified rules.
 *
 * @param data  the map to validate (supports nested maps via dot-notation keys)
 * @param rules map of field paths to pipe-separated rule strings
 * @return ValidationResponse with errors keyed by the dot-path field name
 */
public static ValidationResponse validateMap(Map<String, ?> data, Map<String, String> rules)

/**
 * Validates a Map and throws NotValidException if validation fails.
 */
public static void validateMapOrThrow(Map<String, ?> data, Map<String, String> rules)
```

### Usage Example

```java
@PostMapping("/events")
public void store(@RequestBody Map<String, String> body) {
    Validator.validateMapOrThrow(body, Map.of(
        "title", "required|min:3",
        "description", "required",
        "date", "required|date:yyyy-MM-dd"
    ));

    eventService.create(new Event(
        null,
        body.get("title"),
        body.get("description"),
        LocalDate.parse(body.get("date"))
    ));
}
```

### Nested Map Example

```java
Map<String, Object> body = Map.of(
    "name", "John",
    "address", Map.of(
        "city", "Lagos",
        "zip", "100001"
    )
);

Validator.validateMapOrThrow(body, Map.of(
    "name", "required|min:2",
    "address.city", "required",
    "address.zip", "required|digits:6"
));
// Errors would use the full dot-path as field name: "address.zip"
```

---

## Dot-Path Resolution

For a rule key like `"location.city"`, the resolver walks the data map segment by segment:

1. Split the key on `.` → `["location", "city"]`
2. `data.get("location")` → if result is a `Map`, continue; otherwise the value is null
3. `nestedMap.get("city")` → final value

**Edge cases:**
- If an intermediate segment is null → final value is null (let `required` catch it)
- If an intermediate segment is not a Map (e.g., a String) → final value is null
- If the data map itself is null → return a single error: "Validation data cannot be null"
- Top-level keys (no dots) → simple `data.get(key)`

---

## Internal Implementation

### Value Resolution

New package-private utility method (can live in `Validator.java` or a `MapUtils` helper):

```java
static Object resolveMapValue(Map<String, ?> data, String path) {
    String[] segments = path.split("\\.");
    Object current = data;
    for (String segment : segments) {
        if (current instanceof Map<?, ?> map) {
            current = map.get(segment);
        } else {
            return null;
        }
    }
    return current;
}
```

### validateMap Implementation

```java
public static ValidationResponse validateMap(Map<String, ?> data, Map<String, String> rules) {
    ensureInitialized();

    if (data == null) {
        return ValidationResponse.failure(List.of(
            new ValidationError("data", List.of("Validation data cannot be null"), List.of("required"))
        ));
    }

    if (rules == null || rules.isEmpty()) {
        return ValidationResponse.success();
    }

    List<ValidationError> errors = new ArrayList<>();

    for (Map.Entry<String, String> entry : rules.entrySet()) {
        String fieldPath = entry.getKey();
        String ruleString = entry.getValue();

        Object value = resolveMapValue(data, fieldPath);

        ValidationResponse response = validateValue(value, ruleString, fieldPath);
        if (!response.valid()) {
            errors.addAll(response.errors());
        }
    }

    return errors.isEmpty()
            ? ValidationResponse.success()
            : ValidationResponse.failure(errors);
}
```

**Key design choice:** `validateMap` delegates to the existing `validateValue()` for each field. This means:
- All 36 built-in rules work automatically
- Bail support works per field
- Strict/graceful mode works
- Rule names are populated in `ValidationError.rules()`
- The same restrictions apply: conditional rules (`required_if`, `same`, etc.) and `enum` rule are not supported (they require DTO context)

### validateMapOrThrow

```java
public static void validateMapOrThrow(Map<String, ?> data, Map<String, String> rules) {
    ValidationResponse response = validateMap(data, rules);
    if (!response.valid()) {
        throw new NotValidException("Map validation failed", response.errors());
    }
}
```

---

## What This Does NOT Support

- **Spring AOP integration** — Map validation is programmatic only. No annotation-based auto-validation for Map parameters.
- **Wildcard paths** like `"items.*.name"` — could be added in a future release.
- **Array index notation** like `"items[0].name"` — could be added in a future release.
- **Conditional rules** (`required_if`, `required_unless`, `same`, `different`) — these require DTO context. Same limitation as `validateValue()`.
- **Enum rule** — requires `enumClass` from annotation. Not available in programmatic API.

---

## Decisions Log

| Decision | Choice | Rationale |
|----------|--------|-----------|
| API pattern | `validateMap` + `validateMapOrThrow` | Consistent with existing `validate`/`validateOrThrow` pattern |
| Map type support | `Map<String, ?>` | Accepts both `Map<String, String>` and `Map<String, Object>` |
| Nested access | Dot-notation path resolution | Matches Laravel's dot notation, intuitive for nested structures |
| Missing intermediate | Resolve to null | Lets `required` rule catch missing nested paths naturally |
| Spring integration | None (programmatic only) | No natural place for annotations on Map keys; explicit is better |
| Rule delegation | Reuse `validateValue()` | Zero duplication, all rules work automatically, same restrictions |
