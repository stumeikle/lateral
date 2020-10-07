package transgenic.lauterbrunnen.lateral.cache.hashmap.generator

import org.apache.velocity.Template
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.VelocityEngine
import org.apache.velocity.runtime.RuntimeConstants
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader

/**
 * Created by stumeikle on 03/10/20.
 */
class GenerateManager {
    protected String outputPackage;
    protected String inputPackage;
    String domainPackage;
    protected String basePath;
    protected String diContext;
    def Set<String> subPackages;

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public void setDiContext(String diContext) {
        this.diContext = diContext;
    }

    public void setOutputPackage(String outputPackage) {
        this.outputPackage = outputPackage;
    }

    public void setInputPackage(String inputPackage) {
        this.inputPackage = inputPackage;
    }

    class RepoTags {
        public String shortName;
        public String lcName;
        public String protoName;

        public String getShortName() { return this.shortName; }
        public String getLcName() { return this.lcName; }
        public String getProtoName(){ return this.protoName; }
    }

    public void generate(List<Class> repos) {

        VelocityEngine ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        ve.init();
        Template t = ve.getTemplate("HMRepositoryManager.vtl");
        VelocityContext context = new VelocityContext();
        context.put("inputPackage", inputPackage);
        context.put("outputPackage", outputPackage);

        List<RepoTags>  repoTagsList = new ArrayList<>(repos.size());
        for(Class repo: repos) {
            RepoTags repoTags = new RepoTags();
            repoTags.shortName=repo.getSimpleName();
            repoTags.lcName = repoTags.shortName.substring(0,1).toLowerCase() + repoTags.shortName.substring(1);
            repoTags.protoName = repoTags.shortName.replace("Repository","");
            repoTagsList.add(repoTags);
        }

        context.put("repos", repoTagsList);
        context.put("diContext", diContext);
        context.put("subPackages", subPackages);

        StringWriter writer = new StringWriter();
        t.merge(context,writer);

        def fn = basePath + "/" + outputPackage.replaceAll("\\.","/") + "/HMRepositoryManagerImpl.java";
        println "Writing " + fn;
        def output = new File(fn);

        output << writer.toString();

    }
}