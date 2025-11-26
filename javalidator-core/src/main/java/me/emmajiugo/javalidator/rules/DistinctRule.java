package me.emmajiugo.javalidator.rules;

import me.emmajiugo.javalidator.ValidationRule;

import java.lang.reflect.Array;
import java.util.*;

/**
 * Validation rule that checks if all elements in an array or collection are unique.
 *
 * <p>Usage: {@code @Rule("distinct")}
 *
 * <p>This rule validates that all elements in an array, List, Set, or other Collection
 * are distinct (no duplicates). It works with any type of element that implements equals().
 *
 * <p>Example: {@code @Rule("distinct")} ensures an array like [1, 2, 3] passes, but [1, 2, 2] fails.
 *
 * <p>Common use cases:
 * <ul>
 *   <li>List of unique IDs: {@code @Rule("distinct")}</li>
 *   <li>Array of unique tags: {@code @Rule("distinct")}</li>
 *   <li>Collection of unique emails: {@code @Rule("distinct")}</li>
 *   <li>List of unique product codes: {@code @Rule("distinct")}</li>
 * </ul>
 *
 * <p>Supported types:
 * <ul>
 *   <li>Arrays: int[], String[], Object[], etc.</li>
 *   <li>Collections: List, ArrayList, LinkedList, etc.</li>
 *   <li>Sets: HashSet, TreeSet, etc. (always pass validation)</li>
 * </ul>
 *
 * <p>Note: Empty arrays/collections and null values are considered valid.
 * Use the 'required' rule if you want to enforce non-empty collections.
 */
public class DistinctRule implements ValidationRule {

    @Override
    public String validate(String fieldName, Object value, String parameter) {
        // Skip validation if value is null (let 'required' rule handle nulls)
        if (value == null) {
            return null;
        }

        // Handle arrays
        if (value.getClass().isArray()) {
            return validateArray(fieldName, value);
        }

        // Handle collections
        if (value instanceof Collection<?>) {
            return validateCollection(fieldName, (Collection<?>) value);
        }

        // If the value is neither an array nor a collection, fail validation
        return "The " + fieldName + " must be an array or collection to use the 'distinct' rule.";
    }

    /**
     * Validates that all elements in an array are distinct.
     */
    private String validateArray(String fieldName, Object arrayValue) {
        int length = Array.getLength(arrayValue);

        // Empty arrays are considered valid
        if (length == 0) {
            return null;
        }

        // Use a set to track seen elements
        Set<Object> seen = new HashSet<>();
        Set<Object> duplicates = new HashSet<>();

        for (int i = 0; i < length; i++) {
            Object element = Array.get(arrayValue, i);
            if (!seen.add(element)) {
                duplicates.add(element);
            }
        }

        if (!duplicates.isEmpty()) {
            return "The " + fieldName + " must contain only distinct values.";
        }

        return null; // Validation passes
    }

    /**
     * Validates that all elements in a collection are distinct.
     */
    private String validateCollection(String fieldName, Collection<?> collection) {
        // Empty collections are considered valid
        if (collection.isEmpty()) {
            return null;
        }

        // Sets inherently contain unique elements
        if (collection instanceof Set) {
            return null;
        }

        // Use a set to track seen elements
        Set<Object> seen = new HashSet<>();
        Set<Object> duplicates = new HashSet<>();

        for (Object element : collection) {
            if (!seen.add(element)) {
                duplicates.add(element);
            }
        }

        if (!duplicates.isEmpty()) {
            return "The " + fieldName + " must contain only distinct values.";
        }

        return null; // Validation passes
    }

    @Override
    public String getName() {
        return "distinct";
    }
}