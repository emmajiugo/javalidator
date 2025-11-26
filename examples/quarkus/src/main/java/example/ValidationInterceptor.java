package example;

import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import me.emmajiugo.javalidator.Validator;
import me.emmajiugo.javalidator.annotations.Validate;
import me.emmajiugo.javalidator.exception.ValidationException;
import me.emmajiugo.javalidator.model.ValidationResponse;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;

/**
 * Quarkus CDI Interceptor that automatically validates method parameters
 * annotated with @Validate.
 */
@Interceptor
@ValidateBinding
public class ValidationInterceptor {

    @AroundInvoke
    public Object validateParameters(InvocationContext context) throws Exception {
        Parameter[] parameters = context.getMethod().getParameters();
        Object[] args = context.getParameters();

        for (int i = 0; i < parameters.length; i++) {
            if (hasValidateAnnotation(parameters[i])) {
                Object arg = args[i];
                if (arg != null) {
                    ValidationResponse response = Validator.validate(arg);
                    if (!response.valid()) {
                        throw new ValidationException(response.errors());
                    }
                }
            }
        }

        return context.proceed();
    }

    private boolean hasValidateAnnotation(Parameter parameter) {
        for (Annotation annotation : parameter.getAnnotations()) {
            if (annotation instanceof Validate) {
                return true;
            }
        }
        return false;
    }
}