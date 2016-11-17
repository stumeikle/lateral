package transgenic.lauterbrunnen.lateral.example.microservice.serverapplication;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.BasicConfigurator;
import transgenic.lauterbrunnen.lateral.Lateral;
import transgenic.lauterbrunnen.lateral.domain.Factory;
import transgenic.lauterbrunnen.lateral.domain.PersistenceException;
import transgenic.lauterbrunnen.lateral.domain.Repository;

/**
 * Created by stumeikle on 03/11/16.
 *
 * TODO:
 * ensure the rest JSON and XML flags are honored
 * ensure that lists and maps are correct supported in the entity generation
 * ensure that plural names are used in the path structure
 *
 * the rest concept wrt References needs to be rethought. why return whole object graphs on
 * a single retrieve,that makes no sense. should be using references and give the user the option
 * to descend or pull all. i suppose in general smaller bits is better
 *
 * could be clever about it. could say if the object is a reference set the id else set all
 * the fields. that would allow us to defer object structure to the cache layer and prevent us
 * duplicating the logic
 */
public class ServerApplication {

    private static final Log LOG = LogFactory.getLog(ServerApplication.class);

    public static void main(String[] args) {

        BasicConfigurator.configure();

        //(1) start the server
        //    .. done by the plugins

        Lateral.INSTANCE.initialise();
    }
}
