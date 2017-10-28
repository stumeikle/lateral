package transgenic.lauterbrunnen.lateral.domain;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by stumeikle on 02/12/16.
 *
 * This is used internally in lateral
 * it indicates that a field should not be persisted to the store but
 * may be present in the cache.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Transient {
}
