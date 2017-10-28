package transgenic.lauterbrunnen.lateral.persist.hazelcast.generator

import org.apache.velocity.Template
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.VelocityEngine
import org.apache.velocity.runtime.RuntimeConstants
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader

/**
 * Created by stumeikle on 29/11/16.
 */
class GeneratePersisterInterface {
    private String cachePackage;
    private String  basePath;

    String getCachePackage() {
        return cachePackage
    }

    void setCachePackage(String cachePackage) {
        this.cachePackage = cachePackage
    }

    void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    void generate(Class proto) {
        VelocityEngine ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        ve.init();
        Template t = ve.getTemplate("PersisterInterface.vtl");
        VelocityContext context = new VelocityContext();
        context.put("cachePackage", cachePackage);
        context.put("name", proto.getSimpleName());

        StringWriter writer = new StringWriter();
        t.merge(context,writer);

        def fn = basePath + "/" + cachePackage.replaceAll("\\.", "/") + "/" + proto.getSimpleName() + "Persister.java";
        //or connector or what
        println "Writing " + fn;
        def output = new File(fn);

        output << writer.toString();
    }
}
