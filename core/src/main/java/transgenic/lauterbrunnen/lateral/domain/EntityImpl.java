package transgenic.lauterbrunnen.lateral.domain;

import java.util.Collection;

/**
 * Created by stumeikle on 31/05/16.
 */
public interface EntityImpl {

    void traverse(EntityTraversalFunction entityTraversalFunction, Collection<EntityImpl> persistCollection) throws PersistenceException;
    Object getRepositoryId();
    EntityReference getReference();
    long getUpdateId();
}
