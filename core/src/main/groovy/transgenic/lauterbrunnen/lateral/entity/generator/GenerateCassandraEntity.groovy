package transgenic.lauterbrunnen.lateral.entity.generator

import org.apache.velocity.Template
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.VelocityEngine
import org.apache.velocity.runtime.RuntimeConstants
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader
import transgenic.lauterbrunnen.lateral.domain.DomainProtoManager
import transgenic.lauterbrunnen.lateral.domain.Transient
import transgenic.lauterbrunnen.lateral.domain.generator.GenerateConverterName
import transgenic.lauterbrunnen.lateral.domain.validation.Validate

import java.lang.annotation.Annotation
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.util.function.Consumer

/**
 * Created by stumeikle on 01/07/20.
 */
class GenerateCassandraEntity {

    private String implPackage;
    private String jpaEntityPackage;
    private Properties properties;
    private String idFieldName;
    private String basePath;
    private String diContext;
    private Map<String, String>  idFields = new HashMap<>();
    private Map<String, String> idFieldNames = new HashMap<>();
    private DomainProtoManager domainProtoManager;
    private String entityName;

    public void setDomainProtoManager(DomainProtoManager domainProtoManager) {
        this.domainProtoManager=domainProtoManager;
    }
    public void setImplPackage( String implPackage ) {
        this.implPackage = implPackage;
    }
    public void setJpaEntityPackage( String jpaEntityPackage ) {
        this.jpaEntityPackage = jpaEntityPackage;
    }
    public void setProperties(Properties properties) {
        this.properties = properties;
    }
    public void setIdFieldName(String idFieldName) {
        this.idFieldName = idFieldName;
    }
    public void setIdFields(Map<String, String> idFields) {
        this.idFields = idFields;
    }
    public void setIdFieldNames(Map<String, String> idFields) {
        this.idFieldNames = idFields;
    }
    public void setBasePath(String basePath) {
        this.basePath=basePath;
    }
    public void setDiContext(String diContext) {
        this.diContext= diContext;
    }

    private Set<GenerateConverterName> converterNames;
    private Class implClass;

    public void generate(Class proto, Class impl) {

        //we're going to write the entity and the transformer here together
        //as we have all the info for the transformer here in one place too

        entityName = domainProtoManager.getEntityName(proto) + "Entity";
        converterNames = new HashSet<>();

        //Use velocity this time
        // (1) add entries to the properties to convert java types to cassandra-aligned-java types
        // IE UniqueID -> UUID
        // List -> Set (*unless overridden)
        // Perhaps that's all for now
        addTypeConvertersToProperties();

        VelocityEngine ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        ve.init();
        Template t = ve.getTemplate("CassandraEntity.vtl");
        VelocityContext context = new VelocityContext();

        context.put("jpaEntityPackage", jpaEntityPackage);
        context.put("diContext", diContext);
        context.put("tableName", domainProtoManager.getEntityName(proto));
        context.put("entityName", domainProtoManager.getEntityName(proto)+"Entity");

        implClass = impl;
        String tableDefinition = createTableDescription();

        context.put("cassandraTableDefinition", tableDefinition);
        context.put("fieldDefinitions", createFieldDefinitions());
        context.put("gettersAndSetters", createGettersAndSetters());

        StringWriter writer = new StringWriter();
        t.merge(context,writer);

        def fn = basePath + "/"+  jpaEntityPackage.replaceAll("\\.","/") + "/" + entityName + ".java";
        println "Writing " + fn;
        def output = new File(fn);

        output << writer.toString();

        // Now generate the transformer class too
        context.put("transform2EntityFromImpl", createTransform2EntityFromImplMethod(domainProtoManager.getEntityName(proto) + "Entity", proto));
        context.put("transform2ImplFromEntity", createTransform2ImplFromEntityMethod(domainProtoManager.getEntityName(proto) + "Entity", proto));

        def transformerfn = basePath + "/"+jpaEntityPackage.replaceAll("\\.","/") + "/" + entityName + "Transformer.java";
        println "Writing " + transformerfn;
        def transformer = new File(transformerfn);
        writer= new StringWriter();
        t = ve.getTemplate("CassandraEntityTransformer.vtl");
        t.merge(context,writer);
        transformer << writer.toString();

    }

    interface FieldProcessor {
        void processField(String fieldName, String type);
    }
    public interface StringInStringOut {
        String swap(String input);
    }

    class TableDescriptionProcessor implements FieldProcessor {
        final StringBuilder sb = new StringBuilder();
        public String getResult() {
            return sb.toString();
        }

        TableDescriptionProcessor() {
            sb.append("(");
        }

        void close() {
            sb.append("PRIMARY KEY (" + getColumnName(idFieldName) + "))");
        }

        @Override
        void processField(String fieldName, String cassandraType) {
            sb.append( getColumnName(fieldName) + " " + cassandraType + ", ");
        }
    }

    public String createTableDescription() {
        TableDescriptionProcessor tableDescriptionProcessor = new TableDescriptionProcessor();
        traverseImplFields(this.&swapTypeNameJava2Cassandra,tableDescriptionProcessor);

        tableDescriptionProcessor.close();

        return tableDescriptionProcessor.getResult();
    }

    class PrintFieldDefinitions implements FieldProcessor {
        final StringBuilder sb = new StringBuilder();

        @Override
        void processField(String fieldName, String type) {
            sb.append( "    private " + type + " " + fieldName + ";" + System.lineSeparator() );
        }

        public String getResult() {return sb.toString();}
    }

    public String createFieldDefinitions() {

        PrintFieldDefinitions printFieldDefinitions = new PrintFieldDefinitions();
        traverseImplFields(this.&swapTypeNameJava2CassandraAlignedJava, printFieldDefinitions);
        return printFieldDefinitions.getResult();
    }

    class GetterAndSetterProcessor implements FieldProcessor {

        final StringBuilder sb = new StringBuilder();
        private boolean first=true;

        public String getResult() {
            return sb.toString();
        }

        @Override
        void processField(String fieldName, String cassandraAlignedJavaType) {

            if (first) {
                sb.append(System.lineSeparator());
            }
            first=false;

            //Getter
            if (fieldName.equals(idFieldName)) {
                sb.append("    @PartitionKey"+ System.lineSeparator());
            }
            sb.append("    @Column(name=\"" + getColumnName(fieldName) + "\")" + System.lineSeparator());
            sb.append("    public " + cassandraAlignedJavaType + " get" + firstUpper(fieldName) + "() { return this." + fieldName + ";}" + System.lineSeparator());
            sb.append(System.lineSeparator());

            //Setter
            sb.append("    public void set" + firstUpper(fieldName) + "( " + cassandraAlignedJavaType + " " + fieldName + ") { this." + fieldName + " = " + fieldName + ";}" + System.lineSeparator());
        }
    }

    private String firstUpper(String fieldName) {
        String firstUpperFN = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        return firstUpperFN;
    }

    public String createGettersAndSetters() {
        GetterAndSetterProcessor getterAndSetterProcessor = new GetterAndSetterProcessor();
        traverseImplFields(this.&swapTypeNameJava2CassandraAlignedJava, getterAndSetterProcessor);
        return getterAndSetterProcessor.getResult();
    }

    public void traverseImplFields( StringInStringOut typeConverter, FieldProcessor fieldProcessor) {
        for(Field field : implClass.getDeclaredFields()) {
            if (field.getModifiers() & Modifier.TRANSIENT) continue;
            if (field.getAnnotation(Transient.class) != null) continue;

            String type = swapType(field.getGenericType(), typeConverter);
            fieldProcessor.processField(field.getName(), type);
        }
    }

    public String swapType(Type type, StringInStringOut swapTypeMethod ) {
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

    private static Map<String, String> caj2c = new HashMap<>();
    static {
        caj2c.put("Integer", "int");
        caj2c.put("Long", "bigint");
        caj2c.put("Float", "float");
        caj2c.put("Double", "double");
        caj2c.put("Boolean", "boolean");
        caj2c.put("java.lang.Integer", "int");
        caj2c.put("java.lang.Long", "bigint");
        caj2c.put("java.lang.Float", "float");
        caj2c.put("java.lang.Double", "double");
        caj2c.put("java.lang.Boolean", "boolean");
        caj2c.put("java.lang.String", "text");
        caj2c.put("java.util.UUID", "uuid");
        caj2c.put("java.util.List", "list");
        caj2c.put("java.util.Map", "map");
        caj2c.put("java.util.Set", "set");
        caj2c.put("java.math.BigDecimal", "decimal");

    }

    public String swapTypeNameJava2Cassandra(String fieldName) {

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


    //-------------------------------------------------------

    public String getColumnName(String fieldName) {
        fieldName = fieldName.replaceAll(/([a-z])([A-Z])/) { all, first, second ->
            first + "_" + second
        }

        return fieldName.toUpperCase();
    }


    private void addTypeConvertersToProperties() {
        properties.put("entity.swap.type.java.util.List","java.util.Set");
        properties.put("entity.swap.type.transgenic.lauterbrunnen.lateral.domain.UniqueId", "java.util.UUID");
    }

    String createTransform2EntityFromImplMethod(def entityName, def proto) {
        StringBuilder sb = new StringBuilder();

        sb << "    public static void transform(" << entityName << " entity, " <<
                implPackage << "." << proto.getSimpleName() << "Impl impl) {" << System.lineSeparator();
        for(Field field : implClass.getDeclaredFields()) {
            if (field.getModifiers() & Modifier.TRANSIENT) continue;
            if (field.getAnnotation(Transient.class) != null) continue;

            String firstUpper = firstUpper(field.getName());
            createSetLogicImplToEntity(sb, field, firstUpper);
        }
        sb << "    }";

        return sb.toString();
    }

    String createTransform2ImplFromEntityMethod(def entityName, def proto) {
        StringBuilder sb = new StringBuilder();

        String validationException = containsValidatedField(proto) ? " throws ValidationException" : "";

        sb << "    public static void transform(" << implPackage << "." << proto.getSimpleName() << "Impl impl," <<
                entityName << " entity)" << validationException << " {" << System.lineSeparator();

        for(Field field : implClass.getDeclaredFields()) {
            if (field.getModifiers() & Modifier.TRANSIENT) continue;
            if (field.getAnnotation(Transient.class) != null) continue;

            String firstUpper = firstUpper(field.getName());
            createSetLogicEntityToImpl(sb, field, firstUpper);
        }
        sb << "    }";

        return sb.toString();
    }

    private boolean containsValidatedField(Class proto) {
        for(Field field: proto.getDeclaredFields()) {
            Annotation[] notes = field.getAnnotations();
            for (Annotation note: notes) {
                if (note.annotationType().getName().equals(Validate.class.getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    // Methods for the transformer
    void createSetLogicImplToEntity(def transformer, Field field, String firstUpperFN) {
        Type type = field.getGenericType();

        //need to transform the fieldname
        String fufn = "get" + firstUpperFN;
        if (field.getType().getTypeName().equalsIgnoreCase("Boolean"))
        {
            if (field.getName().startsWith("is"))
                fufn = field.getName();
        }

        String fieldName = "impl." + fufn + "()";

        //prefix need to be controlled by annotations TODO
        transformer << "        ";
        if (!field.getType().isPrimitive())
            transformer << "if (impl." << fufn << "()!=null) "
        transformer << "entity.set" + firstUpperFN + "("

        recurseSetLogicImplToEntity(transformer, type, fieldName);

        transformer << ");" << System.lineSeparator();
    }

    private void recurseSetLogicImplToEntity(def transformer, Type type, String fieldName) {

        String entityType = swapType(type, this.&swapTypeNameJava2CassandraAlignedJava);

        String typeName= type.getTypeName();
        if (type instanceof ParameterizedType) {
            typeName = ((ParameterizedType)type).getRawType().getTypeName();
        }

        if (typeName.contains(implPackage)) {
            String ref = type.getTypeName() + "Reference";

            //sometimes the repository id is eg unique id but we need to convert it to eg byte[]
            String reptype =idFields.get(type.getTypeName());
            String converter = properties.get("entity.type.converter." + reptype);
            if (converter!=null) {
                //transformer << converter;

                //getting pretty hideous
                //we need to
                //(1) establish the name of a linking method for the conversion
                //(2) add the name to a set of such names
                //(3) use that method plus the config to convert the type
                GenerateConverterName converterName = GenerateConverterName.createHook( reptype, entityType );

                //add to the set of known converternames
                converterNames.add(converterName);
                transformer << converterName.getConverterMethodName() << "( " <<
                        "((" + ref + ")" + fieldName + ").getRepositoryId()" << ", " <<
                        converter << ")";

            } else {
                //no conversion needed
                transformer << "((" + ref + ")" + fieldName + ").getRepositoryId()";
            }

        } else {

            if (type instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) type;
                String rawname = pt.getRawType().getTypeName();

                handleListImplToEntity(transformer, rawname, fieldName, pt);
                handleMapImplToEntity( transformer, rawname, fieldName, pt);

            }
            else {
                String converter = properties.get("entity.type.converter." + type.getTypeName());
                if (converter != null) {
                    //we need the type associated with fieldName ...
                    GenerateConverterName converterName = GenerateConverterName.createHook( type.getTypeName(), entityType );
                    converterNames.add(converterName);
                    transformer << converterName.getConverterMethodName() << "( " <<
                            fieldName << ", " << converter << ")";
                } else {
                    transformer << fieldName;
                }
            }
        }
    }

    private void handleListImplToEntity( def transformer, String rawName, String fieldName, ParameterizedType parameterizedType) {
        try {

            Class c = Class.forName(rawName);
            boolean list = false;
            if (List.class.getName().equals(c.getName())) list = true;
            for(Class iface: c.getInterfaces()) {
                if (List.class.getName().equals(iface.getName())) {
                    list = true;
                    break;
                }
            }

            if (list) {
                //preamble
                transformer << fieldName << " == null ? null : "+ fieldName + ".stream().map( item -> " << System.lineSeparator();
                transformer << "            ";

                //then recurse into the type
                for (Type t : parameterizedType.getActualTypeArguments()) {
                    recurseSetLogicImplToEntity(transformer,t, "item");
                }

                //then postamble
                String collector = getCollector("java.util.List");
                transformer << System.lineSeparator() << "        ).collect(" + collector +")"

            }

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private String getCollector(String implFieldType) {

        String entityFieldType = swapTypeNameJava2CassandraAlignedJava(implFieldType);
        if ("java.util.List".equals(entityFieldType))  {
            return "Collectors.toList()";
        }
        if ("java.util.Set".equals(entityFieldType)) {
            return "Collectors.toSet()";
        }

        return "/* Support for additional types needed */";

    }

    private void handleMapImplToEntity( def transformer, String rawName, String fieldName, ParameterizedType parameterizedType) {
        try {
            Class c = Class.forName(rawName);
            boolean map = false;
            if (Map.class.getName().equals(c.getName())) map = true;
            for(Class iface: c.getInterfaces()) {
                if (Map.class.getName().equals(iface.getName())) {
                    map = true;
                    break;
                }
            }

            if (map) {
                //preamble
                transformer << fieldName + " ==null ? null : " << fieldName + ".entrySet().stream().collect(Collectors.toMap( " << System.lineSeparator()
                transformer << "            e -> "

                //then recurse into the type
                Type keytype = parameterizedType.getActualTypeArguments()[0];
                recurseSetLogicImplToEntity(transformer, keytype, "e.getKey()");
                transformer << "," << System.lineSeparator()
                transformer << "            e-> ";
                Type valuetype = parameterizedType.getActualTypeArguments()[1];
                recurseSetLogicImplToEntity(transformer, valuetype,"e.getValue()");

                //then postamble
                transformer << "))"

            }

        } catch(Exception e) {}
    }

    //Field is the IMPL field here, not the entity field
    private void createSetLogicEntityToImpl(def transformer, Field field, String firstUpperFN) {
        Type type = field.getGenericType();

        //need to transform the fieldname
        String fufn = "get" + firstUpperFN;
        if (field.getType().getTypeName().equalsIgnoreCase("Boolean"))
        {
            //fufn = field.getName();
            firstUpperFN = firstUpperFN.replaceFirst("Is", "");
        }

        String fieldName = "entity." + fufn + "()";

        //prefix need to be controlled by annotations TODO
        transformer << "        ";
        if (!field.getType().isPrimitive())
            transformer << "if (entity." << fufn << "()!=null) "
        transformer << "impl.set" + firstUpperFN + "("

        recurseSetLogicEntityToImpl(transformer, type, fieldName);

        transformer << ");" << System.lineSeparator();
    }

    private void recurseSetLogicEntityToImpl(def transformer, Type type, String fieldName) {

        String entityType = swapType(type, this.&swapTypeNameJava2CassandraAlignedJava);

        String typeName= type.getTypeName();
        if (type instanceof ParameterizedType) {
            typeName = ((ParameterizedType)type).getRawType().getTypeName();
        }

        //Different here. We need to look at the type in the Impl not the type in the entity
        //to know if we need to convert, eg, byte[] to new ObjectReference(byte[] bb)
        if (typeName.contains(implPackage)) {
            String ref = type.getTypeName() + "Reference";

            //(2) fine if there is a conversion then let's convert
            //sometimes the repository id is eg unique id but we need to convert it to eg byte[]
            String reptype = idFields.get(type.getTypeName());
            String converter = properties.get("entity.type.reverse.converter." + reptype);
            if (converter!=null) {
                //we need to
                //(1) establish the name of a linking method for the conversion
                //(2) add the name to a set of such names
                //(3) use that method plus the config to convert the type
                GenerateConverterName converterName = GenerateConverterName.createHook( entityType, reptype);

                //add to the set of known converternames
                converterNames.add(converterName);
                transformer << "new " << ref << "( "
                transformer << converterName.getConverterMethodName() << "( " <<
                        fieldName + ", " <<
                        converter << "))";

            } else {
                //(1) if there is no conversion needed we just do
                // eg impl.setTeacher( new TeacherReference( entity.getTeacher() ) )
                transformer << "new " << ref << "( " <<  fieldName << " )"
            }

        } else {

            if (type instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) type;
                String rawname = pt.getRawType().getTypeName();

                handleListEntityToImpl(transformer, rawname, fieldName, pt);
                handleMapEntityToImpl( transformer, rawname, fieldName, pt);

            }
            else {
                String converter = properties.get("entity.type.reverse.converter." + type.getTypeName());
                if (converter != null) {
                    //we need the type associated with fieldName ...
                    GenerateConverterName converterName = GenerateConverterName.createHook( entityType, type.getTypeName() );
                    converterNames.add(converterName);
                    transformer << converterName.getConverterMethodName() << "( " <<
                            fieldName << ", " << converter << ")";
                } else {
                    transformer << fieldName;
                }
            }
        }
    }

    private void handleListEntityToImpl( def transformer, String rawName, String fieldName, ParameterizedType parameterizedType) {
        try {

            Class c = Class.forName(rawName);
            boolean list = false;
            if (List.class.getName().equals(c.getName())) list = true;
            for(Class iface: c.getInterfaces()) {
                if (List.class.getName().equals(iface.getName())) {
                    list = true;
                    break;
                }
            }

            if (list) {
                //preamble
                transformer << fieldName << " == null ? null : "+ fieldName + ".stream().map( item -> " << System.lineSeparator();
                transformer << "            ";

                //then recurse into the type
                for (Type t : parameterizedType.getActualTypeArguments()) {
                    recurseSetLogicEntityToImpl(transformer,t, "item");
                }

                //then postamble
                transformer << System.lineSeparator() << "        ).collect(Collectors.toList())"

            }

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void handleMapEntityToImpl( def transformer, String rawName, String fieldName, ParameterizedType parameterizedType) {
        try {
            Class c = Class.forName(rawName);
            boolean map = false;
            if (Map.class.getName().equals(c.getName())) map = true;
            for(Class iface: c.getInterfaces()) {
                if (Map.class.getName().equals(iface.getName())) {
                    map = true;
                    break;
                }
            }

            if (map) {
                //preamble
                transformer << fieldName + " ==null ? null : " << fieldName + ".entrySet().stream().collect(Collectors.toMap( " << System.lineSeparator()
                transformer << "            e -> "

                //then recurse into the type
                Type keytype = parameterizedType.getActualTypeArguments()[0];
                recurseSetLogicEntityToImpl(transformer, keytype, "e.getKey()");
                transformer << "," << System.lineSeparator()
                transformer << "            e-> ";
                Type valuetype = parameterizedType.getActualTypeArguments()[1];
                recurseSetLogicEntityToImpl(transformer, valuetype,"e.getValue()");

                //then postamble
                transformer << "))"

            }

        } catch(Exception e) {}
    }
}
