package transgenic.lauterbrunnen.lateral.persist.hazelcast.generator

import transgenic.lauterbrunnen.lateral.domain.DomainProtoManager
import transgenic.lauterbrunnen.lateral.domain.PackageScanner
import transgenic.lauterbrunnen.lateral.domain.RepositoryId
import transgenic.lauterbrunnen.lateral.domain.UniqueId

import java.lang.annotation.Annotation
import java.lang.reflect.Field

/**
 * Created by stumeikle on 18/06/16.
 */
class GenerateHazelcastCachePersist {

    def generatedSourcesPath;
    def propertyFile;
    def generateDirect;
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

        String implPackage = properties.get("domain.generated.package");
        String entityPackage = properties.get("entity.generated.package");
        String cachePackage = properties.get("persist.hazelcast.generated.package");
        diContext = properties.get("lateral.di.context");
        def domainProtoManager = new DomainProtoManager(properties);
        def classes = domainProtoManager.getProtoClasses();

        def dbdumpbase = generatedSourcesPath;
        def generatedDir = dbdumpbase + "/" + cachePackage.replaceAll("\\.", "/") + "/";
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

            if (generateDirect) {
                GeneratePersister gp = new GeneratePersister();
                gp.setDomainProtoManager(domainProtoManager);
                gp.setBasePath(dbdumpbase);
                gp.setImplPackage(implPackage);
                gp.setCachePackage(cachePackage);
                gp.setEntityPackage(entityPackage);
                gp.setDiContext(diContext);
                gp.generate(proto);
                GeneratePersisterInterface gpi = new GeneratePersisterInterface();
                gpi.setCachePackage(cachePackage);
                gpi.setBasePath(dbdumpbase);
                gpi.setDiContext(diContext);
                gpi.generate(proto);
            }

            GenerateRetrieverInterface gri = new GenerateRetrieverInterface();
            gri.setDomainProtoManager(domainProtoManager);
            gri.setCachePackage(cachePackage);
            gri.setBasePath(dbdumpbase);
            gri.setDiContext(diContext);
            gri.generate(proto);

            if (generateDirect) {
                GenerateRetrieverDirect gr = new GenerateRetrieverDirect();
                gr.setDomainProtoManager(domainProtoManager);
                gr.setBasePath(dbdumpbase);
                gr.setImplPackage(implPackage);
                gr.setCachePackage(cachePackage);
                gr.setEntityPackage(entityPackage);
                gr.setIdFields(idFields);
                gr.setProperties(properties);
                gr.setDiContext(diContext);
                gr.generate(proto);
            }

        }

        GenerateMapStoreFactory gmsf = new GenerateMapStoreFactory();
        gmsf.setBasePath(dbdumpbase);
        gmsf.setImplPackage(implPackage);
        gmsf.setCachePackage(cachePackage);
        gmsf.setEntityPackage(entityPackage);
        gmsf.setGenerateDirect(generateDirect);
        gmsf.setDiContext(diContext);
        gmsf.generate(classes);


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
