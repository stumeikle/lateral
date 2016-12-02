package transgenic.lauterbrunnen.lateral.example.simplehazelcast.serverapplication;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.BasicConfigurator;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import transgenic.lauterbrunnen.lateral.Lateral;
import org.glassfish.grizzly.http.server.HttpServer;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Properties;

/**
 * Created by stumeikle on 21/06/16.
 */
public class ServerApplication {

    private static final Log LOG = LogFactory.getLog(ServerApplication.class);
    private HttpServer server = null;
    private final Properties properties;
    private static ResourceConfig resourceConfig=null;

    public ServerApplication() {
        BasicConfigurator.configure();
        Lateral.INSTANCE.initialise();

        //get the properties from lateral
        properties = Lateral.INSTANCE.getProperties();

        startup();
    }

    public static void setResourceConfig(ResourceConfig resourceConfig) {
        ServerApplication.resourceConfig= resourceConfig;
    }

    private void startup() {
        int port =  Integer.parseInt(properties.getProperty("http.server.port"));
        URI uri =  UriBuilder.fromUri("http://localhost/").port(port).build();
        try {
            LOG.info("Starting server...");
            server = GrizzlyHttpServerFactory.createHttpServer(uri, resourceConfig);
            LOG.info("started");
            LOG.info("Press any key to stop the server...");
            System.in.read();
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    private void shutdown() {
        LOG.info("Shutting down...");
        if (server!=null) {
            server.stop();
        }
        System.exit(0);
    }

    public static void main(String[] args) {
        new ServerApplication();
    }
}
