package io.github.emmajiugo.javalidator.spring.customizer;

import io.github.emmajiugo.javalidator.config.ValidationConfig;

/**
 * Functional interface for customizing Javalidator configuration.
 *
 * <p>Users can create beans implementing this interface to customize
 * the ValidationConfig beyond what is possible with properties.
 *
 * <p>Example usage:
 * <pre>{@code
 * @Bean
 * public JavalidatorCustomizer myCustomizer() {
 *     return builder -> builder
 *         .maxClassHierarchyDepth(15)
 *         .validateFieldNames(true);
 * }
 * }</pre>
 */
@FunctionalInterface
public interface JavalidatorCustomizer {

    /**
     * Customize the ValidationConfig builder.
     *
     * @param builder the ValidationConfig builder to customize
     */
    void customize(ValidationConfig.Builder builder);
}