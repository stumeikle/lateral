package transgenic.lauterbrunnen.lateral.cache.hazelcast.generator

/**
 * Created by stumeikle on 05/06/16.
 */
class GenerateManager {
    protected String outputPackage;
    protected String inputPackage;
    protected String basePath;

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public void setOutputPackage(String outputPackage) {
        this.outputPackage = outputPackage;
    }

    public void setInputPackage(String inputPackage) {
        this.inputPackage = inputPackage;
    }

    public void generate(List<Class> repos) {
        def fn = basePath + "/" + outputPackage.replaceAll("\\.","/") + "/HCRepositoryManagerImpl.java";
        println "Writing " + fn;
        def output = new File(fn);

        output << "package " + outputPackage + ";" << System.lineSeparator()
        output << "" << System.lineSeparator();
        output << "//DO NOT MODIFY, this class was generated by xxx " << System.lineSeparator();
        output << ""<< System.lineSeparator();

        output << "import com.hazelcast.core.HazelcastInstance;" << System.lineSeparator() +
                "import com.hazelcast.core.IMap;" << System.lineSeparator() +
                "import com.hazelcast.core.IdGenerator;" << System.lineSeparator() +
                "import java.util.HashMap;" << System.lineSeparator() +
                "import java.util.Map;" << System.lineSeparator() +
                "import transgenic.lauterbrunnen.lateral.admin.AdminCommandQueue;" << System.lineSeparator()+
                "import transgenic.lauterbrunnen.lateral.admin.hazelcast.HCAdminCommandQueueImpl;"<< System.lineSeparator()+
                "import transgenic.lauterbrunnen.lateral.di.DefaultImpl;"<< System.lineSeparator() +
                "import transgenic.lauterbrunnen.lateral.cache.hazelcast.HCRepositoryManager;" << System.lineSeparator() +
                "import transgenic.lauterbrunnen.lateral.di.ApplicationDI;" << "" << System.lineSeparator()
        output << "import " + inputPackage + ".*;" << System.lineSeparator()

        output << "" << System.lineSeparator()
        output << "@DefaultImpl" << System.lineSeparator()
        output << "public class HCRepositoryManagerImpl implements HCRepositoryManager {" << System.lineSeparator() +
                "" << System.lineSeparator() +
                "    private static Map<String, IMap>    imapNameMap = new HashMap<>();"<< System.lineSeparator() +
                "" << System.lineSeparator() +
                "    public void initRepositories(HazelcastInstance hazel) {" << System.lineSeparator() +
                "        //We need cluster wide unique ids for every update so that we can co-ordinate the actions of the" << System.lineSeparator() +
                "        //master, slave db dumpers. both need to be able to identify an update event so they can agree" << System.lineSeparator() +
                "        //if it has been persisted. so we can use the hazelcast idgenerator, or we could use another" << System.lineSeparator() +
                "        //unique id. I have no idea if its faster to use 1 generator for all caches or to split it" << System.lineSeparator() +
                "        IdGenerator idGen = hazel.getIdGenerator(\"UpdateIdGenerator\");" << System.lineSeparator()

        for(Class repo: repos) {
            String name = repo.getSimpleName().replace("Repository","");
            String namelc = name.substring(0,1).toLowerCase() + name.substring(1);

            output << "" << System.lineSeparator()
            output << "        IMap " + namelc + "Map = hazel.getMap(\"" + name + "\");" << System.lineSeparator()
            output << "        imapNameMap.put(\"" + name + "\", " << namelc << "Map);" << System.lineSeparator()
            output << "        HC" + repo.getSimpleName() + "Impl " + namelc + "RepositoryImpl = new HC" +
                    repo.getSimpleName() + "Impl(" + namelc + "Map, idGen);" << System.lineSeparator()
            output << "        ApplicationDI.registerImplementation(" + repo.getSimpleName() + ".class, " +
                    namelc + "RepositoryImpl);" << System.lineSeparator()
        }
        
        output << System.lineSeparator();
        output << "        //Admin bus" << System.lineSeparator() +
                "        IMap adminCommandQueue = hazel.getMap(\"AdminCommandQueue\");" << System.lineSeparator() +
                "        imapNameMap.put(\"AdminCommandQueue\", adminCommandQueue);" << System.lineSeparator() +
                "        HCAdminCommandQueueImpl hcAdminCommandQueueImpl = new HCAdminCommandQueueImpl();" << System.lineSeparator() +
                "        ApplicationDI.registerImplementation(AdminCommandQueue.class, hcAdminCommandQueueImpl);" << System.lineSeparator()
        output << System.lineSeparator();

        output << "    }" << System.lineSeparator()
        output << "" << System.lineSeparator()
        output << "    public Map<String, IMap> getImapNameMap() {" << System.lineSeparator()+
                  "        return imapNameMap;" << System.lineSeparator()+
                "    }" << System.lineSeparator()

        output << "}" << System.lineSeparator()
    }

}
