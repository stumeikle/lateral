package transgenic.lauterbrunnen.lateral.domain;

import java.util.Collection;
import java.util.List;

/**
 * Created by Stuart.meikle on 05/05/2016.
 */
public interface CRUDRepository<T,I> {

    //create / persist must set the flag on the impl if successful

    void create(T entity)throws PersistenceException;
    void persist(T entity)throws PersistenceException; //Could return a reference perhaps
    T retrieve(I id);
    void delete(I id);
    void update(T entity) throws PersistenceException;

    Collection<T> retrieveAll();
    void persistAll(Collection<EntityImpl> persistCollection)throws PersistenceException;
    void updateAll(Collection<EntityImpl> updateCollection) throws PersistenceException;

    Collection<T> search(String predicate);
    Collection<I> retrieveKeys();
}
