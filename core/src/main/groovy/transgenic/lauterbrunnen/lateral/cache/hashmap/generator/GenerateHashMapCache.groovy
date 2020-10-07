package transgenic.lauterbrunnen.lateral.cache.hashmap.generator

import transgenic.lauterbrunnen.lateral.domain.DomainProtoManager
import transgenic.lauterbrunnen.lateral.domain.PackageScanner
import transgenic.lauterbrunnen.lateral.domain.internal._Sequence

/**
 * Created by stumeikle on 03/10/20.
 */
class GenerateHashMapCache {
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
        String outputPackage = properties.get("cache.hashmap.generated.package");
        String diContext = properties.get("lateral.di.context");

//Find all classes in this package TODO fix for internals
        DomainProtoManager dpm = new DomainProtoManager(properties);
        List<Class>     classes = dpm.getProtoClasses();//PackageScanner.getClasses( inputPackage );
        List<Class>     generatedClasses = PackageScanner.getClasses( inputPackage );
        List<Class>     repoClasses = new ArrayList<>();
        boolean sequencesUsed = classes.contains(_Sequence.class);

        for(Class c: generatedClasses) {
//            println("Checking class " + c);

            for(Class iface: c.getInterfaces()) {
                if (iface.getSimpleName().equals("CRUDRepository")) {
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

//Generate the repository manager
        GenerateManager gm = new GenerateManager();
        gm.setInputPackage(inputPackage);
        gm.setOutputPackage(outputPackage);
        gm.setBasePath( libdomainbase );
        gm.setDiContext(diContext);
        gm.setSubPackages(dpm.getProtoSubPackages());
        gm.generate(repoClasses);

//Generate each of the repository impls
        for( Class repo : repoClasses ) {
            GenerateRepo gr = new GenerateRepo();
            gr.setClassLoader(classLoader);

            //input package needs to include the sub pacakge now
            String repoName = repo.getSimpleName();
            String entityName = repoName.replace("Repository", "");

            String ip = inputPackage;
            String subp= dpm.getSubPackageForProto(entityName);
            if (!"".equals(subp)) {
                ip += "." + subp;
            }
            gr.setInputPackage(ip);
            gr.setDomainPackage(inputPackage);
            gr.setOutputPackage(outputPackage);
            gr.setBasePath( libdomainbase );
            gr.setDiContext(diContext);
            gr.generate(repo);
        }
    }

}
