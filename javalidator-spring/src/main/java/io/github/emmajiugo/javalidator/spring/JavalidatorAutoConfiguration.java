package io.github.emmajiugo.javalidator.spring;

import io.github.emmajiugo.javalidator.RuleRegistry;
import io.github.emmajiugo.javalidator.ValidationRule;
import io.github.emmajiugo.javalidator.Validator;
import io.github.emmajiugo.javalidator.config.ValidationConfig;
import io.github.emmajiugo.javalidator.spring.aspect.ValidationAspect;
import io.github.emmajiugo.javalidator.spring.customizer.JavalidatorCustomizer;
import io.github.emmajiugo.javalidator.spring.handler.GlobalExceptionHandler;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Spring Boot auto-configuration for Javalidator.
 *
 * <p>This configuration automatically sets up:
 * <ul>
 *   <li>ValidationConfig based on application properties</li>
 *   <li>ValidationAspect for AOP-based validation (if AOP is on classpath)</li>
 *   <li>GlobalExceptionHandler for converting ValidationException to HTTP 400</li>
 *   <li>Automatic registration of custom ValidationRule beans</li>
 * </ul>
 *
 * <p>The auto-configuration can be disabled with:
 * <pre>
 * javalidator.enabled=false
 * </pre>
 */
@AutoConfiguration
@ConditionalOnClass(Validator.class)
@ConditionalOnProperty(prefix = "javalidator", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(JavalidatorProperties.class)
public class JavalidatorAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(JavalidatorAutoConfiguration.class);

    private final JavalidatorProperties properties;
    private final ObjectProvider<List<JavalidatorCustomizer>> customizers;
    private final ObjectProvider<List<ValidationRule>> customRules;

    public JavalidatorAutoConfiguration(
            JavalidatorProperties properties,
            ObjectProvider<List<JavalidatorCustomizer>> customizers,
            ObjectProvider<List<ValidationRule>> customRules
    ) {
        this.properties = properties;
        this.customizers = customizers;
        this.customRules = customRules;
    }

    /**
     * Initializes the Validator with configuration from properties
     * and registers any custom ValidationRule beans.
     */
    @PostConstruct
    public void initialize() {
        // Build configuration from properties
        ValidationConfig.Builder configBuilder = ValidationConfig.builder()
                .maxClassHierarchyDepth(properties.getMaxClassHierarchyDepth())
                .validateFieldNames(properties.isValidateFieldNames())
                .fieldNamePattern(properties.getFieldNamePattern());

        // Apply customizers
        customizers.ifAvailable(customizerList -> {
            for (JavalidatorCustomizer customizer : customizerList) {
                customizer.customize(configBuilder);
            }
        });

        // Set the configuration
        ValidationConfig config = configBuilder.build();
        Validator.setConfig(config);

        logger.info("Javalidator configured with maxClassHierarchyDepth={}, validateFieldNames={}",
                config.getMaxClassHierarchyDepth(), config.isValidateFieldNames());

        // Register custom rules
        customRules.ifAvailable(rules -> {
            for (ValidationRule rule : rules) {
                RuleRegistry.register(rule);
                logger.debug("Registered custom validation rule: {}", rule.getName());
            }
            if (!rules.isEmpty()) {
                logger.info("Registered {} custom validation rule(s)", rules.size());
            }
        });
    }

    /**
     * Creates the ValidationConfig bean.
     */
    @Bean
    @ConditionalOnMissingBean
    public ValidationConfig validationConfig() {
        return Validator.getConfig();
    }

    /**
     * AOP-based validation configuration.
     * Only activated when Spring AOP is on the classpath and web application is present.
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(name = "org.aspectj.lang.annotation.Aspect")
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @ConditionalOnProperty(prefix = "javalidator.aspect", name = "enabled", havingValue = "true", matchIfMissing = true)
    static class AspectConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public ValidationAspect validationAspect(JavalidatorProperties properties) {
            return new ValidationAspect(properties);
        }
    }

    /**
     * Exception handler configuration.
     * Only activated for servlet web applications.
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @ConditionalOnProperty(prefix = "javalidator.exception-handler", name = "enabled", havingValue = "true", matchIfMissing = true)
    static class ExceptionHandlerConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public GlobalExceptionHandler javalidatorExceptionHandler(JavalidatorProperties properties) {
            return new GlobalExceptionHandler(properties);
        }
    }
}