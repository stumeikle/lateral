package transgenic.lauterbrunnen.lateral.plugin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import transgenic.lauterbrunnen.lateral.Lateral;
import transgenic.lauterbrunnen.lateral.di.DefaultContext;
import transgenic.lauterbrunnen.lateral.di.LateralDIContext;

import java.util.*;

/**
 * Created by stumeikle on 30/07/19.
 *
 * 20190903 Further changes needed here. In v 0.1-snapshot we initialise according to the graph of groups
 * and for each group we instantiate the plugins and check for violations of mutual exclusivity
 *
 * with v2 things are different - the mutually exclusive groups are per context, if the plugins are 1 per context.
 * ie it would be possible to have 1 context use a hazelcast cache and another use ehcache or zero cache, in principle
 *
 */
public class LateralPluginManager {

    private static final Log LOG = LogFactory.getLog(LateralPluginManager.class);
    private static final String DEFAULT_GROUP = "default";
    private final Map<Class<? extends LateralDIContext>, Map<Class<? extends  LateralPlugin>, LateralPlugin>> plugins = new HashMap<>();
    //<< this could probably be context vs map class / plugin

    private final Map<String, Set<Class<? extends LateralPlugin>>> pluginsPerGroup = new HashMap<>();
    private final Set<String> mutuallyExclusiveGroups = new HashSet<>();
    private final Map<Class<? extends LateralDIContext>, Set<String>> groupsInstantiatedPerContext = new HashMap<>();

    public LateralPlugin getApplicationPlugin(Class fp) {
        return plugins.get(Lateral.INSTANCE.getDefaultContext()).get(fp);
    }
    //<< add a form of this which takes the context also
    public LateralPlugin getApplicationPlugin(Class<? extends LateralDIContext> context, Class fp) {
        return plugins.get(context).get(fp);
    }


    public void reportPlugins() {
        StringBuilder sb = new StringBuilder();

        sb.append("Enabled plugins: ");
        boolean first = true;
        for(Class c: Lateral.INSTANCE.getAnnotationScanner().get(LateralPluginParameters.class)) {
            if (plugins.containsKey(c)) {
                LateralPluginParameters note = (LateralPluginParameters) c.getAnnotation(LateralPluginParameters.class);
                if (!first) sb.append(", ");
                sb.append(note.configName().toUpperCase());
                first= false;
            }
        }

        LOG.info(sb.toString());
    }

    public void initialise(Properties properties) throws Exception {

        //reset and/or create needed maps
        plugins.clear();
        for(Class<? extends  LateralDIContext> context: Lateral.INSTANCE.getDIContexts()) {
            plugins.put(context, new HashMap<>());
        }
        plugins.put(Lateral.INSTANCE.getDefaultContext(), new HashMap<>());

        //new routine
        //Get all the plugins
        //Hash by groups
        //initialise according to the specified order
        if (Lateral.INSTANCE.getAnnotationScanner().get(LateralPluginParameters.class) == null) return;

        for (Class c : Lateral.INSTANCE.getAnnotationScanner().get(LateralPluginParameters.class)) {
            LateralPluginParameters note = (LateralPluginParameters) c.getAnnotation(LateralPluginParameters.class);

            String groups = note.groups();
            if (groups==null || groups.isEmpty()) {
                groups = DEFAULT_GROUP;
            }
            String[] groupArray = groups.split(",");
            for(String group: groupArray) {
                group = group.trim();
                Set<Class<? extends LateralPlugin>> set = pluginsPerGroup.get(group);
                if (set==null) {
                    set = new HashSet<>();
                    pluginsPerGroup.put(group,set);
                }

                set.add(c);
            }
        }

        loadMutexGroups(properties);

        String order = properties.getProperty(LateralPlugin.LATERAL_PLUGIN +".groups.initialise_order");
        if (order!=null) {
            initialisePluginsByGroup(order, properties);
        }
        //initialise any plugins not already initialised ...
        List<String>        keys = new ArrayList<>(pluginsPerGroup.size());
        keys.addAll(pluginsPerGroup.keySet());
        for(String key: keys) {
            initialisePluginsByGroup(key, properties);
        }
    }

    private boolean classEnabled(Class<? extends LateralPlugin> clazz, Properties properties) {
        if (properties==null) return false;

        LateralPluginParameters note = (LateralPluginParameters)clazz.getAnnotation(LateralPluginParameters.class);
        String feature = LateralPlugin.LATERAL_PLUGIN + "." + note.configName() + ".enabled";
        String enabledProperty = (String) properties.get(feature);
        boolean enabled = note.enabledByDefault();
        if (enabledProperty!=null) {
            if ("true".equalsIgnoreCase(enabledProperty)) {
                enabled=true;
            }
            if ("false".equalsIgnoreCase(enabledProperty)) {
                enabled=false;
            }
        }
        return enabled;
    }

    private void loadMutexGroups(Properties properties) throws Exception {
        String mutex_groups = properties.getProperty(LateralPlugin.LATERAL_PLUGIN + ".groups.mutually_exclusive");
        if (mutex_groups!=null) {
            String[] groups = mutex_groups.split(",");
            for(String group: groups) {
                group = group.trim();
                mutuallyExclusiveGroups.add(group);
//                if (pluginsPerGroup.get(group)!=null && pluginsPerGroup.get(group).size()>1) {
//                    throw new Exception("Group " + group + " is configured as mutually exclusive, but there are multiple enabled plugins in this group");
//                }
            }
        }
    }

    private void initialisePluginsByGroup(String initOrder, Properties properties) throws Exception {
        String[] groups = initOrder.split(",");
        for(String group: groups) {
            group = group.trim();

            if (pluginsPerGroup.containsKey(group)) {
                for(Class<? extends LateralPlugin> clazz: pluginsPerGroup.get(group)) {
                    initialisePlugin(clazz, properties, group);
                }
            }

            pluginsPerGroup.remove(group);
        }
    }

    private void initialisePlugin(Class<? extends LateralPlugin> clazz, Properties properties, String group) throws Exception{

        //All change here
        //Check if the plugin is for all contexts or one per context
        LateralPluginParameters note = (LateralPluginParameters)clazz.getAnnotation(LateralPluginParameters.class);
        boolean oneInstancePerClass = note.oneInstancePerDIContext();

        //If for all contexts initialise as previously
        if (!oneInstancePerClass) {
            //easy case
            //Single instance here, if it's there already then skip
            instantiateAndRegisterPlugin(clazz,Lateral.INSTANCE.getDefaultContext() , properties, group);
            return;
        }

        //If one per context, we need to pre-process the properties before we initialise
        for(Class<? extends LateralDIContext> context: Lateral.INSTANCE.getDIContexts()) {
            Properties  duplicateProperties = new Properties();

            for(Object objectKey: properties.keySet()) {
                Object property = properties.get(objectKey);
                String key = (String)objectKey;

                key=key.replace(".dicontext."+context.getSimpleName(), "");
                duplicateProperties.put(key,property);
            }

            instantiateAndRegisterPlugin(clazz, context, duplicateProperties, group);
        }

    }

    private void instantiateAndRegisterPlugin(Class<? extends LateralPlugin> clazz, Class<? extends LateralDIContext> context, Properties properties, String group) throws Exception{
        if (pluginInstanceExists(clazz, context)) return;

        if (classEnabled(clazz, properties)) {
            //Ok, now check if we violate the mutual exclusivity setting
            //(1) is this group one which enforced mutual exclusivity?
            Set<String> instantiatedGroups = groupsInstantiatedPerContext.get(context);
            if (mutuallyExclusiveGroups.contains(group)) {
                if (instantiatedGroups!=null && instantiatedGroups.contains(group)) {
                    //failed, throw exception
                    throw new Exception("Group " + group + " is configured as mutually exclusive, but there are multiple enabled plugins in this group");
                }
            };

            if (instantiatedGroups==null) {
                instantiatedGroups = new HashSet<>();
                groupsInstantiatedPerContext.put(context, instantiatedGroups);
            }
            instantiatedGroups.add(group); //ok ok, we haven't actually instantiated it yet but ...

            LateralPlugin fp = null;
            try {
                fp = clazz.newInstance();
                fp.initialise(properties, context);
                registerPluginInstance(clazz, context, fp);
            } catch (InstantiationException e) {
                LOG.error("Unable to instantiate plugin for class " + clazz.getName());
            } catch (IllegalAccessException e) {
                LOG.error("Unable to instantiate plugin for class " + clazz.getName());
            }
        }

    }

    private boolean pluginInstanceExists(Class<? extends LateralPlugin> clazz, Class<? extends LateralDIContext> context) {
//        System.out.println("Checking context " + context + " contains " + clazz.getName());
        return plugins.get(context).containsKey(clazz);
    }

    private void registerPluginInstance(Class<? extends LateralPlugin> clazz, Class<? extends LateralDIContext> context, LateralPlugin lateralPlugin){
        plugins.get(context).put(clazz, lateralPlugin);
    }
}
