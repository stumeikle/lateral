package transgenic.lauterbrunnen.lateral.cache.hazelcast;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import transgenic.lauterbrunnen.lateral.plugin.ApplicationPlugin;
import transgenic.lauterbrunnen.lateral.plugin.ApplicationPluginParameters;

import java.util.Properties;

import static transgenic.lauterbrunnen.lateral.di.ApplicationDI.inject;

/**
 * Created by stumeikle on 21/06/16.
 */

@ApplicationPluginParameters(configName = "hazelcast_embedded_server", groups = "hazelcast_server" )
public class HazelcastEmbedded implements ApplicationPlugin {

    private static final Log LOG = LogFactory.getLog(HazelcastEmbedded.class);

    public void initialise(Properties properties) {

        LOG.info("Initialising hazelcast embedded server plugin...");

        Config cfg = new Config();
        HazelcastInstance instance = Hazelcast.newHazelcastInstance(cfg);

    }
}

