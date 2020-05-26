package ${package};

import org.apache.log4j.BasicConfigurator;
import transgenic.lauterbrunnen.lateral.Lateral;

/**
 * Created by stumeikle on 26/05/20.
 */
public class Server {

    public Server() {
        BasicConfigurator.configure();
        Lateral.INSTANCE.initialise();
    }

    public static void main(String[] args) {
        new Server();
    }
}
