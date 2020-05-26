package transgenic.lauterbrunnen.lateral.cache.zero;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import transgenic.lauterbrunnen.lateral.cache.hazelcast.HCRepositoryManager;
import transgenic.lauterbrunnen.lateral.di.LateralDIContext;
import transgenic.lauterbrunnen.lateral.plugin.LateralPlugin;
import transgenic.lauterbrunnen.lateral.plugin.LateralPluginParameters;

import java.util.Properties;

import static transgenic.lauterbrunnen.lateral.Lateral.inject;


/**
 * Created by stumeikle on 17/04/17.
 */
@LateralPluginParameters(configName = "zero_cache", groups = "cache_provider", oneInstancePerDIContext = true)
public class ZeroCachePlugin implements LateralPlugin {

    private static final Log LOG = LogFactory.getLog(ZeroCachePlugin.class);

    public void initialise(Properties properties, Class<? extends LateralDIContext> context) {

        LOG.info("Initialising zero cache plugin...");

        //purely to prevent builds failing with no generated code, use the annotation scanner to
        //retrieve the repository manager
        ZCRepositoryManager manager = inject(ZCRepositoryManager.class, context);
        manager.initRepositories();
    }
}

