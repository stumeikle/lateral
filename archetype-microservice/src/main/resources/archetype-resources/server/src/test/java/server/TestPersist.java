package ${package}.server;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.BasicConfigurator;
import org.junit.Test;
import transgenic.lauterbrunnen.lateral.Lateral;
import transgenic.lauterbrunnen.lateral.domain.Factory;
import transgenic.lauterbrunnen.lateral.domain.PersistenceException;
import transgenic.lauterbrunnen.lateral.domain.Repository;
import ${package}.libdomain.generated.ExampleObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class TestPersist {

    private static final Log LOG = LogFactory.getLog(TestPersist.class);

    @Test
    public void test1() {
        BasicConfigurator.configure();

        //(1) start the server
        //    .. done by the plugins

        Lateral.INSTANCE.initialise();
        LOG.info("Persisting example object");

        try {
            ExampleObject exampleObject = Factory.create(ExampleObject.class);

            exampleObject.setName("ExampleObject1");
            List<String>    addresses = new ArrayList<>();
            addresses.add("29 Acacier Road");
            exampleObject.setAddresses(addresses);
            Repository.persist(exampleObject);

        } catch (PersistenceException e) {
            LOG.error("Exception thrown.", e);
        }
    }
}
