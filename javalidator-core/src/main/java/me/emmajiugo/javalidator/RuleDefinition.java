package me.emmajiugo.javalidator;

/**
 * Represents a parsed rule definition (name and optional parameter).
 */
record RuleDefinition(String name, String parameter) {

    /**
     * Parses a rule definition string (e.g., "min:3" or "required").
     *
     * @param ruleDefinition the rule definition string
     * @return parsed RuleDefinition
     */
    static RuleDefinition parse(String ruleDefinition) {
        int colonIndex = ruleDefinition.indexOf(':');
        if (colonIndex == -1) {
            return new RuleDefinition(ruleDefinition, null);
        }
        return new RuleDefinition(
                ruleDefinition.substring(0, colonIndex),
                ruleDefinition.substring(colonIndex + 1)
        );
    }
}