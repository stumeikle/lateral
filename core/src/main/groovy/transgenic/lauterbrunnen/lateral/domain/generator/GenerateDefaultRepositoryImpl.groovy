package transgenic.lauterbrunnen.lateral.domain.generator

import org.apache.velocity.Template
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.VelocityEngine
import org.apache.velocity.runtime.RuntimeConstants
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader

/**
 * Created by stumeikle on 12/08/19.
 */
class GenerateDefaultRepositoryImpl extends GenerateRepo {

    public void generateDefaultRepositoryImpl() {
        VelocityEngine ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        ve.init();
        Template t = ve.getTemplate("DefaultRepositoryImpl.vtl");
        VelocityContext context = new VelocityContext();

        //diContext
//        context.put("inputPackage", inputPackage);
        context.put("diContext", diContext);
        context.put("outputPackage", outputPackage);

        List<String>    protoClassNames = new ArrayList<>();
        for(Class proto: prototypeClasses) {
            if (!proto.getSimpleName().startsWith("_"))
                protoClassNames.add(proto.getSimpleName());
        }
        context.put("prototypeClassNames", protoClassNames);

//        context.put("protoName", entityName);
//        context.put("lcProtoName", entityNameLc);
//        context.put("repoIdType", repositoryIdType);
//        context.put("optimisticLocking", optimisticLocking);
//        context.put("sequencesPresent", sequencesPresent);
//        context.put("sequenceFields", sequenceFields);

        StringWriter writer = new StringWriter();
        t.merge(context,writer);

        def fn = basePath + "/" + outputPackage.replaceAll("\\.","/") + "/DefaultRepositoryImpl.java";
        println "Writing " + fn;
        def output = new File(fn);

        output << writer.toString();
    }
}
