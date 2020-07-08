package transgenic.lauterbrunnen.lateral.persist

import transgenic.lauterbrunnen.lateral.domain.DomainProtoManager

/**
 * Created by stumeikle on 07/07/20.
 */
class GenerateCassandraPersistCommon {


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

        String persistGeneratedPackage = properties.get("persist.generated.package");
        String persistenceUnit = properties.get("persist.generated.persistence.unit");
        String diContext = properties.get("lateral.di.context");
        String domainPackage = properties.get("domain.generated.package");
        String entityPackage = properties.get("entity.generated.package");

        def dbdumpbase = generatedSourcesPath;
        def generatedDir = dbdumpbase + "/" + persistGeneratedPackage.replaceAll("\\.", "/") + "/";
        def dir = new File(generatedDir);
        dir.mkdirs();
        for (File file : dir.listFiles()) file.delete();

        GenerateCassandraManagerImpl  gtmi = new GenerateCassandraManagerImpl();
        gtmi.setDiContext(diContext);
        gtmi.setPersistGeneratedPackage(persistGeneratedPackage);
        gtmi.setDomainGeneratedPackage(domainPackage);
        gtmi.setEntityGeneratedPackage(entityPackage);

        DomainProtoManager domainProtoManager = new DomainProtoManager(properties);
        List<String>    allEntities = new ArrayList<>();
        for(Class proto: domainProtoManager.protoClasses) {
            allEntities.add(domainProtoManager.getEntityName(proto));
        }
        gtmi.setAllEntities(allEntities);
        gtmi.setPersistenceUnit(persistenceUnit);
        gtmi.setBasePath(generatedSourcesPath);
        gtmi.generate();
    }


}
