package transgenic.lauterbrunnen.lateral.example.simplehazelcast.serverapplication.rest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import transgenic.lauterbrunnen.lateral.domain.Factory;
import transgenic.lauterbrunnen.lateral.domain.PersistenceException;
import transgenic.lauterbrunnen.lateral.domain.Repository;
import transgenic.lauterbrunnen.lateral.example.simplehazelcast.libdomain.generated.Address;
import transgenic.lauterbrunnen.lateral.example.simplehazelcast.libdomain.generated.ContactDetails;
import transgenic.lauterbrunnen.lateral.example.simplehazelcast.libdomain.generated.MemberOfStaff;
import transgenic.lauterbrunnen.lateral.example.simplehazelcast.libdomain.generated.Teacher;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;

/**
 * Created by Stuart.meikle on 05/05/2016.
 */
@Path("/api")
public class RestEndpoint {

    private static final Log LOG = LogFactory.getLog(RestEndpoint.class);

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String sayPlainTextHello() {
        try {

            // Your logic goes here --->
            // TODO: rewrite this to something sensible

            Teacher teacher = Factory.create(Teacher.class);
            teacher.setForename("bill");
            teacher.setSurname("amavasai");

            Address address = Factory.create(Address.class);
            address.setHouseNumberOrName("25");
            address.setRoad("Main Street");
            address.setTown("London");

            ContactDetails  cd = Factory.create(ContactDetails.class);
            cd.setAddress(address);
            teacher.setContactDetails(cd);

            Repository.persist(teacher);

            //<---

        } catch(PersistenceException pe) {}

        return "Hello from server application";

    }
}