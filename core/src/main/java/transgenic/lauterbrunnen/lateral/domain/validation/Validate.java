package transgenic.lauterbrunnen.lateral.domain.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by stumeikle on 22/11/18.
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Validate {

    Class<? extends Validator> validatorClass() default DefaultValidator.class;
}
