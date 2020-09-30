package transgenic.lauterbrunnen.lateral;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import transgenic.lauterbrunnen.lateral.di.ApplicationCDI;
import transgenic.lauterbrunnen.lateral.di.BindBuilder;
import transgenic.lauterbrunnen.lateral.di.DIException;
import transgenic.lauterbrunnen.lateral.di.LateralDIContext;
import transgenic.lauterbrunnen.lateral.plugin.AnnotationScanner;
import transgenic.lauterbrunnen.lateral.plugin.LateralPluginManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;

/**
 * Created by stumeikle on 29/07/19.
 */
public enum Lateral {

    INSTANCE;

    private static final Log LOG = LogFactory.getLog(Lateral.class);
    private Properties properties;
    private ApplicationCDI  applicationCDI;
    private String propertyFilename="application.properties";

    public void initialise() {
        initialiseDI(null);
        initialisePlugins();
    }

    public void setPropertyFilename(String propertyFilename) {
        this.propertyFilename = propertyFilename;
    }

    public void initialiseDI(String packageName) {
        properties = new Properties();
        InputStream in = getClass().getClassLoader().getResourceAsStream(propertyFilename);
        try {
            properties.load(in);
            in.close();
        } catch (IOException | NullPointerException e) {
            LOG.warn("Unable to load properties file.");
        }

        try {
            if (packageName==null)
                applicationCDI = new ApplicationCDI(properties);
            else
                applicationCDI = new ApplicationCDI(packageName, properties);
        } catch (DIException e) {
            LOG.fatal("Unable to initialise application dependency injection.", e);
            System.exit(-1);
        }
    }

    public void initialisePlugins() {
        //tdb
        //need context specific plugin instantiation
        //depending on the annotations. 1 for all or 1 per context
        LateralPluginManager pluginManager2 = new LateralPluginManager();
        try {
            pluginManager2.initialise(properties);
        } catch (Exception ex) {
            LOG.fatal("Unable to initialise plugins",ex);
        }
    }

    //Temporary perhaps
    public ApplicationCDI getApplicationCDI() {
        return applicationCDI;
    }

    //------- Helper methods --------------------------------------------------------
    // Some of these catch exceptions in the name of convenience so
    // use with care!!
    //
    public static <T> T inject(Class<T> interfaceClass) {
        try {
            return INSTANCE.applicationCDI.getImplementation(interfaceClass);
        } catch (DIException e) {
            LOG.fatal(e);
            return null;
        }
    }

    public static <T> T inject(Class<T> interfaceClass, Class<? extends LateralDIContext> context) {
        try {
            return INSTANCE.applicationCDI.getImplementation(interfaceClass, context);
        } catch (DIException e) {
            LOG.fatal(e);
            return null;
        }
    }

    public static <T> boolean registerImplementationClass(Class<T> interfaceClass, Class<? extends T> implClass) {
        try {
            INSTANCE.applicationCDI.registerImplementationClass(interfaceClass, implClass);
            return true;
        } catch (DIException e) {
            LOG.fatal(e);
            return false;
        }
    }

    public static <T> void registerImplementationClass(Class<T> interfaceClass, Class<? extends LateralDIContext> context, Class<? extends T> implClass) {
        INSTANCE.applicationCDI.registerImplementationClass(interfaceClass, context, implClass);
    }

    public static <T> boolean registerImplementation(Class<T> interfaceClass, T impl) {
        try {
            INSTANCE.applicationCDI.registerImplementation(interfaceClass, impl);
            return true;
        } catch (DIException e) {
            LOG.fatal(e);
            return false;
        }
    }

    public static <T> void registerImplementation(Class<T> interfaceClass, Class<? extends LateralDIContext> context, T impl) {
        INSTANCE.applicationCDI.registerImplementation(interfaceClass, context, impl);
    }

    public AnnotationScanner getAnnotationScanner() {
        return applicationCDI.getAnnotationScanner();
    }

    public Set<Class<? extends  LateralDIContext>> getDIContexts() {
        return INSTANCE.applicationCDI.getDIContexts();
    }

    public  Class<? extends LateralDIContext> getDefaultContext() {
        return INSTANCE.applicationCDI.getDefaultContext();
    }

    //probably we need a way to add a context also.


}
