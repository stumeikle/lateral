package transgenic.lauterbrunnen.lateral.domain.generator

import transgenic.lauterbrunnen.lateral.domain.OptimisticLocking
import transgenic.lauterbrunnen.lateral.domain.RepositoryId

import java.lang.annotation.Annotation
import java.lang.reflect.Field
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.util.function.Consumer

import static groovy.lang.Sequence.*

/**
 * Created by stumeikle on 29/05/16.
 */
class GenerateImpl {

    protected String outputPackage;
    protected List<Class> prototypeClasses;
    protected String basePath;
    private Map<String, Class> classMap = new HashMap<>();

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

    private String swapTypeName(String name) {
        if (classMap.containsKey(name)) {
            Class c = classMap.get(name);
            return /*outputPackage + "." + */c.getSimpleName() ;//+ "Reference";
        }
        return name;
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

    public void generateImpl(Class proto) {
        def fn = basePath + "/" + outputPackage.replaceAll("\\.","/") + "/" + proto.getSimpleName() + "Impl.java";
        println "Writing " + fn;
        def output = new File(fn);

        boolean optimisticLocking = false;
        for(Annotation note: proto.getAnnotations()) {
            if(note.annotationType().getName().equals(OptimisticLocking.class.getName())) optimisticLocking=true;
        }

        output << "package " + outputPackage + ";" << System.lineSeparator()
        output << "" << System.lineSeparator();
        output << "//DO NOT MODIFY, this class was generated by xxx " << System.lineSeparator();
        output << ""<< System.lineSeparator();
        output << "import transgenic.lauterbrunnen.lateral.domain.*;" << System.lineSeparator()
        output << "import java.io.Serializable;" << System.lineSeparator()
        output << "import java.util.Collection;" << System.lineSeparator()
        if (optimisticLocking) {
            output << "import transgenic.lauterbrunnen.lateral.domain.OptimisticLockingException;"<< System.lineSeparator()
        }
        output << "" << System.lineSeparator()

        //transfer the annotation if needed
        if (optimisticLocking) {
            output << "@OptimisticLocking" << System.lineSeparator();
        }
        output << "public class " + proto.getSimpleName() + "Impl implements " + proto.getSimpleName() + ",Serializable,EntityImpl {"<< System.lineSeparator()

        output << "    @Transient"<<System.lineSeparator();
        output << "    private boolean loadedFromStore=false;"<< System.lineSeparator()
        output << "    public boolean loadedFromStore() { return loadedFromStore; }"<< System.lineSeparator()
        output << "    public void setLoadedFromStore(boolean loadedFromStore) { this.loadedFromStore = loadedFromStore; }"<< System.lineSeparator()
        output << "    private long updateId;" << System.lineSeparator()
        output << "    public void setUpdateId(long updateId) { this.updateId = updateId; }" << System.lineSeparator()
        output << "    public long getUpdateId() { return this.updateId; }" << System.lineSeparator()
        output << ""<< System.lineSeparator();

        //simple case where there's no inheritance
        List<Field>     allFields = getAllFields(proto);

        Field   idField = null;
        List<Field>     directProtoFields = new ArrayList<>();
        List<Field>     collectionFields = new ArrayList<>();
        for(Field field : allFields) {

            Annotation[] notes = field.getAnnotations();
            String fieldTypeName = swapType(field.getGenericType());
            boolean sequence = false;
            for(Annotation note: notes) {
                if (note.annotationType().getName().equals(RepositoryId.class.getName())) {
                    idField = field;
                    if (idField.getType().isPrimitive()) {
                        // convert to non primitive
                        fieldTypeName = swapPrimitiveForNon( idField.getType());
                    }
                }
                if (note.annotationType().getName().equals(transgenic.lauterbrunnen.lateral.domain.Sequence.class.getName())) {
                    sequence=true;
                }
            }

            if (classMap.containsKey(field.getType().getTypeName())) {
                directProtoFields.add(field);
            }

            //check if the type is referenced as a generic
            boolean found = false;
            iterateGenerics(field.getGenericType(), new Consumer<Type> () {
                void accept(Type t) {
                    String name = t.getTypeName();
                    if (t instanceof ParameterizedType) {
                        ParameterizedType pt = (ParameterizedType)t;
                        name = pt.getRawType().getTypeName();
                    }

                    if (classMap.containsKey(name)) {
                        found =true;
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
                    for(Class<?> iface : typeClass.getInterfaces()) {
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

            if (sequence)
                output << "    @Sequence" <<System.lineSeparator()
            output << "    private " + fieldTypeName + " " + field.getName() + ";"<< System.lineSeparator()
        }

        //If no id field is annotated , create one
        if (idField==null) {
            output << "    private UniqueId repositoryId;"<< System.lineSeparator();
            output << ""<< System.lineSeparator();
            output << "    @Override"<< System.lineSeparator();
            output << "    public UniqueId getRepositoryId() {" + System.lineSeparator() +
                    "        return repositoryId;" + System.lineSeparator() +
                    "    }"<< System.lineSeparator();
            //we need a setter in this case
            output << "    public void setRepositoryId( UniqueId repositoryId ) {" << System.lineSeparator()
            output << "        this.repositoryId = repositoryId;" << System.lineSeparator()
            output << "    }" << System.lineSeparator()
        } else {

            output << ""<< System.lineSeparator();
            output << "    @Override"<< System.lineSeparator();

            //20161129 if the idfield is a primitive type make it a non primitive type in the impl
            // [in progress]
            String ftn = idField.getType().getTypeName();
            if (idField.getType().isPrimitive()) {
                ftn = swapPrimitiveForNon( idField.getType());
            }
            output << "    public " << ftn << " getRepositoryId() {" + System.lineSeparator() +
                    "        return get" + convertFirstCharToUpper(idField.getName()) + "();" + System.lineSeparator() +
                    "    }"<< System.lineSeparator();
        }

        output << ""<< System.lineSeparator()
        output << "    public EntityReference<" + proto.getSimpleName() + "Impl> getReference() {"<< System.lineSeparator();
        output << "        return new " + proto.getSimpleName() + "Reference(this);"<< System.lineSeparator();
        output << "    }"<< System.lineSeparator()

        generateGettersAndSetters(output, proto, allFields, idField);
        generateTraversal(output, proto, directProtoFields, collectionFields);

        //hashcode
        //
        output << ""<< System.lineSeparator()
        output << "    @Override"<< System.lineSeparator()
        output << "    public int hashCode() {"<< System.lineSeparator()
        output << "        return getRepositoryId() == null ? -1 : getRepositoryId().hashCode();"<< System.lineSeparator()
        output << "    }"<< System.lineSeparator()

        String implName = proto.getSimpleName() + "Impl";
        String refName = proto.getSimpleName() + "Reference";
        String varImpl = implName.substring(0,1).toLowerCase() + implName.substring(1);

        output << ""<< System.lineSeparator()
        output << "    @Override"<< System.lineSeparator()
        output << "    public boolean equals(Object other) {" + System.lineSeparator() +
                "        if (getRepositoryId()==null) return false;"+ System.lineSeparator() +
                "        if (other instanceof " + implName +") {" + System.lineSeparator() +
                "            " +implName + " " + varImpl + " = (" + implName +")other;" + System.lineSeparator() +
                "            return " + varImpl + ".getRepositoryId().equals(getRepositoryId());" + System.lineSeparator() +
                "        }" + System.lineSeparator()+
                "" + System.lineSeparator()+
                "        if (other instanceof " + refName +") {" + System.lineSeparator()+
                "            "+refName+ " reference = (" + refName + ")other;" + System.lineSeparator()+
                "            return reference.getRepositoryId().equals(getRepositoryId());" + System.lineSeparator()+
                "        }" + System.lineSeparator()+
                "        return false;" + System.lineSeparator() +
                "    }"

        output << "}" << System.lineSeparator()
    }

    protected String convertFirstCharToUpper(String name) {
        return name.substring(0,1).toUpperCase() + name.substring(1);
    }

    protected void generateGettersAndSetters(def output, Class proto, List<Field> allFields, Field idField) {
        for(Field field : allFields) {

            if (field.getName().startsWith("is")) {
                String shortName = field.getName().replace("is", "");
                String shortNameFirstLower = shortName.substring(0,1).toLowerCase() + shortName.substring(1);

                output << ""<< System.lineSeparator();
                output << "    @Override"<< System.lineSeparator()
                output << "    public " + swapType(field.getGenericType()) + " " + field.getName() + "() {"<< System.lineSeparator();
                output << "        return this." + field.getName() + ";"<< System.lineSeparator();
                output << "    }"<< System.lineSeparator();

                output << ""<< System.lineSeparator();
                output << "    @Override"<< System.lineSeparator()
                output << "    public void set" + shortName + "(" + swapType(field.getGenericType()) + " " + shortNameFirstLower + ") {"<< System.lineSeparator();
                output << "        this." + field.getName() + " = " + shortNameFirstLower + ";"<< System.lineSeparator();
                output << "    }"<< System.lineSeparator();


            } else {

                String tn = swapType(field.getGenericType());
                if (field==idField) {
                    if (idField.getType().isPrimitive()) {
                        tn = swapPrimitiveForNon(idField.getType());
                    }
                }

                output << ""<< System.lineSeparator();
                output << "    @Override"<< System.lineSeparator()
                output << "    public " + tn + " get" + convertFirstCharToUpper(field.getName()) + "() {"<< System.lineSeparator()
                output << "        return this." + field.getName() + ";"<< System.lineSeparator()
                output << "    }"<< System.lineSeparator()

                output << ""<< System.lineSeparator()
                output << "    @Override"<< System.lineSeparator()
                output << "    public void set" + convertFirstCharToUpper(field.getName()) + "(" + tn + " " + field.getName() + ") {"<< System.lineSeparator()
                output << "        this." + field.getName() + " = " + field.getName() + ";"<< System.lineSeparator()
                output << "    }"<< System.lineSeparator()
            }
        }
    }

    public void generateTraversal(def output, Class proto, List<Field> directProtoFields, List<Field> collectionFields) {

        output << ""<< System.lineSeparator();
        output << "    public void traverse(EntityTraversalFunction entityTraversalFunction," + System.lineSeparator() +
                "                         Collection<EntityImpl> collection) throws PersistenceException {"<< System.lineSeparator();

        //logic per direct field
        for(Field field : directProtoFields) {
            output << ""<< System.lineSeparator()
            output << "        // " + field.getName() << System.lineSeparator()
            output << "        " + swapType(field.getGenericType()) + " " + field.getName() + " = get" + convertFirstCharToUpper(field.getName()) + "();"<< System.lineSeparator()
            output << "        if ( " + field.getName() + " !=null ) {"<< System.lineSeparator();
            output << "            entityTraversalFunction.action(this, " + field.getName() + ", collection);"<< System.lineSeparator()
            output << "            if ( " + field.getName() + " instanceof " + swapType(field.getType()) + "Impl ) {"<< System.lineSeparator();
            output << "                " + swapType(field.getType()) + "Reference " + field.getName() + "Reference = new " + swapType(field.getType()) +"Reference((" +
                    swapType(field.getType()) + "Impl) " + field.getName() + ");"<< System.lineSeparator()
            output << "                set" + convertFirstCharToUpper(field.getName()) + "(" + field.getName() + "Reference);"<< System.lineSeparator()
            output << "            }"<< System.lineSeparator()
            output << "        }"<< System.lineSeparator()

        }

        //ditto collections
        for(Field field: collectionFields) {
            output << ""<< System.lineSeparator()
            output << "        // " + field.getName()<< System.lineSeparator()
            output << "        " + swapType(field.getGenericType()) + " " + field.getName() + " = get" + convertFirstCharToUpper(field.getName()) + "();"<< System.lineSeparator()
            output << "        if ( " + field.getName() + " !=null ) {"<< System.lineSeparator()
            output << "            entityTraversalFunction.action(this, " + field.getName() + ", collection);"<< System.lineSeparator()
            output << "        }"<< System.lineSeparator()
        }

        //ditto maps
        //and do ourselves to finish
        output << ""<< System.lineSeparator()
        output << "        //ourselves "<< System.lineSeparator()
        output << "        entityTraversalFunction.action(this, this, collection);"<< System.lineSeparator()
        output << "    }"<< System.lineSeparator()

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

    private boolean isCollection(Type type) {
        String name = type.getTypeName();
        if (type instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) type;
            name = pt.getRawType().getTypeName();
        }
        Class c= Class.forName(name);

        for(Class<?> iface: c.getInterfaces()) {
            if ("java.util.Collection".equals(iface.getName())) {
                return true;
            }
        }

        return false;
    }

    private String  createTypeName(Type[] types) {
        String retval = "";
        boolean first = true;
        for(Type t: types) {
            if (!first) {
                retval+=",";
            }
            first=false;
            retval += t.getTypeName();
            if (t instanceof ParameterizedType) {
                ParameterizedType pt =(ParameterizedType)t;
                Type[]      typeargs = pt.getActualTypeArguments();
                if (typeargs!=null && typeargs.length>0) {
                    retval += "<";
                    retval += createTypeName(typeargs);
                    retval += ">";
                }
            }
        }
        return retval;
    }

    def String swapPrimitiveForNon(Class prim) {
        if (prim.equals(Boolean.TYPE)) {
            return "Boolean";
        }
        if (prim.equals(Character.TYPE)) {
            return "Character";
        }
        if (prim.equals(Byte.TYPE)) {
            return "Byte";
        }
        if (prim.equals(Short.TYPE)) {
            return "Short";
        }
        if (prim.equals(Integer.TYPE)) {
            return "Integer";
        }
        if (prim.equals(Long.TYPE)) {
            return "Long";
        }
        if (prim.equals(Float.TYPE)) {
            return "Float";
        }
        if (prim.equals(Double.TYPE)) {
            return "Double";
        }
    }
}
