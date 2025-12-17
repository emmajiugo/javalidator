package example;

import io.github.emmajiugo.javalidator.ValidationRule;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Custom validation rule that prevents use of reserved words.
 *
 * <p>This rule demonstrates how to create custom validation rules
 * that are automatically registered by the javalidator-spring starter.
 * Simply annotate your ValidationRule implementation with @Component
 * (or @Bean in a configuration class) and it will be auto-registered.
 *
 * <p>Usage: @Rule("noreservedwords") or @Rule("noreservedwords:admin,root,system")
 */
@Component
public class NoReservedWordsRule implements ValidationRule {

    private static final Set<String> DEFAULT_RESERVED_WORDS = Set.of(
            "admin", "administrator", "root", "system", "superuser", "moderator"
    );

    @Override
    public String validate(String fieldName, Object value, String parameter) {
        if (value == null) {
            return null; // Let 'required' rule handle null values
        }

        String stringValue = value.toString().toLowerCase();
        Set<String> reservedWords = getReservedWords(parameter);

        for (String reserved : reservedWords) {
            if (stringValue.contains(reserved)) {
                return "The " + fieldName + " cannot contain reserved word: " + reserved;
            }
        }

        return null;
    }

    private Set<String> getReservedWords(String parameter) {
        if (parameter == null || parameter.isBlank()) {
            return DEFAULT_RESERVED_WORDS;
        }
        // Allow custom reserved words via parameter: noreservedwords:word1,word2,word3
        return Set.of(parameter.toLowerCase().split(","));
    }

    @Override
    public String getName() {
        return "noreservedwords";
    }
}