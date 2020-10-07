package service;

import domain.ExampleObject;
import org.apache.log4j.BasicConfigurator;
import org.junit.Test;
import transgenic.lauterbrunnen.lateral.Lateral;
import transgenic.lauterbrunnen.lateral.domain.AccessRestricted;
import transgenic.lauterbrunnen.lateral.domain.AccessViolationException;
import transgenic.lauterbrunnen.lateral.domain.Factory;

import static transgenic.lauterbrunnen.lateral.Lateral.inject;

/**
 * Created by stumeikle on 02/10/20.
 */
public class TestAccessRestriction {

    @Test
    public void test() {
        BasicConfigurator.configure();
        Lateral.INSTANCE.initialise();

        Factory factory = inject(Factory.class);

            //Object creation includes field setting from within the domain
            ExampleObject eo = factory.create(ExampleObject.class);

        try {
            //but here when we set it ourselves it should fail
            eo.setDescription("My description");
        } catch(Exception ave) {
            assert(true);
            return;
        }
        assert(false);
    }
}
