package transgenic.lauterbrunnen.lateral.persist.hazelcast.generator

import org.apache.velocity.Template
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.VelocityEngine
import org.apache.velocity.runtime.RuntimeConstants
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader
import transgenic.lauterbrunnen.lateral.domain.DomainProtoManager

/**
 * Created by stumeikle on 28/11/16.
 */
class GenerateRetrieverInterface {

    private String cachePackage;
    private String  basePath;
    private DomainProtoManager domainProtoManager;
    private String diContext;

    void setDomainProtoManager(DomainProtoManager domainProtoManager1) {
        this.domainProtoManager = domainProtoManager1;
    }

    String getCachePackage() {
        return cachePackage
    }

    void setCachePackage(String cachePackage) {
        this.cachePackage = cachePackage
    }

    void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    void setDiContext(String diContext) {
        this.diContext = diContext;
    }

    void generate(Class proto) {
        VelocityEngine ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        ve.init();
        Template t = ve.getTemplate("RetrieverInterface.vtl");
        VelocityContext context = new VelocityContext();
        context.put("cachePackage", cachePackage);
        context.put("name", proto.getSimpleName());
        context.put("diContext", diContext);

        StringWriter writer = new StringWriter();
        t.merge(context,writer);

        def fn = basePath + "/" + cachePackage.replaceAll("\\.", "/") + "/" + proto.getSimpleName() + "Retriever.java";
        //or connector or what
        println "Writing " + fn;
        def output = new File(fn);

        output << writer.toString();
    }
}
