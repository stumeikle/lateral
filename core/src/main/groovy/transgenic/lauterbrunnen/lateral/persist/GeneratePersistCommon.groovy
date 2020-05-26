package transgenic.lauterbrunnen.lateral.persist

/**
 * Created by stumeikle on 01/09/19.
 */
class GeneratePersistCommon {

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

        def dbdumpbase = generatedSourcesPath;
        def generatedDir = dbdumpbase + "/" + persistGeneratedPackage.replaceAll("\\.", "/") + "/";
        def dir = new File(generatedDir);
        dir.mkdirs();
        for (File file : dir.listFiles()) file.delete();

        GenerateTransactionManagerImpl  gtmi = new GenerateTransactionManagerImpl();
        gtmi.setDiContext(diContext);
        gtmi.setPersistGeneratedPackage(persistGeneratedPackage);
        gtmi.setDomainGeneratedPackage(domainPackage);
        gtmi.setPersistenceUnit(persistenceUnit);
        gtmi.setBasePath(generatedSourcesPath);
        gtmi.generate();
    }


}
