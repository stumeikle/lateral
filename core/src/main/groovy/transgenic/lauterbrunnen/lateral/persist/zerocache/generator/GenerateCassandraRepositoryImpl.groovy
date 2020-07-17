package transgenic.lauterbrunnen.lateral.persist.zerocache.generator

import org.apache.velocity.Template
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.VelocityEngine
import org.apache.velocity.runtime.RuntimeConstants
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader
import transgenic.lauterbrunnen.lateral.domain.DomainProtoManager
import transgenic.lauterbrunnen.lateral.domain.OptimisticLocking
import transgenic.lauterbrunnen.lateral.domain.validation.Validate

import java.lang.annotation.Annotation
import java.lang.reflect.Field

/**
 * Created by stumeikle on 15/07/20.
 */
class GenerateCassandraRepositoryImpl {

    def domainGeneratedPackage;
    String outputPackage;
    def cacheZeroGeneratedPackage;
    def entityGeneratedPackage;
    def idFieldType;
    def idTransformer;
    def basePath;
    def dbIdType;
    def diContext;
    def idFieldNames;
    def cassandraIdTypeForJava;
    DomainProtoManager domainProtoManager;
    private ClassLoader classLoader;

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    def generate(def proto) {

        //check the annotations on the class for optimistic locking
        boolean optimisticLocking = false;
        //Class implClass = classLoader.loadClass(proto.getName()+"Impl");
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Class implClass = null;

        try
        {
            implClass = loader.loadClass(domainGeneratedPackage + "." + proto.getSimpleName()+"Impl");//Class.forName(domainGeneratedPackage + "." + proto.getSimpleName()+"Impl");
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        for(Annotation note: implClass.getAnnotations()) {
            if(note.annotationType().getName().equals(OptimisticLocking.class.getName())) optimisticLocking=true;
        }
        boolean sequencesPresent=false;
        boolean validationPresent=false;
        List<String> sequenceFields = new ArrayList<>();
        for(Field f: implClass.getDeclaredFields()) {
            boolean found=false;
            for(Annotation note: f.getAnnotations()) {
                //println "Checking annotation " + note.annotationType().getName();
                if (note.annotationType().getName().equals(transgenic.lauterbrunnen.lateral.domain.Sequence.class.getName())) {
                    found=true; break;
                }
            }
            if (found) {
                sequencesPresent=true;
                String fieldName = f.getName();
                fieldName = fieldName.substring(0,1).toUpperCase() + fieldName.substring(1);
                sequenceFields.add(fieldName);
            }
        }

        for(Field field: proto.getDeclaredFields()) {
            Annotation[] notes = field.getAnnotations();
            for(Annotation note: notes) {
                if (note.annotationType().getName().equals(Validate.class.getName())) {
                    validationPresent=true;break;
                }
            }
        }

        //Foreach entity in protos ....

        VelocityEngine ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        ve.init();
        Template t = ve.getTemplate("CassandraZCRepositoryImpl.vtl");
        VelocityContext context = new VelocityContext();
        context.put("domainGeneratedPackage", domainGeneratedPackage);
        context.put("outputPackage", outputPackage);
        context.put("cacheZeroGeneratedPackage", cacheZeroGeneratedPackage);
        context.put("entityGeneratedPackage", entityGeneratedPackage);
        context.put("repositoryIdTransformation", idTransformer);
        context.put("repositoryIdType", idFieldType);
        context.put("entity", proto.getSimpleName());
        context.put("JPAentity", domainProtoManager.getEntityName(proto) + "Entity");
        context.put("dbIdType", dbIdType);
        context.put("sequencesPresent", sequencesPresent);
        context.put("sequenceFields", sequenceFields);
        context.put("throwValidationException", validationPresent ? "throws ValidationException": "");
        context.put("diContext", diContext);

        String name = proto.getName().replace(entityGeneratedPackage, domainGeneratedPackage);
        String idFieldName = idFieldNames.get(name);
        context.put("idColumnName", getColumnName(idFieldName));
        context.put("cassandraIdTypeForJava", cassandraIdTypeForJava); //TODO this wont work with parameterised types


        String lcentity = proto.getSimpleName();
        lcentity = lcentity.substring(0,1).toLowerCase()+lcentity.substring(1);
        context.put("lcentity", lcentity);

        //entity, lcentity
        //repositoryIdType , repositoryIdTransformation
        //dbIdType
        StringWriter writer = new StringWriter();
        t.merge(context,writer);

        def fn = basePath + "/" + outputPackage.replaceAll("\\.","/") + "/ZC" + proto.getSimpleName() + "RepositoryImpl.java";
        println "Writing " + fn;
        def output = new File(fn);

        output << writer.toString();
    }

    public String getColumnName(String fieldName) {
        fieldName = fieldName.replaceAll(/([a-z])([A-Z])/) { all, first, second ->
            first + "_" + second
        }

        return fieldName.toUpperCase();
    }


}
