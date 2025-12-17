package io.github.emmajiugo.javalidator.spring;

import io.github.emmajiugo.javalidator.RuleRegistry;
import io.github.emmajiugo.javalidator.ValidationRule;
import io.github.emmajiugo.javalidator.Validator;
import io.github.emmajiugo.javalidator.spring.aspect.ValidationAspect;
import io.github.emmajiugo.javalidator.spring.customizer.JavalidatorCustomizer;
import io.github.emmajiugo.javalidator.spring.handler.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class JavalidatorAutoConfigurationTest {

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(JavalidatorAutoConfiguration.class));

    @Test
    void shouldAutoConfigureWithDefaults() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(ValidationAspect.class);
            assertThat(context).hasSingleBean(GlobalExceptionHandler.class);
            assertThat(Validator.getConfig().getMaxClassHierarchyDepth()).isEqualTo(10);
        });
    }

    @Test
    void shouldDisableWhenPropertySetToFalse() {
        contextRunner
                .withPropertyValues("javalidator.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(ValidationAspect.class);
                    assertThat(context).doesNotHaveBean(GlobalExceptionHandler.class);
                });
    }

    @Test
    void shouldApplyCustomProperties() {
        contextRunner
                .withPropertyValues(
                        "javalidator.max-class-hierarchy-depth=20",
                        "javalidator.validate-field-names=false"
                )
                .run(context -> {
                    assertThat(Validator.getConfig().getMaxClassHierarchyDepth()).isEqualTo(20);
                    assertThat(Validator.getConfig().isValidateFieldNames()).isFalse();
                });
    }

    @Test
    void shouldDisableAspectWhenPropertySetToFalse() {
        contextRunner
                .withPropertyValues("javalidator.aspect.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(ValidationAspect.class);
                    assertThat(context).hasSingleBean(GlobalExceptionHandler.class);
                });
    }

    @Test
    void shouldDisableExceptionHandlerWhenPropertySetToFalse() {
        contextRunner
                .withPropertyValues("javalidator.exception-handler.enabled=false")
                .run(context -> {
                    assertThat(context).hasSingleBean(ValidationAspect.class);
                    assertThat(context).doesNotHaveBean(GlobalExceptionHandler.class);
                });
    }

    @Test
    void shouldApplyCustomizers() {
        contextRunner
                .withUserConfiguration(CustomizerConfiguration.class)
                .run(context -> {
                    assertThat(Validator.getConfig().getMaxClassHierarchyDepth()).isEqualTo(25);
                });
    }

    @Test
    void shouldRegisterCustomRules() {
        contextRunner
                .withUserConfiguration(CustomRuleConfiguration.class)
                .run(context -> {
                    assertThat(RuleRegistry.hasRule("customtestrule")).isTrue();
                });
    }

    @Configuration
    static class CustomizerConfiguration {
        @Bean
        JavalidatorCustomizer customizer() {
            return builder -> builder.maxClassHierarchyDepth(25);
        }
    }

    @Configuration
    static class CustomRuleConfiguration {
        @Bean
        ValidationRule customTestRule() {
            return new ValidationRule() {
                @Override
                public String validate(String fieldName, Object value, String parameter) {
                    return null;
                }

                @Override
                public String getName() {
                    return "customtestrule";
                }
            };
        }
    }
}