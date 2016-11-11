package transgenic.lauterbrunnen.lateral.rest.generator

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

        String entityPackage = properties.get("domain.proto.package");
        String domainGeneratedPackage = properties.get("domain.generated.package");
        String outputPackage = properties.get("rest.generated.package");

//Find all classes in this package
        List<Class> classes = PackageScanner.getClasses(entityPackage);

//Skip the enums
        Iterator<Class> iterator = classes.iterator();
        while (iterator.hasNext()) {
            Class c = iterator.next();
            if (c.isEnum()) {
                iterator.remove();
            }
        }

        def srcbase = generatedSourcesPath;
        def generatedDir = srcbase + "/" + outputPackage.replaceAll("\\.", "/") + "/";
        def dir = new File(generatedDir);
        dir.mkdirs();

        //clean first
        for (File file : dir.listFiles()) file.delete();

        for (Class proto : classes) {
            GenerateRestPojo grp = new GenerateRestPojo();
            grp.setBasePath(srcbase);
            grp.setPrototypePackage(entityPackage);
            grp.setDomainGeneratedPackage(domainGeneratedPackage);
            grp.setOutputPackage(outputPackage);
            grp.setPrototypeClasses(classes);
            grp.setProperties(properties);
            grp.generate(proto);

            //generate the endpoint
            GenerateEndpoint ge = new GenerateEndpoint();
            ge.setBasePath(srcbase);
            ge.setPrototypePackage(entityPackage);
            ge.setDomainGeneratedPackage(domainGeneratedPackage);
            ge.setOutputPackage(outputPackage);
            ge.setPrototypeClasses(classes);
            ge.setProperties(properties);
            ge.generate(proto);

        }

        GenerateRestResource gr = new GenerateRestResource();
        gr.setBasePath(srcbase);
        gr.setOutputPackage(outputPackage);
        gr.generate(classes);
    }
}