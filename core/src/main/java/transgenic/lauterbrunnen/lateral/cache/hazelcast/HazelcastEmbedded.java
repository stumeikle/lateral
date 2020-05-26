package transgenic.lauterbrunnen.lateral.cache.hazelcast;

import com.hazelcast.config.ClasspathXmlConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MapStoreConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.flakeidgen.FlakeIdGenerator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import transgenic.lauterbrunnen.lateral.di.LateralDIContext;
import transgenic.lauterbrunnen.lateral.plugin.LateralPlugin;
import transgenic.lauterbrunnen.lateral.plugin.LateralPluginParameters;

import java.util.Map;
import java.util.Properties;

import static transgenic.lauterbrunnen.lateral.Lateral.inject;

/**
 * Created by stumeikle on 21/06/16.
 */

@LateralPluginParameters(configName = "hazelcast_embedded_server", groups = "cache_provider", oneInstancePerDIContext = true )
public class HazelcastEmbedded implements LateralPlugin {

    private static final Log LOG = LogFactory.getLog(HazelcastEmbedded.class);

    public void initialise(Properties properties, Class<? extends LateralDIContext> context) {

        LOG.info("Initialising hazelcast embedded server plugin...");

        //Server side we have more options to change default behaviour:
        //check for specified config
        String cp_fn = properties.getProperty("lateral_plugin.hazelcast_embedded_server.config_file");
        Config cfg = null;
        if (cp_fn!=null) {
            cfg= new ClasspathXmlConfig(cp_fn);
        } else {
            cfg = new Config();
        }

        boolean write_through = "true".equalsIgnoreCase(properties.getProperty("lateral_plugin.hazelcast_embedded_server.write_through.enabled"));
        boolean read_through  = "true".equalsIgnoreCase(properties.getProperty("lateral_plugin.hazelcast_embedded_server.read_through.enabled"));
        boolean write_behind  = "true".equalsIgnoreCase(properties.getProperty("lateral_plugin.hazelcast_embedded_server.write_behind.enabled"));

        if (write_through || read_through || write_behind){
            String contextPrefix = context.getSimpleName().replaceAll("Context$","") + "_";

            MapConfig mapCfg = new MapConfig();
            mapCfg.setName(contextPrefix +"*");
            cfg.addMapConfig(mapCfg);

            MapConfig mapConfig = cfg.getMapConfig(contextPrefix +"*");
            MapStoreConfig mapStoreConfig = new MapStoreConfig();
            mapConfig.setMapStoreConfig(mapStoreConfig);
            HCMapStoreFactory factory = inject(HCMapStoreFactory.class, context);
            factory.setWriteThrough(write_through|write_behind);
            factory.setReadThrough(read_through);

            //20161213 We need to store all idgenerators in the map store so that they can be
            //later initialised. However. the repository manager is *not* initialised on the hc server side
            //so we don't have access to that information here.
            //Gets really messy

            mapStoreConfig.setFactoryImplementation(factory);
            if (write_through || write_behind) {
                int delayValue = write_through ? 0 : 60; //set a default for write behind
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

        cfg.setInstanceName(context.getName());
        HazelcastInstance instance = Hazelcast.newHazelcastInstance(cfg);
        HCRepositoryManager manager = inject(HCRepositoryManager.class, context);
        manager.initRepositories(instance);

    }

}

