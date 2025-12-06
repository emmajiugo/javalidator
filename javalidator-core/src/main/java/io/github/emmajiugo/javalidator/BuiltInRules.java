package io.github.emmajiugo.javalidator;

import io.github.emmajiugo.javalidator.rules.*;

/**
 * Contains all built-in validation rules.
 * This class is responsible for registering default rules with the registry.
 */
final class BuiltInRules {

    private BuiltInRules() {
        // Private constructor
    }

    /**
     * Registers all built-in validation rules.
     */
    static void registerAll() {
        RuleRegistry.register(
                // Basic validation rules
                new RequiredRule(),
                new MinRule(),
                new MaxRule(),
                new EmailRule(),
                new NumericRule(),

                // Numeric comparison rules
                new GtRule(),
                new LtRule(),
                new GteRule(),
                new LteRule(),
                new BetweenRule(),

                // String pattern rules
                new RegexRule(),
                new AlphaRule(),
                new AlphaNumRule(),

                // Format validation rules
                new InRule(),
                new UrlRule(),
                new IpRule(),
                new UuidRule(),
                new JsonRule(),

                // Date/Time rules
                new DateRule(),
                new BeforeRule(),
                new AfterRule(),
                new FutureRule(),
                new PastRule(),

                // Special rules
                new EnumRule(),
                new SizeRule(),

                // Conditional rules
                new RequiredIfRule(),
                new RequiredUnlessRule(),

                // Field comparison rules
                new SameRule(),
                new DifferentRule(),

                // Control flow rules
                new NullableRule(),

                // String format rules
                new DigitsRule(),

                // Array/Collection rules
                new DistinctRule()
        );
    }
}