package transgenic.lauterbrunnen.lateral.entity.generator

import transgenic.lauterbrunnen.lateral.domain.DomainProtoManager
import transgenic.lauterbrunnen.lateral.domain.RepositoryId
import transgenic.lauterbrunnen.lateral.domain.UniqueId

import java.lang.annotation.Annotation
import java.lang.reflect.Field

/**
 * Created by stumeikle on 01/07/20.
 */
class GenerateCassandraEntityTask {

    def generatedSourcesPath;
    def propertyFile;
    def diContext;

    public void generate() {
        Properties properties = new Properties();
        try {
            InputStream inputStream = new FileInputStream(propertyFile);
            properties.load(inputStream);
        } catch (Exception e) {
            println("Unable to load properties file.");
            System.exit(0);
        }

        String implPackage = properties.get("domain.generated.package");
        String jpaEntityPackage = properties.get("entity.generated.package");
        diContext = properties.get("lateral.di.context");
        def domainProtoManager = new DomainProtoManager(properties);
        def classes = domainProtoManager.getProtoClasses();

        println("Found " + classes.size() + " proto classes");

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

                        String name = implPackage +"." + domainProtoManager.stripPackageName(proto);
                        idFields.put(name, field.getType().getTypeName());
                        idFieldNames.put(name, repositoryIdFieldName);
                    }
                }
            }

            if( idField==null) {
                repositoryIdFieldName = "repositoryId";

                String name = implPackage +"." + domainProtoManager.stripPackageName(proto);
//                String name = proto.getName().replace(entityPackage, implPackage);
                idFields.put(name, UniqueId.class.getName());
                idFieldNames.put(name, repositoryIdFieldName);
            }
        }

        def dbdumpbase= generatedSourcesPath ;
        def generatedDir = dbdumpbase + "/" + jpaEntityPackage.replaceAll("\\.","/") + "/";
        def dir = new File(generatedDir);
        dir.mkdirs();
        for(File file: dir.listFiles()) file.delete();

        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        for(Class proto: classes) {

            String name = implPackage +"." + domainProtoManager.stripPackageName(proto);
            String repositoryIdFieldName = idFieldNames.get(name);

            //Now we need the impl class for the rest
            //String implClassName  = proto.getName().replace(entityPackage, implPackage) + "Impl";
            String implClassName  = name + "Impl";
            Class impl = null;

            try {
                impl = loader.loadClass(implClassName,true);//Class.forName(implClassName)
            } catch(Exception e) {
                e.printStackTrace();
            }

            GenerateCassandraEntity ge = new GenerateCassandraEntity();
            ge.setDomainProtoManager(domainProtoManager);
            ge.setBasePath( dbdumpbase );
            ge.setImplPackage( implPackage );
            ge.setJpaEntityPackage( jpaEntityPackage );
            ge.setProperties(properties);
            ge.setIdFields(idFields);
            ge.setIdFieldName(repositoryIdFieldName);
            ge.setIdFieldNames(idFieldNames);
            ge.setDiContext(diContext);
            ge.generate(proto, impl);
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
