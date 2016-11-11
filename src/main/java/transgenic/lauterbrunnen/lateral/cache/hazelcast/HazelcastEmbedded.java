package transgenic.lauterbrunnen.lateral.cache.hazelcast;

import com.hazelcast.config.ClasspathXmlConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MapStoreConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import transgenic.lauterbrunnen.lateral.plugin.LateralPlugin;
import transgenic.lauterbrunnen.lateral.plugin.LateralPluginParameters;

import java.util.Properties;

import static transgenic.lauterbrunnen.lateral.di.ApplicationDI.inject;

/**
 * Created by stumeikle on 21/06/16.
 */

@LateralPluginParameters(configName = "hazelcast_embedded_server", groups = "hazelcast_server" )
public class HazelcastEmbedded implements LateralPlugin {

    private static final Log LOG = LogFactory.getLog(HazelcastEmbedded.class);

    public void initialise(Properties properties) {

        LOG.info("Initialising hazelcast embedded server plugin...");

        //Server side we have more options to change default behaviour:
        //check for specified config
        String cp_fn = properties.getProperty("application_plugin.hazelcast_embedded_server.cp_config");
        Config cfg = null;
        if (cp_fn!=null) {
            cfg= new ClasspathXmlConfig(cp_fn);
        } else {
            cfg = new Config();
        }

        if ("true".equalsIgnoreCase(properties.getProperty("application_plugin.hazelcast_embedded_server.write_through.enabled"))){
            MapConfig mapConfig = cfg.getMapConfig("*");
            MapStoreConfig mapStoreConfig = mapConfig.getMapStoreConfig();
            HCMapStoreFactory factory = inject(HCMapStoreFactory.class);
            mapStoreConfig.setFactoryImplementation(factory);
            mapStoreConfig.setWriteDelaySeconds(0); //this means cache writes wait on db commits (write-thru)
            mapStoreConfig.setEnabled(true);
        }

        cfg.setInstanceName("lateral");
        HazelcastInstance instance = Hazelcast.newHazelcastInstance(cfg);

    }
}

