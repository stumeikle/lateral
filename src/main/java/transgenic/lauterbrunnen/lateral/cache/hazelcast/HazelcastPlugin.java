package transgenic.lauterbrunnen.lateral.cache.hazelcast;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import transgenic.lauterbrunnen.lateral.plugin.ApplicationPlugin;
import transgenic.lauterbrunnen.lateral.plugin.ApplicationPluginParameters;

import java.util.Properties;

import static transgenic.lauterbrunnen.lateral.di.ApplicationDI.inject;

/**
 * Created by stumeikle on 13/05/16.
 */
@ApplicationPluginParameters(configName = "hazelcast_cache", groups = "hazelcast_client, cache_provider" )
public class HazelcastPlugin implements ApplicationPlugin {

    private static final Log LOG = LogFactory.getLog(HazelcastPlugin.class);

    public void initialise(Properties properties) {

        LOG.info("Initialising hazelcast plugin...");

        HazelcastInstance hazel=null;
        ClientConfig clientConfig = new ClientConfig();
        hazel = HazelcastClient.newHazelcastClient(clientConfig);

        //purely to prevent builds failing with no generated code, use the annotation scanner to
        //retrieve the repository manager
        HCRepositoryManager manager = inject(HCRepositoryManager.class);
        manager.initRepositories(hazel);
    }
}
