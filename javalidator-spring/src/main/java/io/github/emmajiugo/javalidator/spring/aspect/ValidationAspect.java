package io.github.emmajiugo.javalidator.spring.aspect;

import io.github.emmajiugo.javalidator.Validator;
import io.github.emmajiugo.javalidator.annotations.Rule;
import io.github.emmajiugo.javalidator.annotations.Valid;
import io.github.emmajiugo.javalidator.annotations.Validated;
import io.github.emmajiugo.javalidator.exception.ValidationException;
import io.github.emmajiugo.javalidator.model.ValidationError;
import io.github.emmajiugo.javalidator.model.ValidationResponse;
import io.github.emmajiugo.javalidator.spring.JavalidatorProperties;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

/**
 * AOP aspect that intercepts methods and automatically validates parameters.
 *
 * <p>This aspect supports:
 * <ul>
 *   <li>Parameter-level {@link Valid} annotation - cascaded validation of object fields</li>
 *   <li>Parameter-level {@link Rule} annotation - direct validation of parameter values</li>
 *   <li>Class-level {@link Validated} annotation - enables method parameter validation for
 *       {@code @Service}, {@code @Component}, and other Spring beans</li>
 *   <li>Configurable HTTP method interception via properties for {@code @RestController}</li>
 * </ul>
 *
 * <p>For {@code @RestController} classes, validation is enabled by default without requiring
 * the {@code @Validated} annotation (for backward compatibility). For other Spring beans
 * like {@code @Service} or {@code @Component}, the {@code @Validated} annotation must be
 * present at the class level to enable method parameter validation.
 *
 * <p>Example usage:
 * <pre>{@code
 * // RestController - validation enabled by default
 * @RestController
 * public class UserController {
 *     @GetMapping("/{id}")
 *     public User getUser(@PathVariable @Rule("min:1") Long id) { ... }
 *
 *     @PostMapping
 *     public User createUser(@Valid @RequestBody CreateUserDTO request) { ... }
 * }
 *
 * // Service - requires @Validated
 * @Validated
 * @Service
 * public class UserService {
 *     public User findById(@Rule("min:1") Long id) { ... }
 *     public User createUser(@Valid CreateUserRequest request) { ... }
 * }
 * }</pre>
 */
@Aspect
public class ValidationAspect {

    private static final Logger logger = LoggerFactory.getLogger(ValidationAspect.class);

    private final JavalidatorProperties properties;

    public ValidationAspect(JavalidatorProperties properties) {
        this.properties = properties;
    }

    // ========== RestController Pointcuts ==========

    @Pointcut("@within(org.springframework.web.bind.annotation.RestController)")
    public void inRestController() {}

    @Pointcut("@annotation(org.springframework.web.bind.annotation.PostMapping)")
    public void postMapping() {}

    @Pointcut("@annotation(org.springframework.web.bind.annotation.PutMapping)")
    public void putMapping() {}

    @Pointcut("@annotation(org.springframework.web.bind.annotation.PatchMapping)")
    public void patchMapping() {}

    @Pointcut("@annotation(org.springframework.web.bind.annotation.GetMapping)")
    public void getMapping() {}

    @Pointcut("@annotation(org.springframework.web.bind.annotation.DeleteMapping)")
    public void deleteMapping() {}

    @Pointcut("postMapping() || putMapping() || patchMapping()")
    public void writeOperations() {}

    // ========== @Validated Class Pointcuts ==========

    @Pointcut("@within(io.github.emmajiugo.javalidator.annotations.Validated)")
    public void inValidatedClass() {}

    // ========== Advice Methods ==========

    /**
     * Validates parameters for POST, PUT, and PATCH requests in RestControllers.
     */
    @Before("inRestController() && writeOperations()")
    public void validateWriteOperations(JoinPoint joinPoint) {
        validateMethodParameters(joinPoint);
    }

    /**
     * Validates parameters for GET requests in RestControllers if enabled.
     */
    @Before("inRestController() && getMapping()")
    public void validateGetOperations(JoinPoint joinPoint) {
        if (properties.getAspect().isValidateGetRequests()) {
            validateMethodParameters(joinPoint);
        }
    }

    /**
     * Validates parameters for DELETE requests in RestControllers if enabled.
     */
    @Before("inRestController() && deleteMapping()")
    public void validateDeleteOperations(JoinPoint joinPoint) {
        if (properties.getAspect().isValidateDeleteRequests()) {
            validateMethodParameters(joinPoint);
        }
    }

    /**
     * Validates parameters for any public method in classes annotated with @Validated.
     * This enables validation in @Service, @Component, and other Spring beans.
     */
    @Before("inValidatedClass() && execution(public * *(..))")
    public void validateInValidatedClass(JoinPoint joinPoint) {
        // Skip if target is also a RestController (handled by other advice)
        if (joinPoint.getTarget().getClass().isAnnotationPresent(
                org.springframework.web.bind.annotation.RestController.class)) {
            return;
        }

        if (properties.getAspect().isValidateServices()) {
            validateMethodParameters(joinPoint);
        }
    }

    // ========== Validation Logic ==========

    /**
     * Validates all parameters of a method based on @Valid and @Rule annotations.
     *
     * @param joinPoint the join point representing the method call
     */
    private void validateMethodParameters(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Parameter[] parameters = signature.getMethod().getParameters();
        Object[] args = joinPoint.getArgs();

        List<ValidationError> allErrors = new ArrayList<>();

        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            Object arg = args[i];
            String paramName = getParameterName(parameter, i);

            // Handle @Valid - cascaded object validation
            if (hasValidAnnotation(parameter)) {
                if (arg != null) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Validating @Valid parameter '{}' of type '{}'",
                                paramName, parameter.getType().getSimpleName());
                    }

                    ValidationResponse response = Validator.validate(arg);
                    if (!response.valid()) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("@Valid validation failed for '{}': {} errors",
                                    paramName, response.errors().size());
                        }
                        allErrors.addAll(response.errors());
                    }
                }
            }

            // Handle @Rule - direct value validation
            Rule[] rules = parameter.getAnnotationsByType(Rule.class);
            if (rules.length > 0) {
                for (Rule rule : rules) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Validating @Rule('{}') on parameter '{}'",
                                rule.value(), paramName);
                    }

                    ValidationResponse response = Validator.validateValue(arg, rule.value(), paramName);
                    if (!response.valid()) {
                        // Apply custom message if provided
                        if (!rule.message().isEmpty()) {
                            allErrors.add(new ValidationError(paramName, List.of(rule.message())));
                        } else {
                            allErrors.addAll(response.errors());
                        }

                        if (logger.isDebugEnabled()) {
                            logger.debug("@Rule validation failed for '{}': {}",
                                    paramName, response.errors());
                        }
                    }
                }
            }
        }

        if (!allErrors.isEmpty()) {
            throw new ValidationException(allErrors);
        }
    }

    /**
     * Gets the parameter name for error messages.
     *
     * @param parameter the method parameter
     * @param index the parameter index (fallback)
     * @return the parameter name
     */
    private String getParameterName(Parameter parameter, int index) {
        // Try to get actual parameter name (requires -parameters compiler flag)
        if (parameter.isNamePresent()) {
            return parameter.getName();
        }
        // Fallback to type-based name with index
        return parameter.getType().getSimpleName().toLowerCase() + index;
    }

    /**
     * Checks if a parameter has the @Valid annotation.
     */
    private boolean hasValidAnnotation(Parameter parameter) {
        return parameter.isAnnotationPresent(Valid.class);
    }
}