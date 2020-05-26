package transgenic.lauterbrunnen.lateral.dicontext;

import transgenic.lauterbrunnen.lateral.di.DIContext;
import transgenic.lauterbrunnen.lateral.di.DefaultImpl;

/**
 * Created by stumeikle on 25/07/19.
 */
@DIContext(ServiceDiscoveryContext.class)
@DefaultImpl
public class CommonForServiceDiscovery implements CommonInterface{
    @Override
    public String getMessage() {
        return "Common for Service Discovery says hi";
    }
}
