package transgenic.lauterbrunnen.lateral.persist

import org.apache.velocity.Template
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.VelocityEngine
import org.apache.velocity.runtime.RuntimeConstants
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader

/**
 * Created by stumeikle on 01/09/19.
 */
class GenerateTransactionManagerImpl {

    def diContext;
    def persistGeneratedPackage;
    def persistenceUnit;
    def basePath;
    def domainGeneratedPackage;

    public void generate() {
        VelocityEngine ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        ve.init();
        Template t = ve.getTemplate("TransactionManagerImpl.vtl");
        VelocityContext context = new VelocityContext();

        context.put("diContext", diContext);
        context.put("persistGeneratedPackage", persistGeneratedPackage);
        context.put("persistenceUnit", persistenceUnit);
        context.put("domainGeneratedPackage", domainGeneratedPackage);

        StringWriter writer = new StringWriter();
        t.merge(context,writer);

        //def generatedDir = dbdumpbase + "/" + persistGeneratedPackage.replaceAll("\\.", "/") + "/";

        def fn = basePath + "/" + ((String)persistGeneratedPackage).replaceAll("\\.", "/") + "/" + "TransactionManagerImpl.java";
        //or connector or what
        println "Writing " + fn;
        def output = new File(fn);

        output << writer.toString();

    }
}
