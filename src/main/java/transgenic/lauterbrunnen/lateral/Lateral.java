package transgenic.lauterbrunnen.lateral;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import transgenic.lauterbrunnen.lateral.di.ApplicationDI;
import transgenic.lauterbrunnen.lateral.plugin.ApplicationPluginManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by stumeikle on 19/06/16.
 */
public enum Lateral {

    INSTANCE;

    private static final Log LOG = LogFactory.getLog(Lateral.class);
    private Properties properties;

    public void initialise() {
        properties = new Properties();
        InputStream in = getClass().getClassLoader().getResourceAsStream("application.properties");
        try {
            properties.load(in);
            in.close();
        } catch (IOException | NullPointerException e) {
            LOG.fatal("Unable to load properties file.");
        }

        ApplicationDI.initialise(properties);
        try {
            ApplicationPluginManager.INSTANCE.initialise(properties);
        } catch (Exception ex) {
            LOG.fatal("Unable to initialise plugins",ex);
        }
    }

    public Properties getProperties() {
        return properties;
    }
}
