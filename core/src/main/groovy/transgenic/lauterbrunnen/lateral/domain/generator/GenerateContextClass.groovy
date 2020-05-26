package transgenic.lauterbrunnen.lateral.domain.generator

import org.apache.velocity.Template
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.VelocityEngine
import org.apache.velocity.runtime.RuntimeConstants
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader

/**
 * Created by stumeikle on 16/08/19.
 */
class GenerateContextClass extends GenerateRepo {

    public void generateContextClass() {
        VelocityEngine ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        ve.init();
        Template t = ve.getTemplate("Context.vtl");
        VelocityContext context = new VelocityContext();

        //diContext
        context.put("diContext", diContext);
        context.put("outputPackage", outputPackage);
        StringWriter writer = new StringWriter();
        t.merge(context, writer);

        def fn = basePath + "/" + outputPackage.replaceAll("\\.", "/") + "/" + diContext + "Context.java";
        println "Writing " + fn;
        def output = new File(fn);

        output << writer.toString();
    }
}
