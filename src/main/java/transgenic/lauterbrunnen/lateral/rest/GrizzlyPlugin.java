package transgenic.lauterbrunnen.lateral.rest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import transgenic.lauterbrunnen.lateral.plugin.LateralPlugin;
import transgenic.lauterbrunnen.lateral.plugin.LateralPluginParameters;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Properties;

import static transgenic.lauterbrunnen.lateral.di.ApplicationDI.inject;

/**
 * Created by stumeikle on 10/11/16.
 * Start up a grizzly web server
 * nothing fancy
 */
@LateralPluginParameters(configName = "grizzly_server", groups = "web_server" )
public class GrizzlyPlugin implements LateralPlugin {

    private static final Log LOG = LogFactory.getLog(GrizzlyPlugin.class);
    private Properties properties;
    private HttpServer server = null;
    private PluggableResourceConfig restConfig=null;

    public void initialise(Properties properties) {

        this.properties = properties;

        //inject the resource config
        restConfig = inject(PluggableResourceConfig.class);

        LOG.info("Initialising simple grizzly server plugin...");
        startServer();
    }

    public void startServer() {
        int port =  Integer.parseInt(properties.getProperty("http.server.port"));
        URI uri =  UriBuilder.fromUri("http://localhost/").port(port).build();
        try {
            LOG.info("Starting server...");
            server = GrizzlyHttpServerFactory.createHttpServer(uri, restConfig.getResourceConfig());
            LOG.info("started");
        } catch (Exception e) {
            LOG.error(e);
        }
    }

    public void shutdownServer() {
    }
}
