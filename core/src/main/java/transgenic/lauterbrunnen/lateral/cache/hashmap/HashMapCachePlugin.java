package transgenic.lauterbrunnen.lateral.cache.hashmap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import transgenic.lauterbrunnen.lateral.di.LateralDIContext;
import transgenic.lauterbrunnen.lateral.plugin.LateralPlugin;
import transgenic.lauterbrunnen.lateral.plugin.LateralPluginParameters;

import java.util.Properties;

import static transgenic.lauterbrunnen.lateral.Lateral.inject;

/**
 * Created by stumeikle on 03/10/20.
 *
 * Provides very simple caching for testing domain classes in isolation of sophisticated caching layers
 */
@LateralPluginParameters(configName = "hashmap_cache", groups = "cache_provider" , oneInstancePerDIContext = true )
public class HashMapCachePlugin implements LateralPlugin {

    private static final Log LOG = LogFactory.getLog(HashMapCachePlugin.class);

    public void initialise(Properties properties, Class<? extends LateralDIContext> context) {

        LOG.info("Initialising hashmap cache plugin...");

        HMRepositoryManager manager = inject(HMRepositoryManager.class, context);
        manager.initRepositories();

    }
}