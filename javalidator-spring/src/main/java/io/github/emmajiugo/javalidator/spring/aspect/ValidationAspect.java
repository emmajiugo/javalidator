package io.github.emmajiugo.javalidator.spring.aspect;

import io.github.emmajiugo.javalidator.Validator;
import io.github.emmajiugo.javalidator.annotations.Validate;
import io.github.emmajiugo.javalidator.exception.ValidationException;
import io.github.emmajiugo.javalidator.model.ValidationResponse;
import io.github.emmajiugo.javalidator.spring.JavalidatorProperties;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;

/**
 * AOP aspect that intercepts REST controller methods and automatically
 * validates parameters annotated with {@link Validate}.
 *
 * <p>This aspect supports:
 * <ul>
 *   <li>Parameter-level @Validate annotation</li>
 *   <li>Type-level @Validate annotation (on the DTO class)</li>
 *   <li>Configurable HTTP method interception via properties</li>
 * </ul>
 *
 * <p>By default, only @PostMapping, @PutMapping, and @PatchMapping methods
 * are intercepted. This can be changed via properties.
 */
@Aspect
public class ValidationAspect {

    private static final Logger logger = LoggerFactory.getLogger(ValidationAspect.class);

    private final JavalidatorProperties properties;

    public ValidationAspect(JavalidatorProperties properties) {
        this.properties = properties;
    }

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

    /**
     * Validates parameters for POST, PUT, and PATCH requests.
     */
    @Before("inRestController() && writeOperations()")
    public void validateWriteOperations(JoinPoint joinPoint) {
        validateParameters(joinPoint);
    }

    /**
     * Validates parameters for GET requests if enabled.
     */
    @Before("inRestController() && getMapping()")
    public void validateGetOperations(JoinPoint joinPoint) {
        if (properties.getAspect().isValidateGetRequests()) {
            validateParameters(joinPoint);
        }
    }

    /**
     * Validates parameters for DELETE requests if enabled.
     */
    @Before("inRestController() && deleteMapping()")
    public void validateDeleteOperations(JoinPoint joinPoint) {
        if (properties.getAspect().isValidateDeleteRequests()) {
            validateParameters(joinPoint);
        }
    }

    private void validateParameters(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Parameter[] parameters = signature.getMethod().getParameters();
        Object[] args = joinPoint.getArgs();

        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            Object arg = args[i];

            if (shouldValidate(parameter) && arg != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Validating parameter '{}' of type '{}'",
                            parameter.getName(), parameter.getType().getSimpleName());
                }

                ValidationResponse response = Validator.validate(arg);
                if (!response.valid()) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Validation failed for '{}': {} errors",
                                parameter.getName(), response.errors().size());
                    }
                    throw new ValidationException(response.errors());
                }
            }
        }
    }

    private boolean shouldValidate(Parameter parameter) {
        return hasValidateAnnotation(parameter) || hasValidateAnnotationOnType(parameter.getType());
    }

    private boolean hasValidateAnnotation(Parameter parameter) {
        for (Annotation annotation : parameter.getAnnotations()) {
            if (annotation instanceof Validate) {
                return true;
            }
        }
        return false;
    }

    private boolean hasValidateAnnotationOnType(Class<?> type) {
        return type.isAnnotationPresent(Validate.class);
    }
}