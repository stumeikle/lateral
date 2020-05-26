package transgenic.lauterbrunnen.lateral.rest.generator

import org.apache.velocity.Template
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.VelocityEngine
import org.apache.velocity.runtime.RuntimeConstants
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader

/**
 * Created by stumeikle on 11/11/16.
 */
class GenerateRestResource {

    protected String outputPackage;
    protected String basePath;
    protected String diContext;
    protected String domainGeneratedPackage;

    def setOutputPackage( p ) {
        this.outputPackage = p;
    }

    def setDiContext(String diContext) {
        this.diContext = diContext;
    }

    def setDomainGeneratedPackage(String domainGeneratedPackage) {
        this.domainGeneratedPackage = domainGeneratedPackage;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    def generate(List<Class> classes) {

        VelocityEngine ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        ve.init();
        Template t = ve.getTemplate("PluggableResource.vtl");
        VelocityContext context = new VelocityContext();

        context.put("restGeneratedPackage", outputPackage);
        context.put("diContext", diContext);
        context.put("domainGeneratedPackage", domainGeneratedPackage);

        StringBuilder   sb = new StringBuilder();
        boolean first = true;
        for(Class proto: classes) {
            if (!first) sb.append(" ,");
            sb.append(proto.getSimpleName());
            sb.append("Endpoint");
            sb.append(".class");
            first=false;
        }
        context.put("endpointClasses", sb.toString());

        StringWriter writer = new StringWriter();
        t.merge(context,writer);

        //write out
        def className = "PluggableResourceConfigImpl";
        def fn = basePath + "/" + outputPackage.replaceAll("\\.", "/") + "/" + className + ".java";
        println "Writing " + fn;
        def output = new File(fn);

        output << writer.toString();


    }
}
