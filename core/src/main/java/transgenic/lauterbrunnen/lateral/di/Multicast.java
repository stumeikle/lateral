package transgenic.lauterbrunnen.lateral.di;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by stumeikle on 27/08/20.
 */
@Target({ TYPE })
@Retention(RUNTIME)
public @interface Multicast {
}
