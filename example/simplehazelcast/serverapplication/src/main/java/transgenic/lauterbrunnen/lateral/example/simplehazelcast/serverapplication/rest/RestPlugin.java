package transgenic.lauterbrunnen.lateral.example.simplehazelcast.serverapplication.rest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glassfish.jersey.server.ResourceConfig;
import transgenic.lauterbrunnen.lateral.example.simplehazelcast.serverapplication.ServerApplication;
import transgenic.lauterbrunnen.lateral.plugin.LateralPlugin;
import transgenic.lauterbrunnen.lateral.plugin.LateralPluginParameters;

import java.util.Properties;

/**
 * Created by stumeikle on 21/06/16.
 */
@LateralPluginParameters(configName="rest", enabledByDefault=true)
public class RestPlugin implements LateralPlugin {

    private static final Log LOG = LogFactory.getLog(RestPlugin.class);

    public void initialise(Properties properties) {
        LOG.info("Starting REST endpoint");

        ResourceConfig config = new ResourceConfig(RestEndpoint.class);
        ServerApplication.setResourceConfig(config);
        //register resource with the server startup
    }
}
