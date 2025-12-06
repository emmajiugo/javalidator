package example;

import io.github.emmajiugo.javalidator.Validator;
import io.github.emmajiugo.javalidator.annotations.Validate;
import io.github.emmajiugo.javalidator.exception.ValidationException;
import io.github.emmajiugo.javalidator.model.ValidationResponse;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;

/**
 * AOP aspect that intercepts REST controller endpoint methods with @Validate annotated parameters
 * and automatically validates them using Javalidator.
 *
 * <p>This aspect only intercepts methods in @RestController classes that are annotated with
 * :@PostMapping, @PutMapping, or @PatchMapping for optimal performance.
 *
 * <p>This enables declarative validation across controller endpoints
 * without repetitive validation code.
 */
@Aspect
@Component
public class ValidationAspect {

    @Before("@within(org.springframework.web.bind.annotation.RestController) && " +
            "(@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PutMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PatchMapping))")
    public void validateParameters(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Parameter[] parameters = signature.getMethod().getParameters();
        Object[] args = joinPoint.getArgs();

        for (int i = 0; i < parameters.length; i++) {
            // Check if parameter has @Validate OR if the parameter's type has @Validate
            if (hasValidateAnnotation(parameters[i]) || hasValidateAnnotationOnType(parameters[i].getType())) {
                Object arg = args[i];
                if (arg != null) {
                    ValidationResponse response = Validator.validate(arg);
                    if (!response.valid()) {
                        throw new ValidationException(response.errors());
                    }
                }
            }
        }
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