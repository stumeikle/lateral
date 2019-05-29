package transgenic.lauterbrunnen.lateral.cache.hazelcast;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import transgenic.lauterbrunnen.lateral.plugin.LateralPlugin;
import transgenic.lauterbrunnen.lateral.plugin.LateralPluginParameters;

import java.util.Properties;

import static transgenic.lauterbrunnen.lateral.di.ApplicationDI.inject;

/**
 * Created by stumeikle on 13/05/16.
 */
@LateralPluginParameters(configName = "hazelcast_cache", groups = "cache_provider" )
public class HazelcastPlugin implements LateralPlugin {

    private static final Log LOG = LogFactory.getLog(HazelcastPlugin.class);

    public void initialise(Properties properties) {

        LOG.info("Initialising hazelcast plugin...");

        HazelcastInstance hazel=null;

        // NOTE: configure the map store if needed
        //(no!) for hazelcast the mapstores can only be configured for the
        //server not the client, so put the code there

        ClientConfig clientConfig = new ClientConfig();
        hazel = HazelcastClient.newHazelcastClient(clientConfig);

        //purely to prevent builds failing with no generated code, use the annotation scanner to
        //retrieve the repository manager
        HCRepositoryManager manager = inject(HCRepositoryManager.class);
        manager.initRepositories(hazel);
        manager.initTopics(hazel);
    }
}
