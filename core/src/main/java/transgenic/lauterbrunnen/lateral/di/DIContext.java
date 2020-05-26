package transgenic.lauterbrunnen.lateral.di;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by stumeikle on 25/07/19.
 */
@Target({ TYPE })
@Retention(RUNTIME)
public @interface DIContext {
    Class<? extends LateralDIContext> value() default LateralDIContext.class;
}
