package transgenic.lauterbrunnen.lateral.cache.hazelcast;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.flakeidgen.FlakeIdGenerator;

import java.util.Map;

/**
 * Created by stumeikle on 12/06/16.
 */
public interface HCRepositoryManager {
    void initRepositories(HazelcastInstance hazel);
    Map<String, IMap> getImapNameMap();
    Map<String, FlakeIdGenerator> getUpdateIdNameMap();
}
