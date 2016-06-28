package transgenic.lauterbrunnen.lateral.plugin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

/**
 * Created by Stuart.meikle on 05/05/2016.
 */
public enum ApplicationPluginManager {

    INSTANCE;

    private static final Log LOG = LogFactory.getLog(ApplicationPluginManager.class);
    private static final String DEFAULT_GROUP = "default";
    private final HashMap<Class, ApplicationPlugin>  plugins = new HashMap<>();
    private final Map<String, Set<Class<? extends ApplicationPlugin>>> pluginsPerGroup = new HashMap<>();

    public ApplicationPlugin getApplicationPlugin(Class fp) {
        return plugins.get(fp);
    }

    public void reportPlugins() {
        StringBuilder sb = new StringBuilder();

        sb.append("Enabled plugins: ");
        boolean first = true;
        for(Class c: AnnotationScanner.INSTANCE.get(ApplicationPluginParameters.class)) {
            if (plugins.containsKey(c)) {
                ApplicationPluginParameters note = (ApplicationPluginParameters) c.getAnnotation(ApplicationPluginParameters.class);
                if (!first) sb.append(", ");
                sb.append(note.configName().toUpperCase());
                first= false;
            }
        }

        LOG.info(sb.toString());
    }

    //20160514
    public void initialise(Properties properties) throws Exception {

        //new routine
        //Get all the plugins
        //Hash by groups
        //initialise according to the specified order
        if (AnnotationScanner.INSTANCE.get(ApplicationPluginParameters.class) == null) return;

        for (Class c : AnnotationScanner.INSTANCE.get(ApplicationPluginParameters.class)) {
            ApplicationPluginParameters note = (ApplicationPluginParameters) c.getAnnotation(ApplicationPluginParameters.class);

            String groups = note.groups();
            if (groups==null || groups.isEmpty()) {
                groups = DEFAULT_GROUP;
            }
            String[] groupArray = groups.split(",");
            for(String group: groupArray) {
                group = group.trim();
                Set<Class<? extends ApplicationPlugin>> set = pluginsPerGroup.get(group);
                if (set==null) {
                    set = new HashSet<>();
                    pluginsPerGroup.put(group,set);
                }

                //only add the class if it is enabled
                if (classEnabled(c, properties))
                    set.add(c);
            }
        }

        checkMutexGroups(properties);

        String order = properties.getProperty(ApplicationPlugin.APPLICATION_PLUGIN +".groups.initialise_order");
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

    private void checkMutexGroups(Properties properties) throws Exception {
        String mutex_groups = properties.getProperty(ApplicationPlugin.APPLICATION_PLUGIN + ".groups.mutually_exclusive");
        if (mutex_groups!=null) {
            String[] groups = mutex_groups.split(",");
            for(String group: groups) {
                group = group.trim();
                if (pluginsPerGroup.get(group)!=null && pluginsPerGroup.get(group).size()>1) {
                    throw new Exception("Group " + group + " is configured as mutually exclusive, but there are multiple enabled plugins in this group");
                }
            }
        }
    }

    private void initialisePluginsByGroup(String initOrder, Properties properties) throws Exception {
        String[] groups = initOrder.split(",");
        for(String group: groups) {
            group = group.trim();

            if (pluginsPerGroup.containsKey(group)) {
                for(Class<? extends ApplicationPlugin> clazz: pluginsPerGroup.get(group)) {
                    initialisePlugin(clazz, properties);
                }
            }

            pluginsPerGroup.remove(group);
        }
    }

    private boolean classEnabled(Class<? extends ApplicationPlugin> clazz, Properties properties) {
        ApplicationPluginParameters note = (ApplicationPluginParameters)clazz.getAnnotation(ApplicationPluginParameters.class);
        String feature = ApplicationPlugin.APPLICATION_PLUGIN + "." + note.configName() + ".enabled";
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

    private void initialisePlugin(Class<? extends ApplicationPlugin> clazz, Properties properties) {
        //skip if already initialised
        if (plugins.containsKey(clazz)) return;

        //(2) look at the config for which ones should be initialised
        ApplicationPlugin fp = null;
        try {
            fp = clazz.newInstance();
            fp.initialise(properties);
            plugins.put(clazz, fp);
        } catch (InstantiationException e) {
            LOG.error("Unable to instantiate plugin for class " + clazz.getName());
        } catch (IllegalAccessException e) {
            LOG.error("Unable to instantiate plugin for class " + clazz.getName());
        }

    }
}
