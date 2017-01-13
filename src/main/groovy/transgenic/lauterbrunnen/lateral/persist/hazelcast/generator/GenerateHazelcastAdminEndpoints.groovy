package transgenic.lauterbrunnen.lateral.persist.hazelcast.generator

import transgenic.lauterbrunnen.lateral.domain.DomainProtoManager
import transgenic.lauterbrunnen.lateral.domain.PackageScanner
import transgenic.lauterbrunnen.lateral.domain.RepositoryId
import transgenic.lauterbrunnen.lateral.domain.UniqueId

import java.lang.annotation.Annotation
import java.lang.reflect.Field

/**
 * Created by stumeikle on 01/12/16.
 */
class GenerateHazelcastAdminEndpoints {
    def generatedSourcesPath;
    def propertyFile;
    def generateDirect;

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
        def domainProtoManager = new DomainProtoManager(properties);
        def classes = domainProtoManager.getProtoClasses();

        def dbdumpbase = generatedSourcesPath;
        def generatedDir = dbdumpbase + "/" + cachePackage.replaceAll("\\.", "/") + "/";
        def dir = new File(generatedDir);
        dir.mkdirs();
        for (File file : dir.listFiles()) {
            if (file.getName().endsWith("AdminEndpoint.java"))
                file.delete()
        };

        Map<String, String> idFields = new HashMap<>();
        Map<String, String> idFieldNames = new HashMap<>();

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

        for (Class proto : classes) {
            GenerateHCAdminEndpoint ghcae = new GenerateHCAdminEndpoint();
            ghcae.setCachePackage(cachePackage);
            ghcae.setBasePath(dbdumpbase);
            ghcae.setImplPackage(implPackage);
            ghcae.generate(proto);
        }

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
