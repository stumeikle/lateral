package transgenic.lauterbrunnen.lateral.domain.generator

import transgenic.lauterbrunnen.lateral.domain.DomainProtoManager

/**
 * Created by stumeikle on 13/09/19.
 */
class GenerateDomainTaskPt2 extends GenerateDomainTask{

    def generate() {
        Properties properties = new Properties();
        try {
            InputStream inputStream = new FileInputStream(propertyFile);
            properties.load(inputStream);
            inputStream.close();
        } catch (Exception e) {
            println("Unable to load properties file.");
            System.exit(0);
        }

        String outputPackage = properties.get("domain.generated.package");
        String diContext = properties.get("lateral.di.context");

        def domainProtoManager = new DomainProtoManager(properties);
        def classes = domainProtoManager.getProtoClasses();

        def libdomainbase=generatedSourcesPath;

        GenerateFactory generateFactory = new GenerateFactory();
        generateFactory.setProtoPackage(domainProtoManager.getProtoPackage());
        generateFactory.setProtoSubPackages( domainProtoManager.getProtoSubPackages() );
        generateFactory.setBasePath( libdomainbase );
        generateFactory.setOutputPackage(outputPackage );
        generateFactory.setPrototypeClasses(classes);
        generateFactory.setDiContext(diContext);
        generateFactory.setClassLoader(classLoader);
        generateFactory.generateFactory();
    }
}
