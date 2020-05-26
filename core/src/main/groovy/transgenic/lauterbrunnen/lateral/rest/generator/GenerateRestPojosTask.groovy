package transgenic.lauterbrunnen.lateral.rest.generator

import transgenic.lauterbrunnen.lateral.domain.DomainProtoManager
import transgenic.lauterbrunnen.lateral.domain.PackageScanner

/**
 * Created by stumeikle on 10/11/16.
 */
class GenerateRestPojosTask {

    def generatedSourcesPath;
    def propertyFile;

    def generate() {
        //run the generator
//Get the config

        Properties properties = new Properties();
        try {
            InputStream inputStream = new FileInputStream(propertyFile);
            properties.load(inputStream);
            inputStream.close();
        } catch (Exception e) {
            println("Unable to load properties file.");
            System.exit(0);
        }

        String domainGeneratedPackage = properties.get("domain.generated.package");
        String outputPackage = properties.get("rest.generated.package");
        String diContext = properties.get("lateral.di.context");
        def domainProtoManager = new DomainProtoManager(properties);
        def classes = domainProtoManager.getProtoClassesNoInternals();

        def srcbase = generatedSourcesPath;
        def generatedDir = srcbase + "/" + outputPackage.replaceAll("\\.", "/") + "/";
        def dir = new File(generatedDir);
        dir.mkdirs();

        //clean first
        for (File file : dir.listFiles()) file.delete();

        for (Class proto : classes) {
            GenerateRestPojo grp = new GenerateRestPojo();
            grp.setBasePath(srcbase);
            grp.setDomainGeneratedPackage(domainGeneratedPackage);
            grp.setOutputPackage(outputPackage);
            grp.setPrototypeClasses(classes);
            grp.setProperties(properties);
            grp.setDiContext(diContext);
            grp.generate(proto);

            //generate the endpoint
            GenerateEndpoint ge = new GenerateEndpoint();
            ge.setBasePath(srcbase);
            ge.setDomainGeneratedPackage(domainGeneratedPackage);
            ge.setOutputPackage(outputPackage);
            ge.setPrototypeClasses(classes);
            ge.setProperties(properties);
            ge.setDiContext(diContext);
            ge.generate(proto);

        }

        GenerateRestResource gr = new GenerateRestResource();
        gr.setBasePath(srcbase);
        gr.setOutputPackage(outputPackage);
        gr.setDiContext(diContext);
        gr.setDomainGeneratedPackage(domainGeneratedPackage);
        gr.generate(classes);
    }
}