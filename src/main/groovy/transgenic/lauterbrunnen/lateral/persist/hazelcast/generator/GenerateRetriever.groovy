package transgenic.lauterbrunnen.lateral.persist.hazelcast.generator

import transgenic.lauterbrunnen.lateral.domain.generator.GenerateConverterName

import java.lang.reflect.Type

/**
 * Created by stumeikle on 06/11/16.
 */
class GenerateRetriever {

    protected String basePath;
    protected String implPackage;
    protected String cachePackage;
    protected String entityPackage;
    private Map<String, String>  idFields = new HashMap<>();
    private Properties properties;

    String getBasePath() {
        return basePath
    }

    void setBasePath(String basePath) {
        this.basePath = basePath
    }

    String getImplPackage() {
        return implPackage
    }

    void setImplPackage(String implPackage) {
        this.implPackage = implPackage
    }

    String getCachePackage() {
        return cachePackage
    }

    void setCachePackage(String cachePackage) {
        this.cachePackage = cachePackage
    }

    void setEntityPackage(String entityPackage) {
        this.entityPackage = entityPackage
    }

    public void setIdFields(Map<String, String> idFields) {
        this.idFields = idFields;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public void generate(Class proto) {
        def fn = basePath + "/" + cachePackage.replaceAll("\\.","/") + "/" + proto.getSimpleName() + "ImplRetriever.java";
        println "Writing " + fn;
        def output = new File(fn);

        output << "package " + cachePackage + ";" << System.lineSeparator()
        output << "" << System.lineSeparator();
        output << "//DO NOT MODIFY, this class was generated by xxx " << System.lineSeparator();
        output << ""<< System.lineSeparator();

        output << "import transgenic.lauterbrunnen.lateral.domain.*;" << System.lineSeparator();
        output << "import " << implPackage << ".*;" << System.lineSeparator();
        output << "import " << entityPackage << "." << proto.getSimpleName() << "Entity;" << System.lineSeparator()
        output << "import " << entityPackage << "." << proto.getSimpleName() << "EntityTransformer;" << System.lineSeparator()
        output << 
                "import transgenic.lauterbrunnen.lateral.persist.TransactionManager;"<< System.lineSeparator()

        output << "" << System.lineSeparator();
        String implRetriever = proto.getSimpleName() + "ImplRetriever";
        String impl = proto.getSimpleName() + "Impl";
        String entityTransformer = proto.getSimpleName() + "EntityTransformer";
        String entity = proto.getSimpleName() + "Entity";
        String implLC = impl.substring(0,1).toLowerCase() + impl.substring(1);
        String entityLC = entity.substring(0,1).toLowerCase() + entity.substring(1);

        output << "public class " << implRetriever << " {" << System.lineSeparator() +
                "" << System.lineSeparator() +
                "    private ThreadLocal<" << entity << "> " << entityLC << " = new ThreadLocal<>();" << System.lineSeparator() +
                "" << System.lineSeparator() +
                "    public " << impl << " retrieve(Object key) {" << System.lineSeparator() +
                "" << System.lineSeparator();

        //we need to know which class the repository id is and if there is a converter
        output << "        //convert repository key to db key if needed" << System.lineSeparator();
        output << "        Object      dbKey=";
        String name = proto.getName().replace(entityPackage, implPackage);
//        println "Proto name= " << proto.getName();
//        println "Name= " << name;
//        println "IdFields.get name: " << idFields.get(name);

        String reptype =idFields.get(name);
        String converter = properties.get("entity.type.converter." + reptype);
        String destClass = properties.get("entity.swap.type." + reptype);
        if (converter!=null) {
            GenerateConverterName gcn = GenerateConverterName.createHook(reptype, destClass);

            output << proto.getSimpleName() << "EntityTransformer." << gcn.converterMethodName << "( (" << reptype << ")key, " <<
                    converter << " );" << System.lineSeparator();
        } else {
            output << "key;" << System.lineSeparator();
        }

        output << "        " << entityLC << ".set(null);" << System.lineSeparator() +
                "        TransactionManager.INSTANCE.runInTransactionalContext(em -> {" << System.lineSeparator() +
                "            " << entityLC << ".set( em.find( " << entity << ".class, dbKey ));" << System.lineSeparator() +
                "        });" << System.lineSeparator() +
                "" << System.lineSeparator() +
                "        //convert back to impl" << System.lineSeparator() +
                "        if (" << entityLC << ".get()!=null) {" << System.lineSeparator() +
                "            " << impl << " " << implLC << " = new " << impl << "();" << System.lineSeparator() +
                "            " << entityTransformer << ".transform(" << implLC << "," << entityLC << ".get());" << System.lineSeparator() +
                "" << System.lineSeparator() +
                "            return " << implLC << ";" << System.lineSeparator() +
                "        }" << System.lineSeparator() +
                "        return null;" << System.lineSeparator() +
                "    }" << System.lineSeparator() +
                "}"<< System.lineSeparator()
    }
}
