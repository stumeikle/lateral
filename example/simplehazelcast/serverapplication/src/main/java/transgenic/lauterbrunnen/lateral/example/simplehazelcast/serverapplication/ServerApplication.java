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

    public ServerApplication() {
        BasicConfigurator.configure();
        Lateral.INSTANCE.initialise();
    }


    public static void main(String[] args) {
        new ServerApplication();
    }
}
