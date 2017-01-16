package transgenic.lauterbrunnen.lateral.cache.hazelcast.generator

import transgenic.lauterbrunnen.lateral.domain.DomainProtoManager
import transgenic.lauterbrunnen.lateral.domain.PackageScanner
import transgenic.lauterbrunnen.lateral.domain.internal._Sequence

/**
 * Created by stumeikle on 14/06/16.
 */
class GenerateHazelcastCache {

    def     propertyFile;
    def     generatedSourcesPath;
    def     classLoader;

    public void generate() {
        Properties properties = new Properties();
        try {
            InputStream inputStream = new FileInputStream(propertyFile);
            properties.load(inputStream);
            inputStream.close();
        } catch (Exception e) {
            println("Unable to load properties file.");
            System.exit(0);
        }

        String inputPackage = properties.get("domain.generated.package");
        String outputPackage = properties.get("cache.hazelcast.generated.package");

//Find all classes in this package TODO fix for internals
        DomainProtoManager dpm = new DomainProtoManager(properties);
        List<Class>     classes = dpm.getProtoClasses();//PackageScanner.getClasses( inputPackage );
        List<Class>     repoClasses = new ArrayList<>();
        boolean sequencesUsed = classes.contains(_Sequence.class);

        for(Class c: classes) {
            for(Class iface: c.getInterfaces()) {
                if (iface.getSimpleName().equals("CRUDRepository")) {
                    println "Found class " + c.getName();
                    repoClasses.add(c);
                    break;
                }
            }
        }

        def libdomainbase=generatedSourcesPath;
        def generatedDir = libdomainbase + "/" + outputPackage.replaceAll("\\.","/") + "/";
        def dir = new File(generatedDir);
        dir.mkdirs();
        for(File file: dir.listFiles()) {
            file.delete()
            //println file.getName()
        };

//Generate the common
        GenerateCommon gc = new GenerateCommon();
        gc.setSequencesUsed(sequencesUsed);
        gc.setOutputPackage(outputPackage);
        gc.setBasePath( libdomainbase );
        gc.setGeneratedDomainPackage( inputPackage );
        gc.generate(repoClasses);

//Generate the repository manager
        GenerateManager gm = new GenerateManager();
        gm.setInputPackage(inputPackage);
        gm.setOutputPackage(outputPackage);
        gm.setBasePath( libdomainbase );
        gm.generate(repoClasses);

//Generate each of the repository impls
        for( Class repo : repoClasses ) {
            GenerateRepo gr = new GenerateRepo();
            gr.setClassLoader(classLoader);
            gr.setInputPackage(inputPackage);
            gr.setOutputPackage(outputPackage);
            gr.setBasePath( libdomainbase );
            gr.generate(repo);
        }
    }
}
