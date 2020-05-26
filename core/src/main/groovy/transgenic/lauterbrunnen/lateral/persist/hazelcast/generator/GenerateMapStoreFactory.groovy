package transgenic.lauterbrunnen.lateral.persist.hazelcast.generator

import org.apache.velocity.Template
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.VelocityEngine
import org.apache.velocity.runtime.RuntimeConstants
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader

import java.lang.reflect.Field

/**
 * Created by stumeikle on 06/11/16.
 */
class GenerateMapStoreFactory extends GeneratePersister {

    boolean generateDirect;

    public void generate(List<Class> protoclasses) {

        VelocityEngine ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        ve.init();
        Template t = ve.getTemplate("MapStoreFactory.vtl");
        VelocityContext context = new VelocityContext();
        context.put("cachePackage", cachePackage);
        context.put("implPackage",  implPackage);
        context.put("generateDirect", generateDirect);
        context.put("diContext", diContext);
        Vector<String>      v = new Vector<>();
        for(Class c: protoclasses) {
            v.add(c.getSimpleName());
        }
        context.put("allNames", v);

        StringWriter writer = new StringWriter();
        t.merge(context,writer);

        def fn = basePath + "/" + cachePackage.replaceAll("\\.", "/") + "/HCMapStoreFactoryImpl.java";
        //or connector or what
        println "Writing " + fn;
        def output = new File(fn);

        output << writer.toString();
    }
}
