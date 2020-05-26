package transgenic.lauterbrunnen.lateral.dicontext;

import transgenic.lauterbrunnen.lateral.di.DIContext;
import transgenic.lauterbrunnen.lateral.di.DefaultImpl;

/**
 * Created by stumeikle on 25/07/19.
 */
@DefaultImpl
@DIContext(ServiceDiscoveryContext.class)
public class ServiceDiscoveryFactory implements Factory {
}
