package transgenic.lauterbrunnen.lateral.cache.hazelcast;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.IdGenerator;

import java.util.Map;

/**
 * Created by stumeikle on 12/06/16.
 */
public interface HCRepositoryManager {
    void initRepositories(HazelcastInstance hazel);
    Map<String, IMap> getImapNameMap();
    Map<String, IdGenerator> getUpdateIdNameMap();
}
