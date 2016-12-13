package transgenic.lauterbrunnen.lateral.cache.hazelcast;

import com.hazelcast.config.ClasspathXmlConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MapStoreConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IdGenerator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import transgenic.lauterbrunnen.lateral.admin.Admin;
import transgenic.lauterbrunnen.lateral.admin.Command;
import transgenic.lauterbrunnen.lateral.admin.CommandResponse;
import transgenic.lauterbrunnen.lateral.admin.CommandResponseHandler;
import transgenic.lauterbrunnen.lateral.admin.jgroups.JGAdminCommandBus;
import transgenic.lauterbrunnen.lateral.admin.jgroups.JGIncomingMessageQueue;
import transgenic.lauterbrunnen.lateral.admin.jgroups.JGOutgoingMessageQueue;
import transgenic.lauterbrunnen.lateral.plugin.LateralPlugin;
import transgenic.lauterbrunnen.lateral.plugin.LateralPluginParameters;

import java.util.Map;
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

            //20161213 We need to store all idgenerators in the map store so that they can be
            //later initialised. However. the repository manager is *not* initialised on the hc server side
            //so we don't have access to that information here.
            //Gets really messy

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

        //we are the server so expect to be the admin command sender
        //later we might be a receiver too
        JGAdminCommandBus adminCommandBus = new JGAdminCommandBus();
        JGOutgoingMessageQueue<Command> outgoingMessageQueue = new JGOutgoingMessageQueue<>(adminCommandBus);
        JGIncomingMessageQueue<CommandResponse> incomingMessageQueue = new JGIncomingMessageQueue<>(adminCommandBus);
        incomingMessageQueue.setHandler(new CommandResponseHandler());
        Admin.setAdminCommandBus( adminCommandBus ); //this is just to get the 2 queues

        //the other side will be the inverse
//        {
//            JGAdminCommandBus adminCommandBus = new JGAdminCommandBus();
//            JGOutgoingMessageQueue<CommandResponse> outgoingMessageQueue = new JGOutgoingMessageQueue<>(adminCommandBus);
//            JGIncomingMessageQueue<Command> incomingMessageQueue = new JGIncomingMessageQueue<>(adminCommandBus);
//            incomingMessageQueue.addHandler(new CommandHandler());
//            Admin.setCommandBus( adminCommandBus );
//
//            initialise the admin endpoints and give them the command handler
//        }
        //this is more complex though because the adminendpoints...
        //

        cfg.setInstanceName("lateral");
        HazelcastInstance instance = Hazelcast.newHazelcastInstance(cfg);

    }
}

