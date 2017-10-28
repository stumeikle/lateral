package transgenic.lauterbrunnen.lateral.persist.hazelcast.generator

import org.apache.velocity.Template
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.VelocityEngine
import org.apache.velocity.runtime.RuntimeConstants
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader

/**
 * Created by stumeikle on 28/11/16.
 */
class GenerateRetrieverRemote {

    String basePath;
    String cachePackage;
    String implPackage;

    def generate(Class proto) {
        VelocityEngine ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        ve.init();
        Template t = ve.getTemplate("RetrieverImplRemote.vtl");
        VelocityContext context = new VelocityContext();
        context.put("cachePackage", cachePackage);
        context.put("entityName", proto.getSimpleName());
        context.put("domainGeneratedPackage", implPackage);

        StringWriter writer = new StringWriter();
        t.merge(context,writer);

        def fn = basePath + "/" + cachePackage.replaceAll("\\.", "/") + "/" + proto.getSimpleName() + "RetrieverImplRemote.java";
        //or connector or what
        println "Writing " + fn;
        def output = new File(fn);

        output << writer.toString();
    }

}
