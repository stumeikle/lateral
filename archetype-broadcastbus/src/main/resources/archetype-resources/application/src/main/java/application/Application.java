package ${package}.application;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.BasicConfigurator;
import transgenic.lauterbrunnen.lateral.Lateral;
import transgenic.lauterbrunnen.lateral.domain.Factory;
import transgenic.lauterbrunnen.lateral.domain.Repository;

public class Application {

    private static final Log LOG = LogFactory.getLog(Application.class);

    public static void main(String[] args) {

        BasicConfigurator.configure();
        Lateral.INSTANCE.initialise();

    }
}
