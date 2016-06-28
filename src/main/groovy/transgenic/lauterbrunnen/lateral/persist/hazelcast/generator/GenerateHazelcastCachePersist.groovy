package transgenic.lauterbrunnen.lateral.persist.hazelcast.generator

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

//and combine with the other
//        inputStream = getClass().getClassLoader().getResourceAsStream("generate.entity.properties");
//        Properties propertiesEntity = new Properties();
//        try {
//            propertiesEntity.load(inputStream);
//            inputStream.close();
//        } catch (Exception e) {
//            println("Unable to load entity properties file.");
//            System.exit(0);
//        }
//        properties.putAll( propertiesEntity );
//
//        inputStream = getClass().getClassLoader().getResourceAsStream("generate.cache.properties");
//        Properties propertiesCache = new Properties();
//        try {
//            propertiesCache.load(inputStream);
//            inputStream.close();
//        } catch (Exception e) {
//            println("Unable to load entity properties file.");
//            System.exit(0);
//        }
//        properties.putAll( propertiesCache );

        String protoPackage = properties.get("domain.proto.package");
        String implPackage = properties.get("domain.generated.package");
        String entityPackage = properties.get("entity.generated.package");
        String cachePackage = properties.get("persist.hazelcast.generated.package");

//Find all classes in this package
        List<Class>     classes = PackageScanner.getClasses( protoPackage );

//Skip the enums
        Iterator<Class>     iterator = classes.iterator();
        while(iterator.hasNext()) {
            Class c= iterator.next();
            if (c.isEnum()) {
                iterator.remove();
            }
        }


        def dbdumpbase=generatedSourcesPath;
        def generatedDir = dbdumpbase + "/" + cachePackage.replaceAll("\\.","/") + "/";
        def dir = new File(generatedDir);
        dir.mkdirs();
        for(File file: dir.listFiles()) file.delete();


        Map<String, String>  idFields = new HashMap<>();
        Map<String, String> idFieldNames = new HashMap<>();

        for( Class proto: classes) {
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
                        idFields.put(proto.getName(), field.getType().getTypeName());
                        idFieldNames.put(name, repositoryIdFieldName);
                    }
                }
            }

            if( idField==null) {
                repositoryIdFieldName = "repositoryId";

                String name = proto.getName().replace(entityPackage, implPackage);
                idFields.put(name, UniqueId.class.getName());
                idFieldNames.put(name, repositoryIdFieldName);
            }
        }

        for(Class proto: classes) {

            GeneratePersister gp = new GeneratePersister();
            gp.setBasePath( dbdumpbase  );
            gp.setImplPackage( implPackage );
            gp.setCachePackage(cachePackage);
            gp.setEntityPackage(entityPackage);
            gp.generate(proto);

        }

        GenerateChangeListener gcl = new GenerateChangeListener();
        gcl.setBasePath( dbdumpbase  );
        gcl.setImplPackage( implPackage );
        gcl.setCachePackage(cachePackage);
        gcl.setEntityPackage(entityPackage);
        gcl.generate(classes, idFields);

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
