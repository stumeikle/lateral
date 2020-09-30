package transgenic.lauterbrunnen.lateral.di;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import transgenic.lauterbrunnen.lateral.Lateral;
import transgenic.lauterbrunnen.lateral.plugin.AnnotationScanner;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by stumeikle on 17/07/19.
 *
 * If we have a multiple lateral domains
 * Actually i'm using multiple terms to mean the same thing -- contexts, domains
 * then lateral needs to know what those domains are before it can instantiate the plugins
 * and load the relevant properties files.
 *
 * So should these all be called domains not contexts?
 *
 * btw some plugins might be able to operate cross-domain and won't need domain specific config
 *
 * Let's keep the logic as previously -- you have to mark 1 implementation class as default impl OR you need to
 * specify via properties OR you need to programmatically register, otherwise we won't know what to instantiate.
 * IE if there's a single implementation and it's not tagged as default we'll barf. Could be changed in future perhaps
 */
public class ApplicationCDI {

    private static final Log LOG = LogFactory.getLog(ApplicationCDI.class);
    private static final String PROPERTY_PREFIX = "di.class.for.";
    private static final String PROPERTY_CONTEXT = ".context.";
    private final AnnotationScanner annotationScanner = new AnnotationScanner();
    private Set<Class<? extends LateralDIContext>> contexts;
    private Class<? extends LateralDIContext> defaultContext = DefaultContext.class;
    private Map<Class<? extends LateralDIContext>, Map<Class, Class>> implementationClasses = new ConcurrentHashMap<>();
    private Map<Class<? extends LateralDIContext>, Map<Class, Object>> implementations = new ConcurrentHashMap<>();
    private Map<Class, Class<? extends LateralDIContext>> ifaceContextMap = new ConcurrentHashMap<>();

    //scan and find the contexts
    public ApplicationCDI(Properties properties) throws DIException {
        annotationScanner.scan("", ".ejb.", "transgenic.lauterbrunnen");
        annotationScanner.scan("transgenic.lauterbrunnen", ".ejb.", "transgenic.lauterbrunnen");

        initialise(properties);
    }

    public ApplicationCDI(String packageToScan, Properties properties) throws DIException {
        annotationScanner.scan(packageToScan, ".ejb.", "transgenic.lauterbrunnen");

        initialise(properties);
    }

    private void initialise(Properties properties) throws DIException {

        //(1) find all the contexts from the scan
        Set<Class> dicontexts = annotationScanner.get(DIContext.class);
        this.contexts = new HashSet<>();
        if (dicontexts != null) {
            for (Class context : dicontexts) {

                //bit more involved
                for (Annotation note : context.getAnnotations()) {
                    if (note.annotationType().getName().equals(DIContext.class.getName())) {
                        DIContext diContext = (DIContext) note;
                        this.contexts.add(diContext.value());
                    }
                }
            }
        }

        //establish the default context. ie either use a defaultcontext class or if there is only 1 context use that
        //as the default
        if (this.contexts.size() == 1) {
            defaultContext = this.contexts.iterator().next();
        }

        implementationClasses.put(defaultContext, new ConcurrentHashMap<>());
        implementations.put(defaultContext, new ConcurrentHashMap<>());
        for (Class context : this.contexts) {
            if (!implementationClasses.containsKey(context)) {
                implementationClasses.put(context, new ConcurrentHashMap<>());
            }
            if (!implementations.containsKey(context)) {
                implementations.put(context, new ConcurrentHashMap<>());
            }
        }

        //(2) register the default implementations
        //    we can only have 1 default implementation per interface per context
        //
        // use case -- give me the implementation for this interface
        //             give me the implementation for this interface in this context
        // Map< Context, Map< interface class, impl class > >
        Set<Class> defaultImpls = annotationScanner.get(DefaultImpl.class);

        if (defaultImpls != null) {
            for (Class clazz : defaultImpls) {
                for (Class iface : clazz.getInterfaces()) {

                    //Ok we want to register that the clazz is the one to use for the interface iface
                    //(1) establish the context
                    Class<? extends LateralDIContext> context = getContext(clazz);

                    //(2) do we already have a default implementation?
                    Map<Class, Class> mapping = implementationClasses.get(context);
                    if (mapping.containsKey(iface)) {
                        //we've already registered a default impl for this interface
                        throw new DIException("You have multiple DefaultImpl annotations for interface " + iface.getName() +
                                " . Perhaps you have multiple contexts and need to specify the context?");
                    }
                    registerImplementationClass(iface, context, clazz);
                }
            }
        }

        //(3) overload as per the properties
        //override these with configured properties
        //di.class.for.HCMapStoreFactory=transgenic.lauterbrunnen.lateral.persist.hazelcast.generated.HCReadThroughFactoryImpl
        //di.class.for.HCMapStoreFactory=transgenic.lauterbrunnen.lateral.persist.hazelcast.generated.HCWriteThroughFactoryImpl
        //di.class.for.HCMapStoreFactory=transgenic.lauterbrunnen.lateral.persist.hazelcast.generated.HCReadWriteThroughFactoryImpl
        //
        //define the contexts if needed with
        //
        //di.class.for.HCMapStoreFactory.context.MyContext=transgenic.lauterbrunnen.lateral.persist.hazelcast.generated.HCWriteThroughFactoryImpl
        //
        //Do this by iterating through the properties. This ensures the config is respected if
        //no default is specified
        if (properties == null) return;
        for (Object objectKey : properties.keySet()) {
            String stringKey = (String) objectKey;
            if (!stringKey.startsWith(PROPERTY_PREFIX)) continue;

            stringKey = stringKey.replace(PROPERTY_PREFIX, "");
            String contextClassName = defaultContext.getSimpleName();
            Class<? extends LateralDIContext> contextClass = defaultContext;
            if (stringKey.contains(PROPERTY_CONTEXT)) {
                int position = stringKey.lastIndexOf(PROPERTY_CONTEXT) + PROPERTY_CONTEXT.length();
                contextClassName = stringKey.substring(position);
                stringKey = stringKey.substring(0, stringKey.lastIndexOf(PROPERTY_CONTEXT));

                //(3.0) check that the context class exists and is a context
                contextClass = convertContextNameToClass(contextClassName);
            }

            String ifaceClassName = stringKey;
            //(3.1) check that the value implements the interface specified in the key
            //      if it does then register it.
            try {
                Class valueClass = Class.forName((String) properties.get(objectKey));
                Class ifaceClass = null;

                for (Class iface : valueClass.getInterfaces()) {
                    if (iface.getSimpleName().equals(ifaceClassName)) {
                        ifaceClass = iface;
                        break;
                    }
                }

                if (ifaceClass == null) {
                    throw new DIException("You have class " + (String) objectKey + " configured in your properties for interface " + ifaceClassName + " but it does not implement that interface.");
                }

                LOG.info("Using configured class " + (String) objectKey + " for injecting " + ifaceClassName + " in context " + contextClassName);
                registerImplementationClass(ifaceClass, contextClass, valueClass);

            } catch (ClassNotFoundException e) {
                throw new DIException("Class " + (String) objectKey + " specified in your properties file but I'm unable to Class.forName() it.");
            }
        }
    }

    private Class<? extends LateralDIContext> convertContextNameToClass(String contextClassName) throws DIException {
        //NOT VERY EFFICIENT TODO

        //It's a short name here so we can't class.forname it
        for (Class<? extends LateralDIContext> context : this.contexts) {
            if (context.getSimpleName().equals(contextClassName)) {
                // ok
                return context;
            }
        }

        throw new DIException("You've specified context " + contextClassName + " in your properties but I don't recognise it as a context.");
    }

    private Class<? extends LateralDIContext> getContext(Class clazz) {
        for (Annotation note : clazz.getAnnotations()) {
            if (note.annotationType().getName().equals(DIContext.class.getName())) {
                DIContext diContext = (DIContext) note;
                return diContext.value();
            }
        }

        //if there are no annotations or no context then use the default
        return defaultContext;
    }

    public <T> void registerImplementationClass(Class<T> iface, Class<? extends T> implClass) throws DIException {

        if (!ifaceContextMap.containsKey(iface)) {
            //then we'll use the default
            ifaceContextMap.put(iface, defaultContext);
        } else {
            Class<? extends LateralDIContext> context = ifaceContextMap.get(iface);
            if (context.equals(MultipleContexts.class)) {
                throw new DIException("You need to specify a context when registering implementation class for " + iface.getName());
            }
        }

        registerImplementationClass(iface, ifaceContextMap.get(iface), implClass);
    }

    public <T> void registerImplementationClass(Class<T> iface, Class<? extends LateralDIContext> context, Class<? extends T> implClass) {
        Map<Class, Class> mapping = implementationClasses.get(context);
        if (mapping.containsKey(iface)) {
            LOG.info("Overriding implementation class " + mapping.get(iface).getName() + " with " + implClass + " for context " + context.getSimpleName());
        }
        mapping.put(iface, implClass);

        //also build reverse iface -> context
        if (!ifaceContextMap.containsKey(iface)) {
            ifaceContextMap.put(iface, context);
        } else {
            if (ifaceContextMap.get(iface).equals(context)) {
                //WARNING, comparing classes with equals
                //context same as before, that's fine
            } else {
                //>1 context specifies an implementation for this interface
                ifaceContextMap.put(iface, MultipleContexts.class);
            }
        }
    }

    public <T> void registerImplementation(Class<T> iface, Class<? extends LateralDIContext> context, T impl) {
        Map<Class, Object> mapping = implementations.get(context);

        //let's not be generous to ensure we have a coherent system (ie ignore new on the fly context creation)
        mapping.put(iface, impl);
    }

    public <T> void registerImplementation(Class<T> iface, T impl) throws DIException {
        if (!ifaceContextMap.containsKey(iface)) {
            //then we'll use the default
            ifaceContextMap.put(iface, defaultContext);
        } else {
            Class<? extends LateralDIContext> context = ifaceContextMap.get(iface);
            if (context.equals(MultipleContexts.class)) {
                throw new DIException("You need to specify a context when registering implementation class for " + iface.getName());
            }
        }

        registerImplementation(iface, ifaceContextMap.get(iface), impl);
    }

    //Requesting an implementation without providing the context
    //Bit fiddly
    //We want to understand if the interface is only implemented in 1 context.
    //If so there's no ambiguity and we can go ahead and instantiate it
    //Else barf
    public <T> T getImplementation(Class<T> interfaceClass) throws DIException {

        //(1) unique?
        if (ifaceContextMap.containsKey(interfaceClass)) {
            Class<? extends LateralDIContext> context = ifaceContextMap.get(interfaceClass);

            if (context.equals(MultipleContexts.class)) {
                throw new DIException("Unable to get implementation for class " + interfaceClass + " without specified context as multiple contexts implement it.");
            }

            return getImplementation(interfaceClass, context);
        }
        //else we don't konw the interface
        throw new DIException("Unable to get implementation for " + interfaceClass + " as this interface is unknown.");
    }

    public <T> T getImplementation(Class<T> interfaceClass, Class<? extends LateralDIContext> context) throws DIException {

        Map<Class, Object> instanceMap = implementations.get(context);
        T instance = (T) instanceMap.get(interfaceClass);
        if (instance != null) return instance;

        //There is no implementation yet, try to create a new one
        Class implClass = implementationClasses.get(context).get(interfaceClass);
        if (implClass != null) {
            try {
                instance = (T) implClass.newInstance();
                registerImplementation(interfaceClass, context, instance);
                return instance;
            } catch (InstantiationException e) {
                LOG.error(e);
            } catch (IllegalAccessException e) {
                LOG.error(e);
            }
        }
        //else we don't konw the interface
        throw new DIException("Unable to get implementation for " + interfaceClass + " as this interface is unknown.");
    }

    public AnnotationScanner getAnnotationScanner() {
        return annotationScanner;
    }

    public Set<Class<? extends LateralDIContext>> getDIContexts() {
        return contexts;
    }

    public Class<? extends LateralDIContext> getDefaultContext() {
        return defaultContext;
    }

    //Temporary perhaps
    public Map<Class<? extends LateralDIContext>, Map<Class, Class>> getImplementationClasses() {
        return implementationClasses;
    }

    public <T> BindBuilder bind(Class<T> interfaceClass) {
        return new BindBuilder<T>(this, interfaceClass);
    }

    public <T> T inject(Class<T> interfaceClass) {
        try {
            return getImplementation(interfaceClass);
        } catch (DIException e) {
            LOG.fatal(e);
            return null;
        }
    }

    //For debug
    //Dump the bindings

    public String dumpInjectionBindings() {
        StringBuffer sb = new StringBuffer();

        for (Class<? extends LateralDIContext> context : implementationClasses.keySet()) {
            sb.append("Context " + context.getName() + ":" + System.lineSeparator());
            sb.append(System.lineSeparator());
            Map<Class, Class> api2ProviderMap = implementationClasses.get(context);
            List<Class> setAsList = new ArrayList<>(api2ProviderMap.keySet().size());
            setAsList.addAll(api2ProviderMap.keySet());
            //wow this is FAR TOO DIFFICULT
            Collections.sort(setAsList, new Comparator<Object>() {
                public int compare(Object o1, Object o2) {
                    String s1 = ((Class) o1).getName();
                    String s2 = ((Class) o2).getName();
                    return s1.compareTo(s2);
                }

            });

            for (Class api : setAsList) {
                String apiName = api.getName();
                apiName = apiName.replace("transgenic.lauterbrunnen.lateral", "<lateral>");

                sb.append(apiName + " bound to " + api2ProviderMap.get(api).getName() + System.lineSeparator());
            }
            sb.append(System.lineSeparator());
        }

        return sb.toString();
    }
}
