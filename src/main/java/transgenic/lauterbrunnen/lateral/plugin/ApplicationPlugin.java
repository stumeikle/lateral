package transgenic.lauterbrunnen.lateral.plugin;

import java.util.Properties;

/**
 * Created by Stuart.meikle on 05/05/2016.
 */
public interface ApplicationPlugin {

    public static final String APPLICATION_PLUGIN = "application_plugin";
    default void initialise(Properties properties) {}

}
