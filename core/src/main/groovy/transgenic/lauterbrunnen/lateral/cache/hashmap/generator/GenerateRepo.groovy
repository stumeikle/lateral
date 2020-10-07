package transgenic.lauterbrunnen.lateral.cache.hashmap.generator

import org.apache.velocity.Template
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.VelocityEngine
import org.apache.velocity.runtime.RuntimeConstants
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader
import transgenic.lauterbrunnen.lateral.domain.OptimisticLocking

import java.lang.annotation.Annotation
import java.lang.reflect.Field

/**
 * Created by stumeikle on 03/10/20.
 */
class GenerateRepo extends GenerateManager {

    private ClassLoader classLoader;

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public void generate(Class repo) {

        String repoName = repo.getSimpleName();
        String entityName = repoName.replace("Repository", "");
        String entityNameLc = entityName.substring(0,1).toLowerCase() + entityName.substring(1);

        //check the annotations on the class for optimistic locking
        boolean optimisticLocking = false;
        Class implClass = classLoader.loadClass(repo.getName().replace("Repository","Impl"));
        for(Annotation note: implClass.getAnnotations()) {
            if(note.annotationType().getName().equals(OptimisticLocking.class.getName())) optimisticLocking=true;
        }
        boolean sequencesPresent=false;
        List<String> sequenceFields = new ArrayList<>();
        for(Field f: implClass.getDeclaredFields()) {
            boolean found=false;
            for(Annotation note: f.getAnnotations()) {
                //println "Checking annotation " + note.annotationType().getName();
                if (note.annotationType().getName().equals(transgenic.lauterbrunnen.lateral.domain.Sequence.class.getName())) {
                    found=true;break;
                }
            }
            if (found) {
                sequencesPresent=true;
                String fieldName = f.getName();
                fieldName = fieldName.substring(0,1).toUpperCase() + fieldName.substring(1);
                sequenceFields.add(fieldName);
            }
        }

        //We need the type nane for the repositoryId field
        String repositoryIdType=null;

        //hmm.
        Class entityRef = classLoader.loadClass( repo.getName().replace("Repository", "Reference"));
        for( Field f: entityRef.getDeclaredFields()) {
            if ("repositoryId".equals(f.getName())) {
                repositoryIdType = f.getGenericType().getTypeName();
            }
        }

        //20170113 all change. use velocity here now
        VelocityEngine ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        ve.init();
        Template t = ve.getTemplate("HMRepositoryImpl.vtl");
        VelocityContext context = new VelocityContext();
        context.put("inputPackage", inputPackage);
        context.put("domainPackage", domainPackage);
        context.put("outputPackage", outputPackage);
        context.put("protoName", entityName);
        context.put("lcProtoName", entityNameLc);
        context.put("repoIdType", repositoryIdType);
        context.put("optimisticLocking", optimisticLocking);
        context.put("sequencesPresent", sequencesPresent);
        context.put("sequenceFields", sequenceFields);
        context.put("diContext", diContext);

        StringWriter writer = new StringWriter();
        t.merge(context,writer);

        def fn = basePath + "/" + outputPackage.replaceAll("\\.","/") + "/HM" + repoName + "Impl.java";
        println "Writing " + fn;
        def output = new File(fn);

        output << writer.toString();
    }
}
