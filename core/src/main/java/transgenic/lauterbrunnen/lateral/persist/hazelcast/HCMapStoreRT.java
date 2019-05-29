package transgenic.lauterbrunnen.lateral.persist.hazelcast;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MapLoader;
import com.hazelcast.core.MapLoaderLifecycleSupport;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import transgenic.lauterbrunnen.lateral.persist.Retriever;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;

/**
 * Created by stumeikle on 29/11/16.
 */
public class HCMapStoreRT implements MapLoader<Object,Object>, MapLoaderLifecycleSupport {

    private static final Log LOG = LogFactory.getLog(HCMapStoreRT.class);
    private Retriever retriever;

    public HCMapStoreRT(Retriever retriever) {
        this.retriever= retriever;
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
    public void init(HazelcastInstance hazelcastInstance, Properties properties, String mapName) {
//        long    nextUpdateId = retriever.getLastUpdateId() + 1;
//        TODO fix. bad code. string suffix should live in repo manager at least
//        hazelcastInstance.getIdGenerator(mapName + "UpdateIdGen").init(nextUpdateId);
//        LOG.debug("Set next update id to " + nextUpdateId + " for map " + mapName);
    }

    @Override
    public void destroy() {

    }
}
