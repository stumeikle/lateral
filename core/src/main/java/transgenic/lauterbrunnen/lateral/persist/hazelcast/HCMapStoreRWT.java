package transgenic.lauterbrunnen.lateral.persist.hazelcast;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.MapLoaderLifecycleSupport;
import com.hazelcast.map.MapStore;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import transgenic.lauterbrunnen.lateral.persist.Persister;
import transgenic.lauterbrunnen.lateral.persist.Retriever;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;

/**
 * Created by stumeikle on 29/11/16.
 */
public class HCMapStoreRWT implements MapStore<Object,Object>, MapLoaderLifecycleSupport {

    private static final Log LOG = LogFactory.getLog(HCMapStoreRWT.class);
    private Retriever retriever;
    private Persister persister;

    public HCMapStoreRWT(Retriever retriever, Persister persister) {
        this.retriever= retriever;
        this.persister= persister;
    }

    @Override
    public Object load(Object o) {
        return retriever.load(o);
    }

    @Override
    public Map<Object, Object> loadAll(Collection<Object> collection) {
        return retriever.loadAll(collection);
    }

    @Override
    public Iterable<Object> loadAllKeys() {
        return retriever.loadAllKeys();
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

    @Override
    public void init(HazelcastInstance hazelcastInstance, Properties properties, String mapName) {

        //With hazelcast 4 this is not supported nor needed
//        long    nextUpdateId = retriever.getLastUpdateId() + 1;
//        hazelcastInstance.getFlakeIdGenerator(mapName + "UpdateIdGen").init(nextUpdateId);
//        LOG.debug("Set next update id to " + nextUpdateId + " for map " + mapName);
    }

    @Override
    public void destroy() {

    }
}
