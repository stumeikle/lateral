package transgenic.lauterbrunnen.lateral.persist.hazelcast.generator

import org.apache.velocity.Template
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.VelocityEngine
import org.apache.velocity.runtime.RuntimeConstants
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader

/**
 * Created by stumeikle on 01/12/16.
 */
class GenerateHCAdminEndpoint {
    String basePath;
    String implPackage;
    String cachePackage;
    String entityPackage;

    public void generate(Class proto) {

        VelocityEngine ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        ve.init();
        Template t = ve.getTemplate("EntityAdminEndpoint.vtl");
        VelocityContext context = new VelocityContext();
        context.put("cachePackage", cachePackage);
        context.put("implPackage",  implPackage);
        context.put("entityName", proto.getSimpleName());
        String lcname = proto.getSimpleName().substring(0,1).toLowerCase() + proto.getSimpleName().substring(1);
        context.put("lcEntityName", lcname);

        //we need to convert the incomind string-id into the cache-id
        //not sure how to do this in the general case
        //as don't really want more converters in the generate.properties


        StringWriter writer = new StringWriter();
        t.merge(context,writer);

        def fn = basePath + "/" + cachePackage.replaceAll("\\.", "/") + "/" + proto.getSimpleName() + "AdminEndpoint.java";
        //or connector or what
        println "Writing " + fn;
        def output = new File(fn);

        output << writer.toString();
    }
}
