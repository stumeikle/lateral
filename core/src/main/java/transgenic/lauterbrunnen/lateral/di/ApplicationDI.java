package transgenic.lauterbrunnen.lateral.di;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import transgenic.lauterbrunnen.lateral.plugin.AnnotationScanner;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Stuart.meikle on 05/05/2016.
 * Dependency injection
 */
public class ApplicationDI {

    private static final Log LOG = LogFactory.getLog(ApplicationDI.class);
    private static Map<Class, Class>        implementationClasses = new ConcurrentHashMap<>();
    private static Map<Class, Object>       implementations = new ConcurrentHashMap<>();

    public static void initialise(Properties properties) {
        //Setup the annotation scanner
        AnnotationScanner.INSTANCE.scan("",".ejb.", "transgenic.lauterbrunnen" );
        AnnotationScanner.INSTANCE.scan("transgenic.lauterbrunnen",".ejb.", "transgenic.lauterbrunnen" );

        //look for default implementations
        List<Class> defaultImpls = AnnotationScanner.INSTANCE.get(DefaultImpl.class);
        if (defaultImpls!=null) {
            for (Class c : AnnotationScanner.INSTANCE.get(DefaultImpl.class)) {
                for (Class iface : c.getInterfaces()) {
                    ApplicationDI.registerImplementationClass(iface, c);
                }
            }
        }

        //override these with configured properties
        //di.class.for.HCMapStoreFactory=transgenic.lauterbrunnen.lateral.persist.hazelcast.generated.HCReadThroughFactoryImpl
        //di.class.for.HCMapStoreFactory=transgenic.lauterbrunnen.lateral.persist.hazelcast.generated.HCWriteThroughFactoryImpl
        //di.class.for.HCMapStoreFactory=transgenic.lauterbrunnen.lateral.persist.hazelcast.generated.HCReadWriteThroughFactoryImpl
        //Do this by iterating through the properties. This ensures the config is respected if
        //no default is specified

        //TODO -- me this is odd and seems to ignore the class name in the property key
        for(Object propertyKey: properties.keySet()) {
            String pk = (String) propertyKey;
            if (!pk.startsWith("di.class.for.")) continue;
            String impClassName= (String) properties.get(pk);
            try {
                Class impClass = Class.forName(impClassName);
                for(Class iface: impClass.getInterfaces()) {
                    ApplicationDI.registerImplementationClass(iface, impClass);
                    LOG.info("Using " + impClass + " for " + iface);
                }
            } catch (ClassNotFoundException e) {
                LOG.warn("Unable to find class " + impClassName);
            }
        }
    }

    public static <T> T getImplementation(Class<T> interfaceClass) {
        T retval = (T) implementations.get(interfaceClass);
        if (retval!=null) return retval;

        //if there is no implementation, try to create a new one
        Class implClass = implementationClasses.get(interfaceClass);
        if (implClass!=null) {
            try {
                T   instance = (T) implClass.newInstance();
                registerImplementation(interfaceClass, instance);
                return instance;
            } catch (InstantiationException e) {
                LOG.error(e);
            } catch (IllegalAccessException e) {
                LOG.error(e);
            }
        }

        return null;
    }

    public static Class getImplementationClass(Class interfaceClass) {
        return implementationClasses.get(interfaceClass);
    }

    public static <T> T inject(Class<T> interfaceClass) {
        return getImplementation(interfaceClass);
    }

    public static <T> void registerImplementation(Class<T> interfaceClass, T implementation) {
        implementations.put(interfaceClass, implementation);
    }

    public static <T> void registerImplementationClass(Class<T> interfaceClass, Class<? extends T> implClass) {
        implementationClasses.put(interfaceClass, implClass);
    }

    public static <T> BindBuilder bind(Class<T> interfaceClass) {
        return new BindBuilder<T>(interfaceClass);
    }

}
