package transgenic.lauterbrunnen.lateral.example.cassandra.rest.generated;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import transgenic.lauterbrunnen.lateral.domain.PersistenceException;
import transgenic.lauterbrunnen.lateral.domain.Repository;
import transgenic.lauterbrunnen.lateral.example.cassandra.generated.Food;
import transgenic.lauterbrunnen.lateral.example.cassandra.generated.FoodImpl;
import transgenic.lauterbrunnen.lateral.example.cassandra.generated.restauranttableorderContext;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static transgenic.lauterbrunnen.lateral.Lateral.inject;

/**
 * Created by stumeikle on 08/11/16.
 * To be generated
 *
 * CRUD operations. perhaps Search to come later
 */
@Path("/api/foods")
public class FoodEndpointPatch {

    private static final Log LOG = LogFactory.getLog(FoodEndpointPatch.class);
    private Repository repository = inject(Repository.class, restauranttableorderContext.class);

    //------------------------------------------
    //
    // CREATE
    //
    //------------------------------------------
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({"boss","admin","manager"})
    public Response createFoodJSON(Food_1_0 food) {

        //persist the object
        try {
            FoodImpl impl = food.createImpl();
            repository.persist(impl);
            String result = impl.getRepositoryId().toString();
            return Response.status(201).entity(result).build();
        } catch(PersistenceException  exception) {
            String result = "fail:" + exception.getMessage();

            //really we should check if its a user fail or a server fail
            return Response.status(500).entity(result).build();
        }
    }

    //------------------------------------------
    //
    // READ
    //
    //------------------------------------------
    @GET
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    public Food_1_0 retrieveFoodByIdJSON(@PathParam("id") transgenic.lauterbrunnen.lateral.domain.UniqueId repositoryId) {
        Food retval = repository.retrieve(Food.class, repositoryId);

        if (retval!=null) return Food_1_0.createFromFood(retval);

        throw new NotFoundException();
    }

    //------------------------------------------
    //
    // UPDATE
    // TODO SHOULD THIS BE PUT / PATCH?
    //------------------------------------------
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({"boss","admin","manager"})
    public Response updateFoodJSON(Food_1_0 food) {

        //Really we should
        //(1) retrieve the object from the cache
        //(2) update only the fields which are sent in by the client
        //(3) update the cache with the new values
        try {
            Food found = repository.retrieve(Food.class, transgenic.lauterbrunnen.lateral.domain.UniqueId.fromString( food.getRepositoryId() ) );
            if (found!=null) {
                if (found instanceof FoodImpl) {
                    FoodImpl FoodImpl = (FoodImpl)found;
                    food.updateImpl(FoodImpl);
                    repository.update(FoodImpl);
                    String result = "success";
                    return Response.status(201).entity(result).build();
                }
            }

            String result = "failed to find object (" + food.getRepositoryId() + ") to update";
            return Response.status(500).entity(result).build();
        } catch(PersistenceException  exception) {
            String result = "fail:" + exception.getMessage();

            //really we should check if its a user fail or a server fail
            return Response.status(500).entity(result).build();
        }
    }

    
    //------------------------------------------
    //
    // DELETE
    //
    //------------------------------------------
    @DELETE
    @Path("{id}")
    public Response deleteFood(@PathParam("id") String repositoryId) {

        //why no exceptions here?
        repository.delete(Food.class, repositoryId);
        String result ="success";
        return Response.status(202).entity(result).build();
    }

    //----- my extra methods -------------------
    public void method1(double door) {
        //lkjlkjs
    }
}
