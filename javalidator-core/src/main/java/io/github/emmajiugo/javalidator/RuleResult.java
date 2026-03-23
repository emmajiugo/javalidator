package io.github.emmajiugo.javalidator;

/**
 * Internal record carrying a rule name alongside its error message
 * through the validation pipeline.
 *
 * @param ruleName     the name of the rule that was evaluated
 * @param errorMessage the error message produced, or null if validation passed
 */
record RuleResult(String ruleName, String errorMessage) {
}
