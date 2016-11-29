package transgenic.lauterbrunnen.lateral.persist;

import java.util.Collection;
import java.util.Map;

/**
 * Created by stumeikle on 13/05/16.
 */
public interface Persister {
    void persist(Object object);
    void persistAll(Map<Object, Object> map);
    void update(Object object);
    void remove(Object object);
    void removeAll(Collection<Object> collection);
}
