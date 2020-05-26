package transgenic.lauterbrunnen.lateral.cache.hazelcast;

import com.hazelcast.map.IMap;

import java.util.Map;

/**
 * Created by stumeikle on 12/06/16.
 */
public interface HCCacheChangeManager {

    void initialise(Map<String, IMap> iMapMap);
}
