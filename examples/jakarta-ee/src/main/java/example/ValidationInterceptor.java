package example;

import io.github.emmajiugo.javalidator.exception.NotValidException;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import io.github.emmajiugo.javalidator.Validator;
import io.github.emmajiugo.javalidator.annotations.Valid;
import io.github.emmajiugo.javalidator.model.ValidationResponse;

import java.lang.reflect.Parameter;

/**
 * CDI Interceptor that automatically validates method parameters
 * annotated with @Valid.
 */
@Interceptor
@ValidateBinding
public class ValidationInterceptor {

    @AroundInvoke
    public Object validateParameters(InvocationContext context) throws Exception {
        Parameter[] parameters = context.getMethod().getParameters();
        Object[] args = context.getParameters();

        for (int i = 0; i < parameters.length; i++) {
            if (hasValidAnnotation(parameters[i])) {
                Object arg = args[i];
                if (arg != null) {
                    ValidationResponse response = Validator.validate(arg);
                    if (!response.valid()) {
                        throw new NotValidException(response.errors());
                    }
                }
            }
        }

        return context.proceed();
    }

    private boolean hasValidAnnotation(Parameter parameter) {
        return parameter.isAnnotationPresent(Valid.class);
    }
}