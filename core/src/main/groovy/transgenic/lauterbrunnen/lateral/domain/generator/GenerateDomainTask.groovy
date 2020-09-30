package transgenic.lauterbrunnen.lateral.domain.generator

import org.apache.commons.io.FileUtils
import transgenic.lauterbrunnen.lateral.domain.DomainProtoManager
import transgenic.lauterbrunnen.lateral.domain.PackageScanner


class GenerateDomainTask  {

    def     generatedSourcesPath;
    def     propertyFile;
    def     classLoader;

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

        String outputPackage = properties.get("domain.generated.package");
        String diContext = properties.get("lateral.di.context");

        def domainProtoManager = new DomainProtoManager(properties);
        def classes = domainProtoManager.getProtoClasses();

        def libdomainbase=generatedSourcesPath;
        def generatedDir = libdomainbase + "/" + outputPackage.replaceAll("\\.","/") + "/";
        def dir = new File(generatedDir);

        //clean first inc sub packages
        FileUtils.deleteDirectory(dir);
        dir.mkdirs();

//For each class generate an impl and an interface in the output package
//And a reference class
        for(Class c: classes) {

            //if (c.getSimpleName().equals("Head")) { //ContactDetails")) {
            GenerateImpl gi = new GenerateImpl();
            gi.setProtoPackage( domainProtoManager.getProtoPackage() );
            gi.setBasePath( libdomainbase );
            gi.setDiContext(diContext);
            gi.setOutputPackage(outputPackage );
            gi.setPrototypeClasses(classes);
            gi.generateImpl( c );

            GenerateIFace gif = new GenerateIFace();
            gif.setProtoPackage( domainProtoManager.getProtoPackage() );
            gif.setDiContext(diContext);
            gif.setBasePath( libdomainbase );
            gif.setOutputPackage(outputPackage );
            gif.setPrototypeClasses(classes);
            gif.setDpm(domainProtoManager);
            gif.generateIFace( c );

            GenerateRef gref = new GenerateRef();
            gref.setProtoPackage( domainProtoManager.getProtoPackage() );
            gref.setBasePath( libdomainbase );
            gref.setDiContext(diContext);
            gref.setOutputPackage(outputPackage );
            gref.setPrototypeClasses(classes);
            gref.generateRef( c );

            GenerateRepo grepo = new GenerateRepo();
            grepo.setProtoPackage( domainProtoManager.getProtoPackage() );
            grepo.setBasePath( libdomainbase );
            grepo.setDiContext(diContext);
            grepo.setOutputPackage(outputPackage );
            grepo.setPrototypeClasses(classes);
            grepo.generateRepo( c );
            //}
        }

        GenerateDefaultRepositoryImpl generateDefaultRepository = new GenerateDefaultRepositoryImpl();
        generateDefaultRepository.setProtoSubPackages( domainProtoManager.getProtoSubPackages() );
        generateDefaultRepository.setBasePath( libdomainbase );
        generateDefaultRepository.setOutputPackage(outputPackage);
        generateDefaultRepository.setDiContext(diContext);
        generateDefaultRepository.setPrototypeClasses(classes);
        generateDefaultRepository.generateDefaultRepositoryImpl();

        GenerateContextClass generateContextClass = new GenerateContextClass();
        generateContextClass.setBasePath(libdomainbase);
        generateContextClass.setOutputPackage(outputPackage);
        generateContextClass.setDiContext(diContext);
        generateContextClass.generateContextClass();

        /*
        GenerateFactory generateFactory = new GenerateFactory();
        generateFactory.setBasePath( libdomainbase );
        generateFactory.setOutputPackage(outputPackage );
        generateFactory.setPrototypeClasses(classes);
        generateFactory.setDiContext(diContext);
        generateFactory.setClassLoader(classLoader);
        generateFactory.generateFactory();
        */
    }

}