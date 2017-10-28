package transgenic.lauterbrunnen.lateral.cache.zero.generator

import org.apache.velocity.Template
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.VelocityEngine
import org.apache.velocity.runtime.RuntimeConstants
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader

/**
 * Created by stumeikle on 17/04/17.
 */
class GenerateCommon {

    private String outputPackage;
    private String inputPackage;
    private String basePath;
    private String generatedDomainPackage;
    private boolean sequencesUsed;

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public void setGeneratedDomainPackage(String generatedDomainPackage) {
        this.generatedDomainPackage = generatedDomainPackage;
    }

    public void setOutputPackage(String outputPackage) {
        this.outputPackage = outputPackage;
    }

    public void setInputPackage(String inputPackage) {
        this.inputPackage = inputPackage;
    }


    public void setSequencesUsed(boolean sequencesUsed) {
        this.sequencesUsed = sequencesUsed;
    }

    public void generate(List<Class> repos) {
        VelocityEngine ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        ve.init();
        Template t = ve.getTemplate("ZCCommonRepositoryImpl.vtl");
        VelocityContext context = new VelocityContext();
        context.put("inputPackage", inputPackage);
        context.put("outputPackage", outputPackage);
        context.put("sequencesUsed", sequencesUsed);

        StringWriter writer = new StringWriter();
        t.merge(context,writer);

        def fn = basePath + "/" + outputPackage.replaceAll("\\.","/") + "/ZCCommonRepositoryImpl.java";
        println "Writing " + fn;
        def output = new File(fn);

        output << writer.toString();
    }
}
