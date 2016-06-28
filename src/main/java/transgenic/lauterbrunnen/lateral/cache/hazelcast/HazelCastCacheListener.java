package transgenic.lauterbrunnen.lateral.cache.hazelcast;

import com.hazelcast.core.IMap;
import transgenic.lauterbrunnen.lateral.plugin.ApplicationPlugin;
import transgenic.lauterbrunnen.lateral.plugin.ApplicationPluginParameters;

import java.util.Map;
import java.util.Properties;

import static transgenic.lauterbrunnen.lateral.di.ApplicationDI.inject;


/**
 * Created by stumeikle on 10/06/16.
 */
@ApplicationPluginParameters(configName = "hazelcast_listener", groups = "hazelcast_listener" )
public class HazelCastCacheListener implements ApplicationPlugin {

    public void initialise(Properties properties)  {

        //for each map in each repository add the needed listeners
        HCRepositoryManager manager = inject(HCRepositoryManager.class);
        Map<String, IMap> iMapMap = manager.getImapNameMap();
        HCCacheChangeManager cacheChangeManager = inject(HCCacheChangeManager.class);
        cacheChangeManager.initialise(iMapMap);
    }
}
