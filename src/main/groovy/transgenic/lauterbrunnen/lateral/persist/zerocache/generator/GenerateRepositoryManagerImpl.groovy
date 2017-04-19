package transgenic.lauterbrunnen.lateral.persist.zerocache.generator

import org.apache.velocity.Template
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.VelocityEngine
import org.apache.velocity.runtime.RuntimeConstants
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader

/**
 * Created by stumeikle on 17/04/17.
 */
class GenerateRepositoryManagerImpl {

    def inputPackage;
    String outputPackage;
    def basePath;

    def generate(def protos) {
        VelocityEngine ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        ve.init();
        Template t = ve.getTemplate("ZCRepositoryManagerImpl.vtl");
        VelocityContext context = new VelocityContext();
        context.put("inputPackage", inputPackage);
        context.put("outputPackage", outputPackage);

        List<String>    classes = new ArrayList<>();
        for(Class proto: protos) {
            classes.add(proto.getSimpleName());
        }
        context.put("domainObjects", classes);

        StringWriter writer = new StringWriter();
        t.merge(context,writer);

        def fn = basePath + "/" + outputPackage.replaceAll("\\.","/") + "/ZCRepositoryManagerImpl.java";
        println "Writing " + fn;
        def output = new File(fn);

        output << writer.toString();
    }
}
