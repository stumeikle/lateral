package transgenic.lauterbrunnen.lateral.domain;

import java.util.Collection;

/**
 * Created by Stuart.meikle on 05/05/2016.
 */
public interface CRUDRepository<T,I> {

    //create / persist must set the flag on the impl if successful

    void create(T entity);
    void persist(T entity); //Could return a reference perhaps
    T retrieve(I id);
    void delete(I id);
    void update(T entity);

    Collection<T> retrieveAll();
    void persistAll(Collection<EntityImpl> persistCollection);
    void updateAll(Collection<EntityImpl> updateCollection);
}
