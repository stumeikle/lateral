package transgenic.lauterbrunnen.lateral.rest.generator

import org.apache.velocity.Template
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.VelocityEngine
import org.apache.velocity.runtime.RuntimeConstants
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader
import transgenic.lauterbrunnen.lateral.domain.RepositoryId
import transgenic.lauterbrunnen.lateral.domain.UniqueId

import java.lang.annotation.Annotation
import java.lang.reflect.Field

/**
 * Created by stumeikle on 11/11/16.
 */
class GenerateEndpoint {
    protected String outputPackage;
    protected String domainGeneratedPackage;
    protected List<Class> prototypeClasses;
    protected String basePath;
    private Map<String, Class> classMap = new HashMap<>();
    protected Properties properties;
    protected Field idField = null;

    public void setOutputPackage(String outputPackage) {
        this.outputPackage = outputPackage;
    }
    public void setPrototypeClasses(List<Class> classes) {
        this.prototypeClasses = classes;
        for(Class clazz: classes) {
            classMap.put(clazz.getName(), clazz);
        }
    }
    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }
    public void setProperties(Properties properties) {
        this.properties = properties;
    }
    public void setDomainGeneratedPackage(String dgp) {
        this.domainGeneratedPackage = dgp;
    }

    def void generate(Class proto) {

        //use velocity this time as the output is fairly static
        VelocityEngine ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        ve.init();
        Template t = ve.getTemplate("Endpoint.vtl");
        VelocityContext context = new VelocityContext();

        setIdField(proto);
        if (idField!=null) {
            context.put("repoIdType", idField.getType().getName());
            context.put("idFieldName", capitalizeFirst(idField.getName()));
        } else {
            context.put("idFieldName", "RepositoryId");
            context.put("repoIdType", UniqueId.class.getName());
        }


        context.put("domainGeneratedPackage", domainGeneratedPackage);
        context.put("restGeneratedPackage", outputPackage);

        //
        String restPath = properties.get("rest.path");
        if (restPath==null) restPath="/api";
        context.put("restPath", restPath);

        //try to pluralise the name
        String propPlural = properties.get("rest.pojo." + proto.getSimpleName().toLowerCase() + ".plural");
        if (propPlural!=null) {
            context.put("lcEntityNamePlural", propPlural);
        } else {
            context.put("lcEntityNamePlural", proto.getSimpleName().toLowerCase() +"s");
        }
        context.put("lcEntityName", proto.getSimpleName().toLowerCase());

        context.put("entityName", proto.getSimpleName());

        boolean json = true;
        boolean xml = true;
        if( "false".equalsIgnoreCase(properties.getProperty("rest.support.json"))) json = false;
        if( "false".equalsIgnoreCase(properties.getProperty("rest.support.xml")))  xml = false;
        context.put("jsonSupported", json);
        context.put("xmlSupported",  xml);

        String api_version = properties.getProperty("rest.version");context.put("lcEntityName", proto.getSimpleName().toLowerCase());
        String pojo_version = properties.getProperty("rest.pojo." + proto.getSimpleName().toLowerCase() + ".version");
        if (api_version == null) api_version = "1";
        if (pojo_version == null) pojo_version = api_version;
        pojo_version = pojo_version.replaceAll("\\.", "_");

        context.put("entityVersion", pojo_version);

        StringWriter writer = new StringWriter();
        t.merge(context,writer);

        //write out
        def className = proto.getSimpleName() + "Endpoint";
        def fn = basePath + "/" + outputPackage.replaceAll("\\.", "/") + "/" + className + ".java";
        println "Writing " + fn;
        def output = new File(fn);

        output << writer.toString();

    }

    private String capitalizeFirst(String string) {
        return string.substring(0,1).toUpperCase() + string.substring(1);
    }

    def setIdField(Class proto) {
        List<Field> allFields = getAllFields(proto);

        for (Field field : allFields) {

            Annotation[] notes = field.getAnnotations();
            for (Annotation note : notes) {
                if (note.annotationType().getName().equals(RepositoryId.class.getName())) {
                    idField = field;
                }
            }
        }
    }

    protected List<Field> getAllFields(Class klass) {
        List<Field> retval = new ArrayList<>();
        Class sc = klass.getSuperclass();

        if (sc!=null) {
            retval.addAll( getAllFields( sc ));
        }

        retval.addAll( klass.getDeclaredFields() );
        return retval;
    }
}
