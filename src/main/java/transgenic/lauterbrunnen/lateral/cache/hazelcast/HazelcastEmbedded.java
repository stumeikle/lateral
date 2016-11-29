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
        String cp_fn = properties.getProperty("lateral_plugin.hazelcast_embedded_server.cp_config");
        Config cfg = null;
        if (cp_fn!=null) {
            cfg= new ClasspathXmlConfig(cp_fn);
        } else {
            cfg = new Config();
        }

        boolean write_through = "true".equalsIgnoreCase(properties.getProperty("lateral_plugin.hazelcast_embedded_server.write_through.enabled"));
        boolean read_through  = "true".equalsIgnoreCase(properties.getProperty("lateral_plugin.hazelcast_embedded_server.read_through.enabled"));
        if (write_through || read_through){
            MapConfig mapConfig = cfg.getMapConfig("*");
            MapStoreConfig mapStoreConfig = mapConfig.getMapStoreConfig();
            HCMapStoreFactory factory = inject(HCMapStoreFactory.class);
            factory.setWriteThrough(write_through);
            factory.setReadThrough(read_through);
            mapStoreConfig.setFactoryImplementation(factory);
            if (write_through) {
                int delayValue = 0;
                String delay = properties.getProperty("lateral_plugin.hazelcast_embedded_server.write_delay_secs");
                if (delay!=null) {
                    try{
                        delayValue = Integer.parseInt(delay);
                    }catch(NumberFormatException e) {}
                }
                mapStoreConfig.setWriteDelaySeconds(delayValue); //this means cache writes wait on db commits (write-thru)
            }
            mapStoreConfig.setEnabled(true);
        }

        cfg.setInstanceName("lateral");
        HazelcastInstance instance = Hazelcast.newHazelcastInstance(cfg);

    }
}

