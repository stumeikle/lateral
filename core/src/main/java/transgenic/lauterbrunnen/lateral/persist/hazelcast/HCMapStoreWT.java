package transgenic.lauterbrunnen.lateral.persist.hazelcast;

import com.hazelcast.map.MapStore;
import transgenic.lauterbrunnen.lateral.persist.Persister;
import transgenic.lauterbrunnen.lateral.persist.Retriever;

import java.util.Collection;
import java.util.Map;

/**
 * Created by stumeikle on 29/11/16.
 */
public class HCMapStoreWT implements MapStore<Object, Object> {
    private Persister persister;

    public HCMapStoreWT(Persister persister) {
        this.persister= persister;
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


    @Override
    public void store(Object o, Object o2) {
        persister.persist(o2); //o is the key and should be set in o2 already
    }

    @Override
    public void storeAll(Map<Object, Object> map) {
        persister.persistAll(map);
    }

    @Override
    public void delete(Object o) {
        persister.remove(o);
    }

    @Override
    public void deleteAll(Collection<Object> collection) {
        persister.removeAll(collection);
    }
}
