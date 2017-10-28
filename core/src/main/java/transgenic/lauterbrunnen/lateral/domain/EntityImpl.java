package transgenic.lauterbrunnen.lateral.domain;

import java.util.Collection;

/**
 * Created by stumeikle on 31/05/16.
 */
public interface EntityImpl {

    void traverse(EntityTraversalFunction entityTraversalFunction, Collection<EntityImpl> persistCollection) throws PersistenceException;
    Object getRepositoryId();
//    boolean hasBeenPersisted();
//    void setHasBeenPersisted(boolean hasBeenPersisted);
    EntityReference getReference();
    long getUpdateId();
    boolean loadedFromStore();
    void setLoadedFromStore(boolean loadedFromStore);
}
