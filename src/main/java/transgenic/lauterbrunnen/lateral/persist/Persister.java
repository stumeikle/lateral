package transgenic.lauterbrunnen.lateral.persist;

/**
 * Created by stumeikle on 13/05/16.
 */
public interface Persister {
    void persist(Object object);
    void update(Object object);
    void remove(Object object);
}
