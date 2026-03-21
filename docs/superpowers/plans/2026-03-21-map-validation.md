# Map Validation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add `validateMap()` and `validateMapOrThrow()` to `Validator` for programmatic validation of `Map<String, ?>` entries with dot-notation nested key support.

**Architecture:** Two new public methods delegate to the existing `validateValue()` pipeline per field. A private `resolveMapValue()` helper walks dot-separated paths through nested maps. Zero new classes needed — all changes are in `Validator.java`.

**Tech Stack:** Java 17, JUnit 5, AssertJ, Maven

**Spec:** `docs/superpowers/specs/2026-03-21-map-validation-design.md`

**Build command:** `mvn -pl javalidator-core test`

---

## File Structure

### Files to Modify

| File | What Changes |
|------|-------------|
| `javalidator-core/src/main/java/io/github/emmajiugo/javalidator/Validator.java` | Add `validateMap()`, `validateMapOrThrow()`, `resolveMapValue()` |

### Files to Create

| File | Purpose |
|------|---------|
| `javalidator-core/src/test/java/io/github/emmajiugo/javalidator/MapValidationTest.java` | All tests for map validation |

---

## Task 1: Core Map Validation — Happy Path

**Files:**
- Create: `javalidator-core/src/test/java/io/github/emmajiugo/javalidator/MapValidationTest.java`
- Modify: `javalidator-core/src/main/java/io/github/emmajiugo/javalidator/Validator.java`

- [ ] **Step 1: Write failing tests for flat map validation**

```java
package io.github.emmajiugo.javalidator;

import io.github.emmajiugo.javalidator.model.ValidationResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Map Validation")
class MapValidationTest {

    @Nested
    @DisplayName("Flat Map Validation")
    class FlatMapTests {

        @Test
        @DisplayName("should pass when all fields are valid")
        void shouldPassWhenAllFieldsValid() {
            Map<String, String> data = Map.of(
                    "title", "My Event",
                    "description", "A great event",
                    "date", "2026-12-25"
            );
            Map<String, String> rules = Map.of(
                    "title", "required|min:3",
                    "description", "required",
                    "date", "required"
            );

            ValidationResponse response = Validator.validateMap(data, rules);
            assertThat(response.valid()).isTrue();
        }

        @Test
        @DisplayName("should fail when required fields are missing")
        void shouldFailWhenRequiredFieldsMissing() {
            Map<String, String> data = Map.of("title", "Hi");
            Map<String, String> rules = Map.of(
                    "title", "required|min:3",
                    "description", "required"
            );

            ValidationResponse response = Validator.validateMap(data, rules);
            assertThat(response.valid()).isFalse();
            assertThat(response.errors()).hasSize(2);

            // title fails min:3, description fails required
            assertThat(response.errors())
                    .anyMatch(e -> e.field().equals("title") && e.rules().contains("min"));
            assertThat(response.errors())
                    .anyMatch(e -> e.field().equals("description") && e.rules().contains("required"));
        }

        @Test
        @DisplayName("should work with Map<String, Object>")
        void shouldWorkWithObjectMap() {
            Map<String, Object> data = Map.of(
                    "name", "John",
                    "age", 25
            );
            Map<String, String> rules = Map.of(
                    "name", "required|min:2",
                    "age", "required|numeric|gte:18"
            );

            ValidationResponse response = Validator.validateMap(data, rules);
            assertThat(response.valid()).isTrue();
        }
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `mvn -pl javalidator-core test -Dtest="MapValidationTest"`

Expected: FAIL — `validateMap` method does not exist.

- [ ] **Step 3: Implement resolveMapValue(), validateMap(), validateMapOrThrow()**

Add to `Validator.java` after the `validateValueOrThrow()` method:

```java
/**
 * Validates a Map's entries against the specified rules.
 *
 * <p>This method enables programmatic validation of Map data using the same
 * pipe-separated rule syntax. Nested map values can be accessed using
 * dot-notation paths (e.g., "address.city").
 *
 * <p>Example:
 * <pre>{@code
 * ValidationResponse response = Validator.validateMap(body, Map.of(
 *     "title", "required|min:3",
 *     "address.city", "required",
 *     "address.zip", "required|digits:5"
 * ));
 * }</pre>
 *
 * <p><strong>Note:</strong> Conditional rules (like {@code required_if}, {@code same},
 * {@code different}) and the {@code enum} rule cannot be used with this method —
 * same restrictions as {@link #validateValue(Object, String, String)}.
 *
 * @param data  the map to validate (supports nested maps via dot-notation keys)
 * @param rules map of field paths to pipe-separated rule strings
 * @return a ValidationResponse containing validation results
 * @throws IllegalArgumentException if a conditional rule or enum rule is used
 */
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
        if (fieldPath == null || fieldPath.isBlank()) {
            continue;
        }
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

/**
 * Validates a Map and throws an exception if validation fails.
 *
 * @param data  the map to validate
 * @param rules map of field paths to pipe-separated rule strings
 * @throws NotValidException if validation fails
 * @see #validateMap(Map, Map)
 */
public static void validateMapOrThrow(Map<String, ?> data, Map<String, String> rules) {
    ValidationResponse response = validateMap(data, rules);
    if (!response.valid()) {
        throw new NotValidException("Map validation failed", response.errors());
    }
}

/**
 * Resolves a value from a possibly-nested map using dot-notation path.
 *
 * @param data the map to resolve from
 * @param path dot-separated path (e.g., "address.city")
 * @return the resolved value, or null if any segment is missing or not a Map
 */
private static Object resolveMapValue(Map<String, ?> data, String path) {
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

Add `import java.util.Map;` to the imports if not already present.

- [ ] **Step 4: Run tests to verify they pass**

Run: `mvn -pl javalidator-core test -Dtest="MapValidationTest"`

Expected: All 3 tests pass.

- [ ] **Step 5: Commit**

```
feat: add validateMap() for programmatic map validation

Supports flat and dot-notation nested key resolution. Delegates to
validateValue() pipeline — all 36 built-in rules work automatically.
```

---

## Task 2: Nested Map + Edge Cases

**Files:**
- Modify: `javalidator-core/src/test/java/io/github/emmajiugo/javalidator/MapValidationTest.java`

- [ ] **Step 1: Add nested map and edge case tests**

Add to `MapValidationTest.java`:

```java
@Nested
@DisplayName("Nested Map Validation")
class NestedMapTests {

    @Test
    @DisplayName("should validate nested map values with dot notation")
    void shouldValidateNestedValues() {
        Map<String, Object> data = Map.of(
                "name", "John",
                "address", Map.of(
                        "city", "Lagos",
                        "zip", "100001"
                )
        );
        Map<String, String> rules = Map.of(
                "name", "required",
                "address.city", "required|min:2",
                "address.zip", "required|digits:6"
        );

        ValidationResponse response = Validator.validateMap(data, rules);
        assertThat(response.valid()).isTrue();
    }

    @Test
    @DisplayName("should fail for invalid nested values")
    void shouldFailForInvalidNestedValues() {
        Map<String, Object> data = Map.of(
                "name", "John",
                "address", Map.of("city", "")
        );
        Map<String, String> rules = Map.of(
                "address.city", "required",
                "address.zip", "required"
        );

        ValidationResponse response = Validator.validateMap(data, rules);
        assertThat(response.valid()).isFalse();
        assertThat(response.errors())
                .anyMatch(e -> e.field().equals("address.city"));
        assertThat(response.errors())
                .anyMatch(e -> e.field().equals("address.zip"));
    }

    @Test
    @DisplayName("should handle deeply nested maps")
    void shouldHandleDeeplyNestedMaps() {
        Map<String, Object> data = Map.of(
                "level1", Map.of(
                        "level2", Map.of(
                                "level3", "deep value"
                        )
                )
        );
        Map<String, String> rules = Map.of(
                "level1.level2.level3", "required|min:3"
        );

        ValidationResponse response = Validator.validateMap(data, rules);
        assertThat(response.valid()).isTrue();
    }

    @Test
    @DisplayName("should return null when intermediate is not a map")
    void shouldReturnNullWhenIntermediateNotMap() {
        Map<String, Object> data = Map.of("name", "John");
        Map<String, String> rules = Map.of(
                "name.nested", "required"
        );

        ValidationResponse response = Validator.validateMap(data, rules);
        assertThat(response.valid()).isFalse();
        assertThat(response.errors())
                .anyMatch(e -> e.field().equals("name.nested") && e.rules().contains("required"));
    }
}

@Nested
@DisplayName("Edge Cases")
class EdgeCaseTests {

    @Test
    @DisplayName("should handle null data map")
    void shouldHandleNullDataMap() {
        ValidationResponse response = Validator.validateMap(null, Map.of("x", "required"));
        assertThat(response.valid()).isFalse();
        assertThat(response.errors().get(0).field()).isEqualTo("data");
    }

    @Test
    @DisplayName("should pass with null rules")
    void shouldPassWithNullRules() {
        ValidationResponse response = Validator.validateMap(Map.of("x", "y"), null);
        assertThat(response.valid()).isTrue();
    }

    @Test
    @DisplayName("should pass with empty rules")
    void shouldPassWithEmptyRules() {
        ValidationResponse response = Validator.validateMap(Map.of("x", "y"), Map.of());
        assertThat(response.valid()).isTrue();
    }

    @Test
    @DisplayName("should handle missing keys as null values")
    void shouldHandleMissingKeysAsNull() {
        Map<String, String> data = Map.of("title", "Hello");
        Map<String, String> rules = Map.of(
                "title", "required",
                "missing_field", "required"
        );

        ValidationResponse response = Validator.validateMap(data, rules);
        assertThat(response.valid()).isFalse();
        assertThat(response.errors()).hasSize(1);
        assertThat(response.errors().get(0).field()).isEqualTo("missing_field");
    }

    @Test
    @DisplayName("should skip blank rule keys")
    void shouldSkipBlankRuleKeys() {
        HashMap<String, String> rules = new HashMap<>();
        rules.put("", "required");
        rules.put("title", "required");

        ValidationResponse response = Validator.validateMap(Map.of("title", "Hi"), rules);
        assertThat(response.valid()).isTrue();
    }

    @Test
    @DisplayName("validateMapOrThrow should throw on failure")
    void validateMapOrThrowShouldThrow() {
        Map<String, String> data = Map.of();
        Map<String, String> rules = Map.of("title", "required");

        org.assertj.core.api.Assertions.assertThatThrownBy(
                () -> Validator.validateMapOrThrow(data, rules)
        ).isInstanceOf(io.github.emmajiugo.javalidator.exception.NotValidException.class);
    }

    @Test
    @DisplayName("validateMapOrThrow should not throw on success")
    void validateMapOrThrowShouldNotThrow() {
        Map<String, String> data = Map.of("title", "Hello");
        Map<String, String> rules = Map.of("title", "required");

        // Should not throw
        Validator.validateMapOrThrow(data, rules);
    }

    @Test
    @DisplayName("should support bail rule in map validation")
    void shouldSupportBailRule() {
        Map<String, String> data = Map.of("email", "");
        Map<String, String> rules = Map.of("email", "bail|required|email");

        ValidationResponse response = Validator.validateMap(data, rules);
        assertThat(response.valid()).isFalse();
        // Bail should stop after required fails
        assertThat(response.errors().get(0).messages()).hasSize(1);
    }

    @Test
    @DisplayName("should populate rules field in errors")
    void shouldPopulateRulesField() {
        Map<String, String> data = Map.of("name", "A");
        Map<String, String> rules = Map.of("name", "required|min:3");

        ValidationResponse response = Validator.validateMap(data, rules);
        assertThat(response.valid()).isFalse();
        assertThat(response.errors().get(0).rules()).contains("min");
    }
}
```

- [ ] **Step 2: Run tests to verify they pass**

Run: `mvn -pl javalidator-core test -Dtest="MapValidationTest"`

Expected: All tests pass (implementation from Task 1 handles all these cases).

- [ ] **Step 3: Run full test suite**

Run: `mvn -pl javalidator-core test`

Expected: All 232+ tests pass.

- [ ] **Step 4: Commit**

```
test: add nested map and edge case tests for validateMap

Covers dot-notation nesting, null/empty data and rules, missing keys,
blank rule keys, validateMapOrThrow, bail support, and rules field.
```

---

## Task 3: Documentation + Final Verification

**Files:**
- Modify: `docs/supported-rules.md`
- Modify: `README.md`

- [ ] **Step 1: Add Map validation section to supported-rules.md**

Add before the "Null Value Handling" section:

```markdown
### Map Validation

Validate `Map<String, ?>` entries programmatically with dot-notation for nested keys:

\```java
Map<String, Object> data = Map.of(
    "title", "My Event",
    "address", Map.of("city", "Lagos", "zip", "100001")
);

Validator.validateMapOrThrow(data, Map.of(
    "title", "required|min:3",
    "address.city", "required",
    "address.zip", "required|digits:6"
));
\```

All built-in rules work with map validation. Conditional rules (`required_if`, `same`, etc.) are not supported — use object validation for those.
```

- [ ] **Step 2: Add Map validation mention to README.md**

Add to the Features list:

```markdown
- ✅ **Map Validation** - Validate `Map<String, ?>` entries with dot-notation nested key support
```

- [ ] **Step 3: Run full project build**

Run: `mvn clean test`

Expected: All tests pass, BUILD SUCCESS.

- [ ] **Step 4: Commit**

```
docs: add map validation to supported rules and README
```
