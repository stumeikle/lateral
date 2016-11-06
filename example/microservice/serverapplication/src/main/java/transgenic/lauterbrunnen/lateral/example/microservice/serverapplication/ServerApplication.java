package transgenic.lauterbrunnen.lateral.example.microservice.serverapplication;

import com.hazelcast.config.Config;
import com.hazelcast.config.MapStoreConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import transgenic.lauterbrunnen.lateral.Lateral;
import transgenic.lauterbrunnen.lateral.cache.hazelcast.HCRepositoryManager;
import transgenic.lauterbrunnen.lateral.domain.Factory;
import transgenic.lauterbrunnen.lateral.domain.PersistenceException;
import transgenic.lauterbrunnen.lateral.domain.Repository;
import transgenic.lauterbrunnen.lateral.example.microservice.libdomain.generated.Vehicle;

import java.util.Map;

import static transgenic.lauterbrunnen.lateral.di.ApplicationDI.inject;

/**
 * Created by stumeikle on 03/11/16.
 */
public class ServerApplication {

    private static final Log LOG = LogFactory.getLog(ServerApplication.class);

    public static void main(String[] args) {

        //configure hazelcast to use a mapstore
        //by adding entry to application.properties and creating
        //hazelcast.xml

        //HazelcastInstance hazel =

        Lateral.INSTANCE.initialise();

        //do something
        Vehicle vehicle = Factory.create(Vehicle.class);

        vehicle.setMake("toyota");
        vehicle.setModel("rav4");
        vehicle.setMileage(86400);
        vehicle.setNumDoors(5);
        vehicle.setRegistration("SO55 KZX"); // must be present

        try {
            Repository.persist(vehicle);
            System.out.println("Persisted record");
            Repository.retrieve(Vehicle.class, "SO55 KZX");//does this hit the cache -> yes
        } catch (PersistenceException e) {
            e.printStackTrace();
        }
    }
}
