package transgenic.lauterbrunnen.lateral.rest;

import org.glassfish.jersey.server.ResourceConfig;

/**
 * Created by stumeikle on 10/11/16.
 * Bogus but allows injection
 */
public interface PluggableResourceConfig {

    ResourceConfig getResourceConfig();
}
