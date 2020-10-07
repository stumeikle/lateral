package domain;

import org.apache.log4j.BasicConfigurator;
import org.junit.Test;
import transgenic.lauterbrunnen.lateral.Lateral;
import transgenic.lauterbrunnen.lateral.domain.Factory;

import static transgenic.lauterbrunnen.lateral.Lateral.inject;

/**
 * Created by stumeikle on 02/10/20.
 */
public class TestDomainObjectCreation {

    @Test
    public void test() {

        BasicConfigurator.configure();
        Lateral.INSTANCE.initialise();

        Factory factory = inject(Factory.class);

        ExampleObject eo = factory.create(ExampleObject.class);
        eo.setDescription("My description");
    }
}
