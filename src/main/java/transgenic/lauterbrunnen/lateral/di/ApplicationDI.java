package transgenic.lauterbrunnen.lateral.di;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import transgenic.lauterbrunnen.lateral.plugin.AnnotationScanner;

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
        //look for default implementations
        for(Class c : AnnotationScanner.INSTANCE.get(DefaultImpl.class)) {

            for(Class iface: c.getInterfaces()) {
                ApplicationDI.registerImplementationClass(iface, c);
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
