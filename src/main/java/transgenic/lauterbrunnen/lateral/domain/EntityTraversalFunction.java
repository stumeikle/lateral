package transgenic.lauterbrunnen.lateral.domain;

import java.util.Collection;

/**
 * Created by stumeikle on 31/05/16.
 */
public interface EntityTraversalFunction {

    void action(EntityImpl entity, Object subentity, Collection<EntityImpl> persistCollection) throws PersistenceException;

}
