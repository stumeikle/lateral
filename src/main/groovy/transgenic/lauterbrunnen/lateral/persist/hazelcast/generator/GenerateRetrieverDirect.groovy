package transgenic.lauterbrunnen.lateral.persist.hazelcast.generator

import org.apache.velocity.Template
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.VelocityEngine
import org.apache.velocity.runtime.RuntimeConstants
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader
import transgenic.lauterbrunnen.lateral.domain.generator.GenerateConverterName

import java.lang.reflect.Type

/**
 * Created by stumeikle on 06/11/16.
 */
class GenerateRetrieverDirect {

    protected String basePath;
    protected String implPackage;
    protected String cachePackage;
    protected String entityPackage;
    private Map<String, String>  idFields = new HashMap<>();
    private Properties properties;

    String getBasePath() {
        return basePath
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

    String getCachePackage() {
        return cachePackage
    }

    void setCachePackage(String cachePackage) {
        this.cachePackage = cachePackage
    }

    void setEntityPackage(String entityPackage) {
        this.entityPackage = entityPackage
    }

    public void setIdFields(Map<String, String> idFields) {
        this.idFields = idFields;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public void generate(Class proto) {
        VelocityEngine ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        ve.init();
        Template t = ve.getTemplate("RetrieverImplDirect.vtl");
        VelocityContext context = new VelocityContext();
        context.put("cachePackage", cachePackage);
        context.put("entityName", proto.getSimpleName());
        context.put("entityPackage", entityPackage);
        context.put("domainGeneratedPackage", implPackage);
        String lcName = proto.getSimpleName().substring(0,1).toLowerCase() + proto.getSimpleName().substring(1);
        context.put("lcEntityName", lcName);

        //figure out if we need to convert the cache key to a db key
        context.put("convertCacheKeyToDbKey","key");
        context.put("importCacheKey","");
        context.put("convertDbKeysToCacheKeys", "(Iterable<Object>)" + lcName + "Entity.get()");
        String name = proto.getName().replace(entityPackage, implPackage);
        String idFieldType = idFields.get(name);
        if (properties.getProperty("entity.swap.type." + idFieldType)!=null) {
            GenerateConverterName converter = GenerateConverterName.createHook(idFieldType, properties.getProperty("entity.swap.type." + idFieldType));
            String convert = proto.getSimpleName() << "EntityTransformer." << converter.converterMethodName <<
                    "((" << idFieldType << ")key," << properties.getProperty("entity.type.converter." + idFieldType) <<
                    ")"
            context.put("convertCacheKeyToDbKey",convert);
            context.put("importCacheKey", "import " + idFieldType + ";");

            //also do the reverse case for the get all keys
            StringBuilder sb = new StringBuilder();
            sb.append("((List<");
            String dbtype = properties.getProperty("entity.swap.type." + idFieldType);
            sb.append(dbtype);
            sb.append(">)");
            sb.append(lcName);
            sb.append("Entity.get())."); sb.append(System.lineSeparator());
            sb.append("            stream()."); sb.append(System.lineSeparator());
            sb.append("            map(");sb.append(System.lineSeparator());
            sb.append("                dbkey ->");
            sb.append(proto.getSimpleName());
            sb.append("EntityTransformer.");
            GenerateConverterName revconverter = GenerateConverterName.createHook(properties.getProperty("entity.swap.type." + idFieldType),idFieldType );

            sb.append(revconverter.converterMethodName);
            sb.append("((");
            sb.append(dbtype);
            sb.append(")dbkey,");
            sb.append(properties.getProperty("entity.type.reverse.converter." + idFieldType));
            sb.append(")");sb.append(System.lineSeparator());
            sb.append("            ).");sb.append(System.lineSeparator());
            sb.append("            collect(Collectors.toList())");
            context.put("convertDbKeysToCacheKeys", sb.toString());
        }

        StringWriter writer = new StringWriter();
        t.merge(context,writer);

        def fn = basePath + "/" + cachePackage.replaceAll("\\.", "/") + "/" + proto.getSimpleName() + "RetrieverImplDirect.java";
        //or connector or what
        println "Writing " + fn;
        def output = new File(fn);

        output << writer.toString();

    }
}
