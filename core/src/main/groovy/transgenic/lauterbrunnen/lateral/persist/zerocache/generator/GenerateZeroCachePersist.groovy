package transgenic.lauterbrunnen.lateral.persist.zerocache.generator

import transgenic.lauterbrunnen.lateral.domain.DomainProtoManager
import transgenic.lauterbrunnen.lateral.domain.RepositoryId
import transgenic.lauterbrunnen.lateral.domain.UniqueId
import transgenic.lauterbrunnen.lateral.domain.generator.GenerateConverterName

import java.lang.annotation.Annotation
import java.lang.reflect.Field

/**
 * Created by stumeikle on 17/04/17.
 */
class GenerateZeroCachePersist {

    def generatedSourcesPath;
    def propertyFile;
    def generateDirect;
    def classLoader;
    def diContext;

    public void generate() {
        //Get the config

        Properties properties = new Properties();
        try {
            InputStream inputStream = new FileInputStream(propertyFile);
            properties.load(inputStream);
        } catch (Exception e) {
            println("Unable to load properties file.");
            System.exit(0);
        }

        DomainProtoManager dpm = new DomainProtoManager(properties);
        String implPackage = properties.get("domain.generated.package");
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

        Map<String, String> idFields = new HashMap<>();
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
           GenerateRepositoryImpl gri = new GenerateRepositoryImpl();

            gri.domainGeneratedPackage = implPackage;
            gri.cacheZeroGeneratedPackage = cachePackage;
            gri.entityGeneratedPackage = entityPackage;
            gri.outputPackage = outputPackage;
            gri.basePath = dbdumpbase;
            gri.domainProtoManager = dpm;
            gri.setClassLoader(classLoader);
            gri.setDiContext(diContext);

            //GenerateConverterName
            String name = proto.getName().replace(entityPackage, implPackage);
            String idFieldType = idFields.get(name);
            String idTransformer = "id";
            gri.dbIdType = idFieldType;

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

//            GenerateRetrieverInterface gri = new GenerateRetrieverInterface();
//            gri.setDomainProtoManager(domainProtoManager);
//            gri.setCachePackage(cachePackage);
//            gri.setBasePath(dbdumpbase);
//            gri.generate(proto);
//
//            GenerateRetrieverRemote grr = new GenerateRetrieverRemote();
//            grr.setCachePackage(cachePackage);
//            grr.setBasePath(dbdumpbase);
//            grr.setImplPackage(implPackage);
//            grr.generate(proto);
//
//            if (generateDirect) {
//                GenerateRetrieverDirect gr = new GenerateRetrieverDirect();
//                gr.setDomainProtoManager(domainProtoManager);
//                gr.setBasePath(dbdumpbase);
//                gr.setImplPackage(implPackage);
//                gr.setCachePackage(cachePackage);
//                gr.setEntityPackage(entityPackage);
//                gr.setIdFields(idFields);
//                gr.setProperties(properties);
//                gr.generate(proto);
//            }

//            GenerateMapStore gms = new GenerateMapStore();
//            gms.setBasePath( dbdumpbase  );
//            gms.setImplPackage( implPackage );
//            gms.setCachePackage(cachePackage);
//            gms.setEntityPackage(entityPackage);
//            gms.generate(proto);

//        }

//        if (generateDirect) {
//            GenerateChangeListener gcl = new GenerateChangeListener();
//            gcl.setBasePath(dbdumpbase);
//            gcl.setImplPackage(implPackage);
//            gcl.setCachePackage(cachePackage);
//            gcl.setEntityPackage(entityPackage);
//            gcl.generate(classes, idFields);
//        }*/

        GenerateRepositoryManagerImpl grmi = new GenerateRepositoryManagerImpl();
        grmi.inputPackage = implPackage;
        grmi.outputPackage = outputPackage;
        grmi.basePath = dbdumpbase;
        grmi.diContext = diContext;
        grmi.subPackages = domainProtoManager.getProtoSubPackages();
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
}
