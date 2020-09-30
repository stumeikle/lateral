package transgenic.lauterbrunnen.lateral.persist.hazelcast.generator

import org.apache.velocity.Template
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.VelocityEngine
import org.apache.velocity.runtime.RuntimeConstants
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader
import transgenic.lauterbrunnen.lateral.domain.DomainProtoManager
import transgenic.lauterbrunnen.lateral.domain.generator.GenerateConverterName
import transgenic.lauterbrunnen.lateral.entity.generator.GenerateCassandraEntity

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * Created by stumeikle on 08/07/20.
 */
class GenerateCassandraRetrieverDirect {


    protected String basePath;
    protected String implPackage;
    protected String cachePackage;
    protected String entityPackage;
    protected DomainProtoManager domainProtoManager;
    private Map<String, String>  idFields = new HashMap<>();
    private Map<String, String>  idFieldNames = new HashMap<>();
    private Properties properties;
    private String diContext;

    String getBasePath() {
        return basePath
    }

    void setDomainProtoManager(DomainProtoManager domainProtoManager) {
        this.domainProtoManager = domainProtoManager;
    }

    void setBasePath(String basePath) {
        this.basePath = basePath
    }

    void setDiContext(String diContext){
        this.diContext=diContext;
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

    public void setIdFieldNames(Map<String, String> idFieldNames) {
        this.idFieldNames = idFieldNames;
    }


    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public void generate(Class proto) {
        addTypeConvertersToProperties();

        VelocityEngine ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        ve.init();
        Template t = ve.getTemplate("CassandraRetrieverImplDirect.vtl");
        VelocityContext context = new VelocityContext();
        context.put("cachePackage", cachePackage);

        String entityNameReduced = domainProtoManager.getEntityName(proto);
        entityNameReduced = entityNameReduced.replace(/Entity$/, "");
        context.put("entityName", entityNameReduced);
        context.put("entityPackage", entityPackage);
        context.put("domainGeneratedPackage", implPackage);
        String lcName = entityNameReduced.substring(0, 1).toLowerCase() + entityNameReduced.substring(1);
        context.put("lcEntityName", lcName);
        context.put("implName", proto.getSimpleName() + "Impl");
        String lcImpl = proto.getSimpleName() + "Impl";
        lcImpl = lcImpl.substring(0, 1).toLowerCase() + lcImpl.substring(1);
        context.put("lcImplName", lcImpl);
        context.put("protoName", proto.getSimpleName());
        context.put("diContext", diContext);

        String subPackage = domainProtoManager.getSubPackageForProto(proto.getSimpleName());
        if(!"".equals(subPackage)) subPackage = subPackage + ".";
        context.put("subPackage", subPackage);

        //figure out if we need to convert the cache key to a db key
        context.put("convertCacheKeyToDbKey","key");
        context.put("importCacheKey","");
        context.put("convertDbKeysToCacheKeys", "(Iterable<Object>)retval");
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
//            sb.append("((List<");
            String dbtype = properties.getProperty("entity.swap.type." + idFieldType);
//            sb.append(dbtype);
//            sb.append(">)");
//            sb.append("retval)."); sb.append(System.lineSeparator());
            // << seems we don't need this cast
            sb.append("retval.");sb.append(System.lineSeparator());

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

        //Need column name for idFieldName and the Cassandra type in java style
        String idFieldName = idFieldNames.get(name);
        context.put("idColumnName", getColumnName(idFieldName));
        context.put("cassandraIdTypeForJava", swapTypeNameJava2CassandraRow(idFieldType)); //TODO this wont work with parameterised types

        StringWriter writer = new StringWriter();
        t.merge(context,writer);

        def fn = basePath + "/" + cachePackage.replaceAll("\\.", "/") + "/" + proto.getSimpleName() + "RetrieverImplDirect.java";
        //or connector or what
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


    private void addTypeConvertersToProperties() {
        properties.put("entity.swap.type.java.util.List","java.util.Set");
        properties.put("entity.swap.type.transgenic.lauterbrunnen.lateral.domain.UniqueId", "java.util.UUID");
        properties.put("entity.type.converter.transgenic.lauterbrunnen.lateral.domain.UniqueId", "UniqueId::convertToJavaUUID");
        properties.put("entity.type.reverse.converter.transgenic.lauterbrunnen.lateral.domain.UniqueId", "UniqueId::revertUuidToUniqueId");
    }

    public String swapType(Type type, GenerateCassandraEntity.StringInStringOut swapTypeMethod ) {
        if (type instanceof ParameterizedType) {
            ParameterizedType p = (ParameterizedType) type;
            String retval = swapTypeMethod.swap(p.getRawType().getTypeName());
            retval += "<";
            //then do the parameters
            boolean first = true;
            for (Type t : p.getActualTypeArguments()) {
                if (!first) retval = retval + ",";
                retval = retval + swapType(t, swapTypeMethod);
                first = false;
            }
            retval += ">";
            return retval;
        }
        String retval = swapTypeMethod.swap(type.getTypeName());
        return retval;
    }

    public String swapTypeNameJava2CassandraRow(String fieldName) {

        String newFieldName = swapTypeNameJava2CassandraAlignedJava(fieldName);
        if (newFieldName.contains(implPackage)) {
            //in this case we need to swap it for the repository id field type
            //as it's a reference
            String urgName = idFields.get(fieldName);
            if (urgName!=null) {
                newFieldName = urgName;
            }
        }

        if (caj2c.containsKey(newFieldName)) return caj2c.get(newFieldName);
        return newFieldName;
    }

    private static Map<String, String> caj2c = new HashMap<>();
    static {
        caj2c.put("Integer", "Int");
        caj2c.put("Long", "Long");
        caj2c.put("Float", "Float");
        caj2c.put("Double", "Double");
        caj2c.put("Boolean", "Bool");
        caj2c.put("java.lang.Integer", "Int");
        caj2c.put("java.lang.Long", "Lomg");
        caj2c.put("java.lang.Float", "Float");
        caj2c.put("java.lang.Double", "Double");
        caj2c.put("java.lang.Boolean", "Bool");
        caj2c.put("java.lang.String", "String");
        caj2c.put("java.util.UUID", "UUID");
        caj2c.put("java.util.List", "List");
        caj2c.put("java.util.Map", "Map");
        caj2c.put("java.util.Set", "Set");
        caj2c.put("java.math.BigDecimal", "Decimal");

    }

    public String swapTypeNameJava2CassandraAlignedJava(String fieldName) {

        String newFieldName = fieldName;
        if (newFieldName.contains(implPackage)) {
            //in this case we need to swap it for the repository id field type
            //as it's a reference
            String urgName = idFields.get(fieldName);
            if (urgName!=null) {
                newFieldName = urgName;
            }
        }

        String propswap = properties.get("entity.swap.type." + newFieldName);
        if (propswap!=null) newFieldName = propswap;


        if ("int".equals(newFieldName)) newFieldName = "Integer";
        if ("long".equals(newFieldName)) newFieldName = "Long";
        if ("float".equals(newFieldName)) newFieldName = "Float";
        if ("double".equals(newFieldName)) newFieldName = "Double";
        if ("boolean".equals(newFieldName)) newFieldName= "Boolean"; //perhaps

        return newFieldName;
    }

}

