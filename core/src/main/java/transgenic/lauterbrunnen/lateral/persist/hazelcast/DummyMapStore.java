package transgenic.lauterbrunnen.lateral.persist.hazelcast;

import com.hazelcast.map.MapStore;

import java.util.Collection;
import java.util.Map;

/**
 * Created by stumeikle on 27/11/16.
 * A dummy used for maps we don't need to persist, such as admincommandqueue
 */
public class DummyMapStore implements MapStore<Object,Object> {
    @Override
    public void store(Object o, Object o2) {

    }

    @Override
    public void storeAll(Map<Object, Object> map) {

    }

    @Override
    public void delete(Object o) {

    }

    @Override
    public void deleteAll(Collection<Object> collection) {

    }

    @Override
    public Object load(Object o) {
        return null;
    }

    @Override
    public Map<Object, Object> loadAll(Collection<Object> collection) {
        return null;
    }

    @Override
    public Iterable<Object> loadAllKeys() {
        return null;
    }
}
