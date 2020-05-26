package transgenic.lauterbrunnen.lateral.example.multidomain.shipping.logic;

import org.apache.log4j.BasicConfigurator;
import org.junit.BeforeClass;
import org.junit.Test;
import transgenic.lauterbrunnen.lateral.Lateral;
import transgenic.lauterbrunnen.lateral.domain.Factory;
import transgenic.lauterbrunnen.lateral.domain.PersistenceException;
import transgenic.lauterbrunnen.lateral.domain.Repository;
import transgenic.lauterbrunnen.lateral.domain.UniqueId;
import transgenic.lauterbrunnen.lateral.example.multidomain.shipping.generated.Address;
import transgenic.lauterbrunnen.lateral.example.multidomain.shipping.generated.Shipment;

import static transgenic.lauterbrunnen.lateral.Lateral.inject;

/**
 * Created by stumeikle on 21/02/20.
 *
 * Idea here is to test the business logic with out having to worry about the persistence layer at all
 * So we could mock the whole factory and repository. Or we could use a simple in memory cache.
 *
 */
public class BasicTest {

    private static Factory     factory;
    private static Repository repository;

    @BeforeClass
    public static void beforeClass() {
        BasicConfigurator.configure();
        Lateral.INSTANCE.initialise();
        factory = inject(Factory.class);
        repository = inject(Repository.class);
    }

    @Test
    public void test1() {

        ShippingManager shippingManager = new ShippingManager();

        Address address = factory.create(Address.class);
        address.setHouseNameNumber("29");
        address.setStreet("Acacia Road");
        address.setCity("Fyffesville");
        //address not explicitly persisted here

        Shipment shipment = null;
        try {
            shipment = shippingManager.createNewShipment(address, UniqueId.generate(), UniqueId.generate());

            //check that everything was saved to the cache
            Address retrievedAddress = (Address) repository.getRepositoryForClass(Address.class).retrieve(address.getId());
            assert(retrievedAddress!=null);

        } catch (PersistenceException e) {
            e.printStackTrace();
            assert(false);
        }

        //check the value of shipping
        //
//        System.out.println("Shipment =" +shipment.toString());
    }

    @Test
    public void test2() {

    }
}
