package transgenic.lauterbrunnen.lateral.rest.generator

import transgenic.lauterbrunnen.lateral.domain.RepositoryId

import java.lang.annotation.Annotation
import java.lang.reflect.Field
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.util.function.Consumer

/**
 * Created by stumeikle on 10/11/16.
 */
class GenerateRestPojo {

    protected String prototypePackage;
    protected String outputPackage;
    protected String domainGeneratedPackage;
    protected List<Class> prototypeClasses;
    protected String basePath;
    private Map<String, Class> classMap = new HashMap<>();
    protected Properties properties;
    protected Field idField = null;

    public void setPrototypePackage(String prototypePackage) {
        this.prototypePackage = prototypePackage;
    }
    public void setOutputPackage(String outputPackage) {
        this.outputPackage = outputPackage;
    }
    public void setPrototypeClasses(List<Class> classes) {
        this.prototypeClasses = classes;
        for(Class clazz: classes) {
            classMap.put(clazz.getName(), clazz);
        }
    }
    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }
    public void setProperties(Properties properties) {
        this.properties = properties;
    }
    public void setDomainGeneratedPackage(String dgp) {
        this.domainGeneratedPackage = dgp;
    }

    def void generate(Class proto) {

        //what's the version?
        //default to the same as the rest api version
        //else get from generate.properties

        String api_version = properties.getProperty("rest.version");
        String pojo_version = properties.getProperty("rest.pojo." + proto.getSimpleName().toLowerCase() + ".version");
        if (api_version == null) api_version = "1";
        if (pojo_version == null) pojo_version = api_version;

        pojo_version = pojo_version.replaceAll("\\.", "_");

        def className = proto.getSimpleName() + "_" + pojo_version;
        def fn = basePath + "/" + outputPackage.replaceAll("\\.", "/") + "/" + className + ".java";
        println "Writing " + fn;
        def output = new File(fn);

        output << "package " + outputPackage + ";" << System.lineSeparator()
        output << "" << System.lineSeparator();
        output << "//DO NOT MODIFY, this class was generated by xxx " << System.lineSeparator();
        output << "" << System.lineSeparator();
        output << "import transgenic.lauterbrunnen.lateral.domain.Factory;" << System.lineSeparator();
        output << "import transgenic.lauterbrunnen.lateral.domain.UniqueId;" << System.lineSeparator();
        output << "import " << domainGeneratedPackage << ".*;" << System.lineSeparator();
        output << "import java.util.stream.Collectors;" << System.lineSeparator();
        output << "" << System.lineSeparator()
        output << "public class " << className << " {" << System.lineSeparator();
        output << System.lineSeparator();

        List<Field> allFields = getAllFields(proto);

        //copied from GenerateImpl, if i'm honest :-)
        List<Field> directProtoFields = new ArrayList<>();
        List<Field> collectionFields = new ArrayList<>();
        for (Field field : allFields) {

            Annotation[] notes = field.getAnnotations();
            for (Annotation note : notes) {
                if (note.annotationType().getName().equals(RepositoryId.class.getName())) {
                    idField = field;
                }
            }

            if (classMap.containsKey(field.getType().getTypeName())) {
                directProtoFields.add(field);
            }

            //check if the type is referenced as a generic
            boolean found = false;
            iterateGenerics(field.getGenericType(), new Consumer<Type>() {
                void accept(Type t) {
                    String name = t.getTypeName();
                    if (t instanceof ParameterizedType) {
                        ParameterizedType pt = (ParameterizedType) t;
                        name = pt.getRawType().getTypeName();
                    }

                    if (classMap.containsKey(name)) {
                        found = true;
                    }
                }
            });

            if (found) {
                //check what the type is, if its a list or a map thats fine
                Class<?> typeClass = field.getType();
                boolean ok = false;
                if (typeClass.getName().equals("java.util.List") || typeClass.getName().equals("java.util.Map")) {
                    ok = true;
                    collectionFields.add(field);
                } else {
                    for (Class<?> iface : typeClass.getInterfaces()) {
                        if (iface.getName().equals("java.util.List") || iface.getName().equals("java.util.Map")) {
                            ok = true;
                            collectionFields.add(field);
                            break;
                        }
                    }
                }
                if (!ok) {
                    throw new Exception("Entity type mentioned in generics, but I don't know how to traverse that type.");
                }
            }

            output << "    private " + swapType(field.getGenericType()) + " " + field.getName() + ";" << System.lineSeparator()
        }

        //If no id field is annotated , create one
        if (idField == null) {
            output << "    private String repositoryId;" << System.lineSeparator();
        } else {
            //nothing in this case
        }

        output << System.lineSeparator();

        //generate the isPresent booleans:
        generateIsPresent(output, allFields);

        //generate the getters and setters
        generateGettersAndSetters(output, proto, allFields);

        generateCreateUpdateImpl(output, proto, allFields);

        generateCreateFromEntity(output, proto, allFields);

        output << "}" << System.lineSeparator();
    }

    def generateIsPresent(def output, def allFields) {
        for(Field field: allFields) {
            output << "    private boolean isPresent" << capitalizeFirst(field.getName()) << " = false;" << System.lineSeparator();
        }
        if (idField==null) {
            output << "    private boolean isPresentRepositoryId = false;" << System.lineSeparator();
        }
    }

    protected void generateGettersAndSetters(def output, Class proto, List<Field> allFields) {
        for(Field field : allFields) {

            if (field.getName().startsWith("is")) {
                String shortName = field.getName().replace("is", "");
                String shortNameFirstLower = shortName.substring(0,1).toLowerCase() + shortName.substring(1);

                output << ""<< System.lineSeparator();
                output << "    public " + swapType(field.getGenericType()) + " " + field.getName() + "() {"<< System.lineSeparator();
                output << "        return this." + field.getName() + ";"<< System.lineSeparator();
                output << "    }"<< System.lineSeparator();

                output << ""<< System.lineSeparator();
                output << "    public void set" + shortName + "(" + swapType(field.getGenericType()) + " " + shortNameFirstLower + ") {"<< System.lineSeparator();
                output << "        this." + field.getName() + " = " + shortNameFirstLower + ";"<< System.lineSeparator();
                output << "        this.isPresent" + capitalizeFirst(field.getName()) + " =true;" << System.lineSeparator();
                output << "    }"<< System.lineSeparator();


            } else {

                output << ""<< System.lineSeparator();
                output << "    public " + swapType(field.getGenericType()) + " get" + capitalizeFirst(field.getName()) + "() {"<< System.lineSeparator()
                output << "        return this." + field.getName() + ";"<< System.lineSeparator()
                output << "    }"<< System.lineSeparator()

                output << ""<< System.lineSeparator()
                output << "    public void set" + capitalizeFirst(field.getName()) + "(" + swapType(field.getGenericType()) + " " + field.getName() + ") {"<< System.lineSeparator()
                output << "        this." + field.getName() + " = " + field.getName() + ";"<< System.lineSeparator()
                output << "        this.isPresent" + capitalizeFirst(field.getName()) + " =true;" << System.lineSeparator();
                output << "    }"<< System.lineSeparator()
            }
        }

        if (idField==null) {
            output << ""<< System.lineSeparator();
            output << "    public String getRepositoryId() {"<< System.lineSeparator()
            output << "        return this.repositoryId;" << System.lineSeparator()
            output << "    }"<< System.lineSeparator()

            output << ""<< System.lineSeparator()
            output << "    public void setRepositoryId( String repositoryId ) {"<< System.lineSeparator()
            output << "        this.repositoryId=repositoryId;"<< System.lineSeparator()
            output << "        this.isPresentRepositoryId = true;" << System.lineSeparator();
            output << "    }"<< System.lineSeparator()
        }
    }

    protected void generateCreateUpdateImpl(def output, Class proto, List<Field> allFields) {

        output << ""<< System.lineSeparator();
        output << "    public " << proto.getSimpleName() << "Impl createImpl() {" << System.lineSeparator();
        output << ""<< System.lineSeparator();
        output << "        " << proto.getSimpleName() << "Impl retval = (" << proto.getSimpleName() <<
                "Impl) Factory.create( " << proto.getSimpleName() << ".class );" << System.lineSeparator();

        for (Field field : allFields) {
            String setFn = field.getName();

            //Fix the booleans
            if (field.getType().getTypeName().equalsIgnoreCase("Boolean")) {
                setFn = setFn.replaceFirst("^is", "");
            }

            //need to test if the field is a domain object
            output << "        if (isPresent" << capitalizeFirst(field.getName()) << ") retval.set" << capitalizeFirst(setFn) <<
                    "("

            //Here we need to differentiate between various types
            //between types which are in our domain and we need to handle all the mapping possibilities with
            //lists and sets
            recurseSetLogic(output, field.getGenericType() ,field.getName(), new SetFromDomainField() {
                @Override
                void doIt(Object o, Object fn, Object tn) {
                    o << fn << ".createImpl()"
                }
            } );
            output << ");" << System.lineSeparator();
        }

        //repositoryid
        if (idField==null) {
            output << "        if (isPresentRepositoryId) retval.setRepositoryId(UniqueId.fromString(repositoryId));" << System.lineSeparator();
        }

        output << "        return retval;" << System.lineSeparator();
        output << "    }" << System.lineSeparator();

        output << ""<< System.lineSeparator();
        output << "    public " << proto.getSimpleName() << "Impl updateImpl( " << proto.getSimpleName() << "Impl basis ) {" << System.lineSeparator();
        output << ""<< System.lineSeparator();
        output << "        " << proto.getSimpleName() << "Impl retval = basis;" << System.lineSeparator();
        for (Field field : allFields) {

            String setFn = field.getName();

            //Fix the booleans
            if (field.getType().getTypeName().equalsIgnoreCase("Boolean")) {
                setFn = setFn.replaceFirst("^is", "");
            }

            //need to test if the field is a domain object
            output << "        if (isPresent" << capitalizeFirst(field.getName()) << ") retval.set" << capitalizeFirst(setFn) <<
                    "("

            //Here we need to differentiate between various types
            //between types which are in our domain and we need to handle all the mapping possibilities with
            //lists and sets
            //looks like groovy no java8
            recurseSetLogic(output, field.getGenericType() ,field.getName(), new SetFromDomainField() {
                @Override
                void doIt(Object o, Object fn, Object tn) {
                    o << fn << ".createImpl()"
                }
            } );
            output << ");" << System.lineSeparator();
        }

        //repositoryid
        if (idField==null) {
            output << "        if (isPresentRepositoryId) retval.setRepositoryId(UniqueId.fromString(repositoryId));" << System.lineSeparator();
        }

        output << "        return retval;" << System.lineSeparator();
        output << "    }" << System.lineSeparator();

    }

    //This part needs to reuse the recursive logic from generate entity transformer
    //ie lists and maps need to be descended into and package classes need to be transformed
    //

    //Me to break the logic out

    def generateCreateFromEntity(def output, Class proto, List<Field> allFields) {
        output << ""<< System.lineSeparator();

        String restEntityName = getClassName(proto.getSimpleName());

        output << "    public static " << restEntityName << " createFrom" << proto.getSimpleName() << "( " <<
                proto.getSimpleName() << " " << uncapitalizeFirst(proto.getSimpleName()) << ") {" << System.lineSeparator();
        output << ""<< System.lineSeparator();
        output << "        " << restEntityName << " retval = new " << restEntityName << "();" << System.lineSeparator();

        output << "        if ( " << uncapitalizeFirst(proto.getSimpleName()) <<" instanceof " <<
                proto.getSimpleName() << "Impl ) {" << System.lineSeparator();
        writeFields( true, output, proto, allFields );
        output << "        } else { " << System.lineSeparator();
        writeFields( false, output, proto, allFields );
        output << "        }" << System.lineSeparator();

        output << "        return retval;" << System.lineSeparator();
        output << "    }" << System.lineSeparator();
    }

    private void writeFields(def writeAll, def output, def proto, def allFields) {
        for (Field field : allFields) {

            if (!writeAll) {
                if (field!=idField) continue;
            }

            //need to test if the field is a domain object
            String setFn = field.getName();
            String getFn = "get" + capitalizeFirst(field.getName());

            //Fix the booleans
            if (field.getType().getTypeName().equalsIgnoreCase("Boolean")) {
                setFn = setFn.replaceFirst("^is", "");
                getFn = field.getName();
            }

            String typename= field.getType().getTypeName();

            //20161118 We need null checks ...
            output << "            ";
            String fieldname = uncapitalizeFirst(proto.getSimpleName()) << "." << getFn << "()";
            if (!field.getType().isPrimitive()) {
                output << "if (" << fieldname << "!=null) ";
            }

            output << "retval.set" << capitalizeFirst(setFn) << "( ";

            recurseSetLogic(output, field.getGenericType() ,fieldname, new SetFromDomainField() {
                @Override
                void doIt(Object o, Object fn, Object tn) {
                    String simpleName = classMap.get(tn).getSimpleName();

                    String otherRest = getClassName(simpleName);
                    o << otherRest << ".createFrom" << simpleName << "(" << fn << ")";
                }
            } );
            output << ");" << System.lineSeparator();
        }
        //repositoryid
        if (idField==null) {

            //retval.setRepositoryId(((GarageImpl)garage).getRepositoryId().toString());
            String castTo="";
            if (writeAll==true) castTo="Impl";
            else                castTo="Reference"

            output << "            retval.setRepositoryId(((" << proto.getSimpleName() << castTo << ")" <<
                    uncapitalizeFirst(proto.getSimpleName()) << ").getRepositoryId().toString());" << System.lineSeparator()
        }
    }

    //copied from generate impl
    private String swapTypeName(String name) {
        if (classMap.containsKey(name)) {
            Class c = classMap.get(name);
            return getClassName(c.getSimpleName());
        }
        return name;
    }

    private String capitalizeFirst(String string) {
        return string.substring(0,1).toUpperCase() + string.substring(1);
    }

    private String uncapitalizeFirst(String string) {
        return string.substring(0,1).toLowerCase() + string.substring(1);
    }

    protected String swapType(Type type) {
        if (type instanceof ParameterizedType) {
            ParameterizedType p = (ParameterizedType)type;
            String retval = swapTypeName(p.getRawType().getTypeName());
            retval+="<";
            //then do the parameters
            boolean first =true;
            for(Type t : p.getActualTypeArguments()) {
                if (!first) retval = retval + ",";
                retval = retval + swapType(t);
                first =false;
            }
            retval+=">";
            return retval;
        }
        String retval = swapTypeName(type.getTypeName());
        return retval;
    }

    private void iterateGenerics(Type type, Consumer<Type> classConsumer) {
        if (type instanceof ParameterizedType) {
            ParameterizedType p = (ParameterizedType)type;
            classConsumer.accept(p);
            for(Type t : p.getActualTypeArguments()) {
                classConsumer.accept(t);
                if (t instanceof ParameterizedType)
                { iterateGenerics((ParameterizedType)t, classConsumer); }
            }
        }
    }

    def String getClassName(String entityName) {
        String api_version = properties.getProperty("rest.version");
        String pojo_version = properties.getProperty("rest.pojo." + entityName.toLowerCase() + ".version");
        if (api_version==null) api_version="1";
        if (pojo_version==null) pojo_version=api_version;

        pojo_version=pojo_version.replaceAll("\\.","_");
        return entityName + "_" + pojo_version;
    }

    protected List<Field> getAllFields(Class klass) {
        List<Field> retval = new ArrayList<>();
        Class sc = klass.getSuperclass();

        if (sc!=null) {
            retval.addAll( getAllFields( sc ));
        }

        retval.addAll( klass.getDeclaredFields() );
        return retval;
    }

    //---------------------------------------------------------------------------------------
    //Largely copied from generate entity for now
    interface SetFromDomainField {
        void doIt( def output, def field_name, def type_name );
    }


    private void recurseSetLogic(def output, def type, def field_name, SetFromDomainField sfdf) {
        String typename = type.getTypeName();

        if (type instanceof ParameterizedType) {
            typename = ((ParameterizedType)type).getRawType().getTypeName();
        }

        if (classMap.containsKey(typename)) {
            //This is one of ours
            //No need to think about converters in this instance
            //output << field_name << ".createImpl()";

            sfdf.doIt( output, field_name, typename );
        } else {

            if (type instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) type;
                String rawname = pt.getRawType().getTypeName();

                recurseSetHandleList(output, rawname, field_name, pt, sfdf );
                recurseSetHandleMap( output, rawname, field_name, pt, sfdf );

            }
            else {
                output << field_name
            }
        }
    }

    private def recurseSetHandleList(def output, def raw_name, def field_name, def parameterized_type,SetFromDomainField sfdf) {
        try {
            //test if this is a list
            Class c = Class.forName(raw_name);
            boolean list = false;
            if (List.class.getName().equals(c.getName())) list = true;
            for(Class iface: c.getInterfaces()) {
                if (List.class.getName().equals(iface.getName())) {
                    list = true;
                    break;
                }
            }

            if (list) {
                //No null check this time as we are already checking isPresent
                //Preamble
                output << field_name << ".stream().map( item -> " << System.lineSeparator();
                output << "            ";

                //then recurse into the type
                for (Type t : parameterized_type.getActualTypeArguments()) {
                    recurseSetLogic(output,t, "item",sfdf);
                }

                //then postamble
                output << System.lineSeparator() << "        ).collect(Collectors.toList())"
            }

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private def recurseSetHandleMap(def output, def raw_name, def field_name, def parameterized_type, SetFromDomainField sfdf) {
        try {
            Class c = Class.forName(raw_name);
            boolean map = false;
            if (Map.class.getName().equals(c.getName())) map = true;
            for(Class iface: c.getInterfaces()) {
                if (Map.class.getName().equals(iface.getName())) {
                    map = true;
                    break;
                }
            }

            if (map) {
                //preamble
                output <<  field_name + ".entrySet().stream().collect(Collectors.toMap( " << System.lineSeparator()
                output << "            e -> "

                //then recurse into the type
                Type keytype = parameterized_type.getActualTypeArguments()[0];
                recurseSetLogic(output, keytype, "e.getKey()", sfdf);
                output << "," << System.lineSeparator()
                output << "            e-> ";
                Type valuetype = parameterized_type.getActualTypeArguments()[1];
                recurseSetLogic(output, valuetype,"e.getValue()", sfdf);

                //then postamble
                output << "))"

            }

        } catch(Exception e) {}
    }

}
