package transgenic.lauterbrunnen.lateral.persist.hazelcast.generator

import org.apache.velocity.Template
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.VelocityEngine
import org.apache.velocity.runtime.RuntimeConstants
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader

import java.lang.reflect.Field

/**
 * Created by stumeikle on 28/05/19.
 */
class GenerateAdminEndpointManager extends GeneratePersister {

    public void generate(List<Class> protoclasses, Map<String, Field> idFields) {

        def fn = basePath + "/" + cachePackage.replaceAll("\\.", "/") + "/HCAdminEndpointManagerImpl.java";
        //or connector or what
        println "Writing " + fn;

        VelocityEngine ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        ve.init();
        Template t = ve.getTemplate("HCAdminEndpointManagerImpl.vtl");
        VelocityContext context = new VelocityContext();
        context.put("implPackage",  implPackage);

        ArrayList<String>   entityNames = new ArrayList<>();
        for (Class proto: protoclasses) {
            entityNames.add(proto.getSimpleName());
        }
        context.put("entityNames", entityNames);

        StringWriter writer = new StringWriter();
        t.merge(context,writer);
        def output = new File(fn);
        output << writer.toString();
    }
}
