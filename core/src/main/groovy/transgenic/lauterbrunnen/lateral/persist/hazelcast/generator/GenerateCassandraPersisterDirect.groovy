package transgenic.lauterbrunnen.lateral.persist.hazelcast.generator

import org.apache.velocity.Template
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.VelocityEngine
import org.apache.velocity.runtime.RuntimeConstants
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader
import transgenic.lauterbrunnen.lateral.domain.DomainProtoManager
import transgenic.lauterbrunnen.lateral.domain.generator.GenerateConverterName

/**
 * Created by stumeikle on 06/07/20.
 */
class GenerateCassandraPersisterDirect {

    protected String basePath;
    protected String implPackage;
    protected String cachePackage;
    protected String entityPackage;
    protected DomainProtoManager domainProtoManager;
    protected String diContext;
    protected Properties properties;
    protected String idFieldType;

    String getBasePath() {
        return basePath
    }

    void setDomainProtoManager(DomainProtoManager domainProtoManager1) {
        this.domainProtoManager = domainProtoManager1;
    }

    void setBasePath(String basePath) {
        this.basePath = basePath
    }

    String getImplPackage() {
        return implPackage
    }

    void setImplPackage(String implPackage) {
        this.implPackage = implPackage
    }

    void setDiContext(String diContext) {
        this.diContext = diContext;
    }

    String getCachePackage() {
        return cachePackage
    }

    void setCachePackage(String cachePackage) {
        this.cachePackage = cachePackage
    }

    void setEntityPackage(String entityPackage) {
        this.entityPackage = entityPackage
    }

    void setProperties(Properties properties) {
        this.properties = properties;
    }

    void setIdFieldType(String idFieldType) {
        this.idFieldType = idFieldType;
    }

    public void generate(Class proto) {

        //velocity
        VelocityEngine ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        ve.init();
        Template t = ve.getTemplate("CassandraPersisterImplDirect.vtl");
        VelocityContext context = new VelocityContext();

        context.put("cachePackage", cachePackage);
        context.put("domainGeneratedPackage", implPackage);
        String implName = proto.getSimpleName() + "Impl";
        context.put("implName",implName);
        context.put("diContext", diContext);
        context.put("entityPackage", entityPackage);
        context.put("protoSimpleName", proto.getSimpleName());
        String entityName = domainProtoManager.getEntityName(proto);
        context.put("entityName", entityName);
        context.put("importCacheKey", "import " + idFieldType + ";");
        context.put("implNameFirstLower", implName.substring(0,1).toLowerCase() + implName.substring(1) );
        context.put("entityNameFirstLower", entityName.substring(0,1).toLowerCase() + entityName.substring(1));


        if (properties.getProperty("entity.swap.type." + idFieldType)!=null) {
            GenerateConverterName converter = GenerateConverterName.createHook(idFieldType, properties.getProperty("entity.swap.type." + idFieldType));
            String convert = proto.getSimpleName() << "EntityTransformer." << converter.converterMethodName <<
                    "((" << idFieldType << ")key," << properties.getProperty("entity.type.converter." + idFieldType) <<
                    ")"
            context.put("convertCacheKeyToDbKey", convert);
        } else {
            context.put("convertCacheKeyToDbKey","key");
        }

        StringWriter writer = new StringWriter();
        t.merge(context,writer);
        def fn = basePath + "/" + cachePackage.replaceAll("\\.", "/") + "/" + proto.getSimpleName() + "PersisterImplDirect.java";
        println "Writing " + fn;
        def output = new File(fn);


        output << writer.toString();
    }
}