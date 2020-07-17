package transgenic.lauterbrunnen.lateral.persist.zerocache.generator

import transgenic.lauterbrunnen.lateral.domain.DomainProtoManager
import transgenic.lauterbrunnen.lateral.domain.RepositoryId
import transgenic.lauterbrunnen.lateral.domain.UniqueId
import transgenic.lauterbrunnen.lateral.domain.generator.GenerateConverterName

import java.lang.annotation.Annotation
import java.lang.reflect.Field

/**
 * Created by stumeikle on 15/07/20.
 */
class GenerateCassandraZCPersist {

    def generatedSourcesPath;
    def propertyFile;
    def generateDirect;
    def classLoader;
    def diContext;

    //internal
    def implPackage;
    def idFields;
    def properties;

    public void generate() {
        //Get the config

        properties = new Properties();
        try {
            InputStream inputStream = new FileInputStream(propertyFile);
            properties.load(inputStream);
        } catch (Exception e) {
            println("Unable to load properties file.");
            System.exit(0);
        }

        addTypeConvertersToProperties();

        DomainProtoManager dpm = new DomainProtoManager(properties);
        implPackage = properties.get("domain.generated.package");
        String entityPackage = properties.get("entity.generated.package");
        String outputPackage = properties.get("persist.zerocache.generated.package");
        String cachePackage  = properties.get("cache.zero.generated.package");
        diContext = properties.get("lateral.di.context");
        def domainProtoManager = new DomainProtoManager(properties);
        def classes = domainProtoManager.getProtoClasses();

        def dbdumpbase = generatedSourcesPath;
        def generatedDir = dbdumpbase + "/" + outputPackage.replaceAll("\\.", "/") + "/";
        def dir = new File(generatedDir);
        dir.mkdirs();
        for (File file : dir.listFiles()) file.delete();

//        Map<String, String>
        idFields = new HashMap<>();
        Map<String, String> idFieldNames = new HashMap<>();

        if (generateDirect) {
            for (Class proto : classes) {
                //For each class we need to know the name of the field which represents the
                //repository id

                //Fields are from the prototype object
                List<Field> allFields = getAllFields(proto);
                Field idField = null;
                String repositoryIdFieldName = "repositoryId";
                for (Field field : allFields) {
                    Annotation[] notes = field.getAnnotations();
                    for (Annotation note : notes) {
                        if (note.annotationType().getName().equals(RepositoryId.class.getName())) {
                            idField = field;
                            repositoryIdFieldName = idField.getName();

                            String name = proto.getName().replace(entityPackage, implPackage);

                            //getting nuts
                            String fn = field.getType().getTypeName();
                            if (field.getType().isPrimitive()) {
                                fn = swapPrimitiveForNon(field.getType());
                            }

                            idFields.put(proto.getName(), fn);
                            idFieldNames.put(name, repositoryIdFieldName);
                        }
                    }
                }

                if (idField == null) {
                    repositoryIdFieldName = "repositoryId";

                    String name = proto.getName().replace(entityPackage, implPackage);
                    idFields.put(name, UniqueId.class.getName());
                    idFieldNames.put(name, repositoryIdFieldName);
                }
            }
        }

        for (Class proto : classes) {
            GenerateCassandraRepositoryImpl gri = new GenerateCassandraRepositoryImpl();

            gri.domainGeneratedPackage = implPackage;
            gri.cacheZeroGeneratedPackage = cachePackage;
            gri.entityGeneratedPackage = entityPackage;
            gri.outputPackage = outputPackage;
            gri.basePath = dbdumpbase;
            gri.domainProtoManager = dpm;
            gri.setClassLoader(classLoader);
            gri.setDiContext(diContext);
            gri.setIdFieldNames(idFieldNames);

            //GenerateConverterName
            String name = proto.getName().replace(entityPackage, implPackage);
            String idFieldType = idFields.get(name);
            String idTransformer = "id";
            gri.dbIdType = idFieldType;

            gri.setCassandraIdTypeForJava(swapTypeNameJava2CassandraRow(idFieldType));


            String converter = properties.get("entity.type.converter." + idFieldType);
            if (converter!=null) {
                //transformer << converter;

                //getting pretty hideous
                //we need to
                //(1) establish the name of a linking method for the conversion
                //(2) add the name to a set of such names
                //(3) use that method plus the config to convert the type
                String propswap = properties.get("entity.swap.type." + idFieldType);
                gri.dbIdType = propswap;
                GenerateConverterName converterName = GenerateConverterName.createHook(idFieldType, propswap);

                idTransformer = proto.getSimpleName() + "EntityTransformer." + converterName.converterMethodName +
                        "(id," + converter + ");";

            }
            gri.idFieldType = idFieldType;
            gri.idTransformer = idTransformer;
            gri.generate(proto);
        }

        GenerateRepositoryManagerImpl grmi = new GenerateRepositoryManagerImpl();
        grmi.inputPackage = implPackage;
        grmi.outputPackage = outputPackage;
        grmi.basePath = dbdumpbase;
        grmi.diContext = diContext;
        grmi.generate(classes);


    }

    protected List<Field> getAllFields(Class klass) {
        List<Field> retval = new ArrayList<>();
        Class sc = klass.getSuperclass();

        if (sc != null) {
            retval.addAll(getAllFields(sc));
        }

        retval.addAll(klass.getDeclaredFields());
        return retval;
    }


    def String swapPrimitiveForNon(Class prim) {
        if (prim.equals(Boolean.TYPE)) {
            return "Boolean";
        }
        if (prim.equals(Character.TYPE)) {
            return "Character";
        }
        if (prim.equals(Byte.TYPE)) {
            return "Byte";
        }
        if (prim.equals(Short.TYPE)) {
            return "Short";
        }
        if (prim.equals(Integer.TYPE)) {
            return "Integer";
        }
        if (prim.equals(Long.TYPE)) {
            return "Long";
        }
        if (prim.equals(Float.TYPE)) {
            return "Float";
        }
        if (prim.equals(Double.TYPE)) {
            return "Double";

        }
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

    private void addTypeConvertersToProperties() {
        properties.put("entity.swap.type.java.util.List","java.util.Set");
        properties.put("entity.swap.type.transgenic.lauterbrunnen.lateral.domain.UniqueId", "java.util.UUID");
        properties.put("entity.type.converter.transgenic.lauterbrunnen.lateral.domain.UniqueId", "UniqueId::convertToJavaUUID");
        properties.put("entity.type.reverse.converter.transgenic.lauterbrunnen.lateral.domain.UniqueId", "UniqueId::revertUuidToUniqueId");
    }

}
