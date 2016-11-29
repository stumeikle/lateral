package transgenic.lauterbrunnen.lateral.persist.hazelcast;

import com.hazelcast.core.MapLoader;
import transgenic.lauterbrunnen.lateral.persist.Retriever;

import java.util.Collection;
import java.util.Map;

/**
 * Created by stumeikle on 29/11/16.
 */
public class HCMapStoreRT implements MapLoader<Object,Object> {

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
}
