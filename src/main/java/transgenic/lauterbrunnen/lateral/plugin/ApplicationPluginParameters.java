package transgenic.lauterbrunnen.lateral.plugin;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by Stuart.meikle on 05/05/2016.
 */
@Target({ TYPE })
@Retention(RUNTIME)
public @interface ApplicationPluginParameters {
    boolean enabledByDefault() default false;
    String configName() default "";
    String groups() default "";
}

