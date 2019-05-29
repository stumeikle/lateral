package transgenic.lauterbrunnen.lateral.cache.hazelcast;

import com.hazelcast.core.IMap;
import transgenic.lauterbrunnen.lateral.admin.Admin;
import transgenic.lauterbrunnen.lateral.admin.Command;
import transgenic.lauterbrunnen.lateral.admin.CommandHandler;
import transgenic.lauterbrunnen.lateral.admin.CommandResponse;
import transgenic.lauterbrunnen.lateral.admin.jgroups.JGAdminCommandBus;
import transgenic.lauterbrunnen.lateral.admin.jgroups.JGIncomingMessageQueue;
import transgenic.lauterbrunnen.lateral.admin.jgroups.JGOutgoingMessageQueue;
import transgenic.lauterbrunnen.lateral.plugin.LateralPlugin;
import transgenic.lauterbrunnen.lateral.plugin.LateralPluginParameters;

import java.util.Map;
import java.util.Properties;

import static transgenic.lauterbrunnen.lateral.di.ApplicationDI.inject;


/**
 * Created by stumeikle on 10/06/16.
 */
@LateralPluginParameters(configName = "hazelcast_listener", groups = "hazelcast_listener" )
public class HazelCastCacheListener implements LateralPlugin {

    public void initialise(Properties properties)  {

        //for each map in each repository add the needed listeners
        HCRepositoryManager manager = inject(HCRepositoryManager.class);
        Map<String, IMap> iMapMap = manager.getImapNameMap();
        HCCacheChangeManager cacheChangeManager = inject(HCCacheChangeManager.class);
        cacheChangeManager.initialise(iMapMap);
    }
}
