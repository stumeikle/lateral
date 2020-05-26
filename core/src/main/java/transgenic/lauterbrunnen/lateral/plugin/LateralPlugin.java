package transgenic.lauterbrunnen.lateral.plugin;

import transgenic.lauterbrunnen.lateral.di.LateralDIContext;

import java.util.Properties;

/**
 * Created by Stuart.meikle on 05/05/2016.
 */
public interface LateralPlugin {

    public static final String LATERAL_PLUGIN = "lateral_plugin";
    default void initialise(Properties properties, Class<? extends LateralDIContext> context) {}

}
