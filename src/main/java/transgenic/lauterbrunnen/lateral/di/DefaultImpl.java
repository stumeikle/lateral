package transgenic.lauterbrunnen.lateral.di;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by stumeikle on 12/06/16.
 */
@Target({ TYPE })
@Retention(RUNTIME)
public @interface DefaultImpl {
}
