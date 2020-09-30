import org.apache.log4j.BasicConfigurator;
import transgenic.lauterbrunnen.lateral.Lateral;
import transgenic.lauterbrunnen.lateral.di.ApplicationCDI;
import transgenic.lauterbrunnen.lateral.di.DIContext;
import transgenic.lauterbrunnen.lateral.di.LateralDIContext;

import java.util.*;

/**
 * Created by stumeikle on 26/05/20.
 */
public class Server {

    public Server() {
        BasicConfigurator.configure();
        Lateral.INSTANCE.initialise();

        System.out.println(Lateral.INSTANCE.getApplicationCDI().dumpInjectionBindings());
    }

    public static void main(String[] args) {
        new Server();
    }
}
