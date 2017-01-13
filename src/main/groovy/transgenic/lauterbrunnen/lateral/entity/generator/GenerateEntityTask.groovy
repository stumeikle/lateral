package transgenic.lauterbrunnen.lateral.entity.generator

import transgenic.lauterbrunnen.lateral.domain.DomainProtoManager
import transgenic.lauterbrunnen.lateral.domain.PackageScanner
import transgenic.lauterbrunnen.lateral.domain.RepositoryId
import transgenic.lauterbrunnen.lateral.domain.UniqueId

import java.lang.annotation.Annotation
import java.lang.reflect.Field

/**
 * Created by stumeikle on 15/06/16.
 */
class GenerateEntityTask  {

    def generatedSourcesPath;
    def propertyFile;
    def persistenceFile;

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
        def domainProtoManager = new DomainProtoManager(properties);
        def classes = domainProtoManager.getProtoClasses();

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

        for(Class proto: classes) {

            String name = implPackage +"." + domainProtoManager.stripPackageName(proto);
            String repositoryIdFieldName = idFieldNames.get(name);

            //Now we need the impl class for the rest
            //String implClassName  = proto.getName().replace(entityPackage, implPackage) + "Impl";
            String implClassName  = name + "Impl";
            Class impl = Class.forName(implClassName)

            GenerateEntity ge = new GenerateEntity();
            ge.setDomainProtoManager(domainProtoManager);
            ge.setBasePath( dbdumpbase );
            ge.setImplPackage( implPackage );
            ge.setJpaEntityPackage( jpaEntityPackage );
            ge.setProperties(properties);
            ge.setIdFields(idFields);
            ge.setIdFieldName(repositoryIdFieldName);
            ge.setIdFieldNames(idFieldNames);
            ge.generate(proto, impl);
        }

        //Last step -- look for persistence.xml and see if we need to insert any lines describing the
        //entities
        generatePersistenceXmlLines(classes, domainProtoManager, jpaEntityPackage);
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

    private static final String PERSISTENCE_BLOCK_START = "<!-- Generate Lateral Entities here: -->"
    private static final String PERSISTENCE_BLOCK_END   = "<!-- End of Lateral Entities -->"

    protected void generatePersistenceXmlLines(List<Class> classes, DomainProtoManager domainProtoManager, String entityPackage) {
        if (persistenceFile==null) return;
        BufferedReader reader = new BufferedReader(new FileReader(persistenceFile));

        String line="";
        boolean startFound = false, endFound = false;
        while(line !=null ) {
            line = reader.readLine();
            if (line!=null) {
                if (line.contains(PERSISTENCE_BLOCK_START)) startFound = true;
                if (line.contains(PERSISTENCE_BLOCK_END)) endFound = true;
            }
        }

        if (startFound) {
            //create new version of the file
            StringBuilder  sb = new StringBuilder();
            //reset to the start of the file. bit yuck
            reader.close();
            reader= new BufferedReader(new FileReader(persistenceFile));

            boolean skipToEnd = false;
            line="";
            while(line !=null) {
                line = reader.readLine();
                if (line!=null) {
                    if (!skipToEnd) {
                        sb.append(line);
                        sb.append(System.lineSeparator());
                    } else {
                        if (line.contains(PERSISTENCE_BLOCK_END)) {
                            skipToEnd=false;
                        }
                    }
                    if (line.contains(PERSISTENCE_BLOCK_START)) {
                        String prefix = line.replace(PERSISTENCE_BLOCK_START, "");

                        for(Class proto: classes) {
                            String name = entityPackage +"." + domainProtoManager.stripPackageName(proto);
                            sb.append(prefix);
                            sb.append("<class>");
                            sb.append(name);
                            sb.append("Entity</class>");
                            sb.append(System.lineSeparator());
                        }
                        sb.append(prefix);
                        sb.append(PERSISTENCE_BLOCK_END);
                        sb.append(System.lineSeparator());

                        skipToEnd = endFound;
                    }
                }
            }

            //if delta TODO write the new version
            reader.close();

            File f = persistenceFile;//new File(getClass().getClassLoader().getResource("META-INF/persistence.xml").toURI());
            System.out.println("Rewriting file :" + f.getCanonicalPath());

            FileWriter      writer = new FileWriter(f);
            writer.write(sb.toString());
            writer.close();

        }
    }
}
