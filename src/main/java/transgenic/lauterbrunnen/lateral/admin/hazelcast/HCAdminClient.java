package transgenic.lauterbrunnen.lateral.admin.hazelcast;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

/**
 * Created by stumeikle on 17/06/16.
 */
public class HCAdminClient {

    public void initialise() {
        HazelcastInstance hazel=null;
        ClientConfig clientConfig = new ClientConfig();
        hazel = HazelcastClient.newHazelcastClient(clientConfig);
        IMap    map = hazel.getMap("admin");
    }
}
