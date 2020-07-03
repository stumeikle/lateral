package transgenic.lauterbrunnen.lateral.persist;

import java.util.Collection;
import java.util.Map;

/**
 * Created by stumeikle on 29/11/16.
 * woof
 * maybe this will need to become HC specific at some point
 */
public interface Retriever {
    Object load(Object o);
    Map<Object, Object> loadAll(Collection<Object> collection);
    Iterable<Object> loadAllKeys();
}
