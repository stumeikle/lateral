package transgenic.lauterbrunnen.lateral.domain.generator

import transgenic.lauterbrunnen.lateral.domain.PackageScanner


class GenerateDomainTask  {

    def     generatedSourcesPath;
    def     propertyFile;

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
        String outputPackage = properties.get("domain.generated.package");

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

        def libdomainbase=generatedSourcesPath;
        def generatedDir = libdomainbase + "/" + outputPackage.replaceAll("\\.","/") + "/";
        def dir = new File(generatedDir);
        dir.mkdirs();

        //clean first
        for(File file: dir.listFiles()) file.delete();

//For each class generate an impl and an interface in the output package
//And a reference class
        for(Class c: classes) {

            //println "Found class " + c.getName();
            //if (c.getSimpleName().equals("Head")) { //ContactDetails")) {
            GenerateImpl gi = new GenerateImpl();
            gi.setBasePath( libdomainbase );
            gi.setPrototypePackage( entityPackage );
            gi.setOutputPackage(outputPackage );
            gi.setPrototypeClasses(classes);
            gi.generateImpl( c );

            GenerateIFace gif = new GenerateIFace();
            gif.setBasePath( libdomainbase );
            gif.setPrototypePackage( entityPackage );
            gif.setOutputPackage(outputPackage );
            gif.setPrototypeClasses(classes);
            gif.generateIFace( c );

            GenerateRef gref = new GenerateRef();
            gref.setBasePath( libdomainbase );
            gref.setPrototypePackage( entityPackage );
            gref.setOutputPackage(outputPackage );
            gref.setPrototypeClasses(classes);
            gref.generateRef( c );

            GenerateRepo grepo = new GenerateRepo();
            grepo.setBasePath( libdomainbase );
            grepo.setPrototypePackage( entityPackage );
            grepo.setOutputPackage(outputPackage );
            grepo.setPrototypeClasses(classes);
            grepo.generateRepo( c );
            //}
        }

        GenerateFactory generateFactory = new GenerateFactory();
        generateFactory.setBasePath( libdomainbase );
        generateFactory.setPrototypePackage( entityPackage );
        generateFactory.setOutputPackage(outputPackage );
        generateFactory.setPrototypeClasses(classes);
        generateFactory.generateFactory();
    }

}