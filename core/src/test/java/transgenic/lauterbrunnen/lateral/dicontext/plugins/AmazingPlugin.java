package transgenic.lauterbrunnen.lateral.dicontext.plugins;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import transgenic.lauterbrunnen.lateral.di.LateralDIContext;
import transgenic.lauterbrunnen.lateral.plugin.LateralPlugin;
import transgenic.lauterbrunnen.lateral.plugin.LateralPluginParameters;

import java.util.Properties;

/**
 * Created by stumeikle on 31/07/19.
 */
@LateralPluginParameters(configName = "amazing", oneInstancePerDIContext = true )
public class AmazingPlugin implements LateralPlugin{

    public static String    output = "";
    private static final Log LOG = LogFactory.getLog(AmazingPlugin.class);

    public void initialise(Properties properties, Class<? extends LateralDIContext> context) {

        LOG.info("Initialising Amazing Plugin...");
        String message = properties.getProperty("lateral_plugin.amazing.message");
        output+=message;
        LOG.info("Message from plugin :" + message);

    }

    public static String getOutput() {
        return output;
    }
}
