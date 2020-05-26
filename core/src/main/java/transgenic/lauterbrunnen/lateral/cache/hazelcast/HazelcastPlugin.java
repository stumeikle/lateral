package transgenic.lauterbrunnen.lateral.cache.hazelcast;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.XmlClientConfigBuilder;
import com.hazelcast.config.ClasspathXmlConfig;
import com.hazelcast.config.Config;
import com.hazelcast.core.HazelcastInstance;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import transgenic.lauterbrunnen.lateral.di.LateralDIContext;
import transgenic.lauterbrunnen.lateral.plugin.LateralPlugin;
import transgenic.lauterbrunnen.lateral.plugin.LateralPluginParameters;

import java.io.IOException;
import java.util.Properties;

import static transgenic.lauterbrunnen.lateral.Lateral.inject;

/**
 * Created by stumeikle on 13/05/16.
 *
 * Now we need to support multiple instances if there are multiple contexts
 * and that means we need to support:
 *
 *
 lateral_plugin.hazelcast_embedded_server.enabled=true
 lateral_plugin.hazelcast_embedded_server.read_through.enabled=true
 lateral_plugin.hazelcast_embedded_server.write_behind.enabled=true
 lateral_plugin.hazelcast_embedded_server.port_base=5700

 and

 lateral_plugin.dicontext.MyContext1.hazelcast_embedded_server.enabled=true
 lateral_plugin.dicontext.MyContext1.hazelcast_embedded_server.read_through.enabled=true
 lateral_plugin.dicontext.MyContext1.hazelcast_embedded_server.write_behind.enabled=true
 lateral_plugin.dicontext.MyContext1.hazelcast_embedded_server.port_base=5700

 lateral_plugin.dicontext.MyContext2.hazelcast_embedded_server.enabled=true
 lateral_plugin.dicontext.MyContext2.hazelcast_embedded_server.read_through.enabled=true
 lateral_plugin.dicontext.MyContext2.hazelcast_embedded_server.write_behind.enabled=true
 lateral_plugin.dicontext.MyContext2.hazelcast_embedded_server.port_base=5700

 IF there is only 1 context either the context name can be given or not
 the plugin shouldn't need to know about the properties differing across contexts, so these should be stripped out as
 necessary.

 Other aspects of the plugins are as in the previous version, ie groups, instantiation order etc.
 The only difference is that we'll repeat the instantiation if the plugin says oneInstancePerDIContext = true

 *
 */
@LateralPluginParameters(configName = "hazelcast_cache", groups = "cache_provider" , oneInstancePerDIContext = true )
public class HazelcastPlugin implements LateralPlugin {

    private static final Log LOG = LogFactory.getLog(HazelcastPlugin.class);

    public void initialise(Properties properties, Class<? extends LateralDIContext> context) {

        LOG.info("Initialising hazelcast plugin...");

        HazelcastInstance hazel=null;

        // NOTE: configure the map store if needed
        //(no!) for hazelcast the mapstores can only be configured for the
        //server not the client, so put the code there
        String cp_fn = properties.getProperty("lateral_plugin.hazelcast_cache.config_file");
        ClientConfig clientConfig = null;
        if (cp_fn!=null) {
            try {
                clientConfig= new XmlClientConfigBuilder(cp_fn).build();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            clientConfig = new ClientConfig();
        }
        hazel = HazelcastClient.newHazelcastClient(clientConfig);

        //purely to prevent builds failing with no generated code, use the annotation scanner to
        //retrieve the repository manager
        HCRepositoryManager manager = inject(HCRepositoryManager.class, context);
        manager.initRepositories(hazel);

    }
}
