package transgenic.lauterbrunnen.lateral.domain;

import java.util.Collection;

/**
 * Created by stumeikle on 12/08/19.
 */
public interface Repository {

    CRUDRepository getRepositoryForClass(Class clazz);

    void persist(Object entity) throws PersistenceException;
    void update(Object entity) throws PersistenceException;
    <T> T retrieve(Class<? extends T> clazz, Object id);
    <T> void delete(Class<? extends T> clazz, Object id);
    <T> Collection<T> search(Class<? extends T> clazz, String predicate);
    <I> Collection<I> retrieveKeys(Class<?> repositoryClass);

}
