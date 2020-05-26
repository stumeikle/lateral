package transgenic.lauterbrunnen.lateral.domain.generator

import transgenic.lauterbrunnen.lateral.domain.RepositoryId
import transgenic.lauterbrunnen.lateral.domain.UniqueId
import transgenic.lauterbrunnen.lateral.domain.validation.Validate

import java.lang.annotation.Annotation
import java.lang.reflect.Field

/**
 * Created by stumeikle on 04/06/16.
 */
class GenerateRef extends GenerateImpl{

    protected Field idField = null;
    protected String className;
    protected String classNameRef;
    protected String classNameImpl;
    protected String classNameLowerFirst;

    public void generateRef(Class proto) {
        def fn = basePath + "/" + outputPackage.replaceAll("\\.","/") + "/" + proto.getSimpleName() + "Reference.java";
        println "Writing " + fn;
        def output = new File(fn);

        output << "package " + outputPackage + ";" << System.lineSeparator()
        output << "" << System.lineSeparator();
        output << "//DO NOT MODIFY, this class was generated by xxx " << System.lineSeparator();
        output << ""<< System.lineSeparator();
        output << "import transgenic.lauterbrunnen.lateral.domain.EntityReference;" << System.lineSeparator()
        output << "import transgenic.lauterbrunnen.lateral.domain.validation.ValidationException;" << System.lineSeparator()
        output << "import java.io.Serializable;" << System.lineSeparator()
        output << "import static transgenic.lauterbrunnen.lateral.Lateral.inject;" << System.lineSeparator()
        output << "" << System.lineSeparator()

        className = proto.getSimpleName();
        classNameRef = className + "Reference";
        classNameImpl = className + "Impl";
        classNameLowerFirst = className.substring(0,1).toLowerCase() + className.substring(1);

        output << "public class " << classNameRef << " implements " << className << ", Serializable, EntityReference<" << classNameImpl << "> {" <<System.lineSeparator()
        output << System.lineSeparator()

        //find the repository id class
        List<Field> allFields = getAllFields(proto);
        setIdField(allFields);
        String idfieldType = (idField!=null?swapType(idField.getGenericType()) : UniqueId.class.getName() );

        //and more
        if (idField!=null && idField.getType().isPrimitive()) {
            idfieldType = swapPrimitiveForNon(idField.getType());
        }

        output << "    private " + idfieldType + " repositoryId;" << System.lineSeparator()
        output << "    private transient " + classNameImpl + " proxee;" << System.lineSeparator()
        //NOTE transient needed on the next else hazelcast will try to persist the repository
        output << "    private transient final " + className + "Repository " + classNameLowerFirst + "Repository = inject(" + className + "Repository.class," + diContext+"Context.class);" << System.lineSeparator()
        output << System.lineSeparator()

        output << "    public " << classNameRef << "(" << classNameImpl << " " << classNameLowerFirst << ") {" << System.lineSeparator()
        output << "        this.repositoryId = (" + idfieldType + ") " << classNameLowerFirst << ".getRepositoryId();" << System.lineSeparator()
        //Good idea or not?
        //output << "        this.proxee = " << classNameLowerFirst << ";" << System.lineSeparator()
        output << "    }" << System.lineSeparator()
        output << System.lineSeparator()

        output << "    public " << classNameRef << "(" << idfieldType << " repositoryId ) {" << System.lineSeparator()
        output << "        this.repositoryId = repositoryId;" << System.lineSeparator()
        output << "    }" << System.lineSeparator()
        output << System.lineSeparator()

        output << "    public " << classNameImpl << " getProxee() {" << System.lineSeparator()
        output << "        return proxee;" << System.lineSeparator()
        output << "    }" << System.lineSeparator()
        output << System.lineSeparator();
        output << "    public " << idfieldType << " getRepositoryId() { " << System.lineSeparator()
        output << "        return repositoryId;" << System.lineSeparator()
        output << "    }" << System.lineSeparator()

        generateGettersAndSetters(output, proto, allFields, idField);
        generateTail(output,proto);
    }

    public void setIdField(List<Field> allFields) {
        idField = null;
        for(Field field : allFields) {

            Annotation[] notes = field.getAnnotations();
            for (Annotation note : notes) {
                if (note.annotationType().getName().equals(RepositoryId.class.getName())) {
                    idField = field;
                }
            }
        }
    }

    protected void generateGettersAndSetters(def output, Class proto, List<Field> allFields, Field idField) {
        for(Field field : allFields) {

            if (field.getName().startsWith("is")) {
                String shortName = field.getName().replace("is", "");
                String shortNameFirstLower = shortName.substring(0,1).toLowerCase() + shortName.substring(1);

                output << ""<< System.lineSeparator();
                output << "    @Override"<< System.lineSeparator()
                output << "    public " + swapType(field.getGenericType()) + " " + field.getName() + "() {"<< System.lineSeparator();
                output << "        if (proxee==null) load();"<< System.lineSeparator()
                output << "        return proxee." + field.getName() + "();"<< System.lineSeparator()
                output << "    }"<< System.lineSeparator();

                output << ""<< System.lineSeparator();
                output << "    @Override"<< System.lineSeparator()
                output << "    public void set" + shortName + "(" + swapType(field.getGenericType()) + " " + shortNameFirstLower + ") {"<< System.lineSeparator();
                output << "        if (proxee==null) load();"<< System.lineSeparator()
                output << "        proxee.set" + shortName + "(" + shortNameFirstLower + ");"<< System.lineSeparator()
                output << "    }"<< System.lineSeparator();


            } else {

                String tn = swapType(field.getGenericType());
                if (field==idField) {
                    if (idField.getType().isPrimitive()) {
                        tn = swapPrimitiveForNon(idField.getType());
                    }
                }

                Annotation[] notes = field.getAnnotations();
                boolean validate = false;
                for(Annotation note: notes) {
                    if (note.annotationType().getName().equals(Validate.class.getName())) {
                        validate = true;
                    }
                }
                String      validateString = validate==true ? "throws ValidationException " : "";

                output << ""<< System.lineSeparator();
                output << "    @Override"<< System.lineSeparator()
                output << "    public " + tn + " get" + convertFirstCharToUpper(field.getName()) + "() {"<< System.lineSeparator()
                output << "        if (proxee==null) load();"<< System.lineSeparator()
                output << "        return proxee.get" + convertFirstCharToUpper(field.getName()) + "();"<< System.lineSeparator()
                output << "    }"<< System.lineSeparator()

                output << ""<< System.lineSeparator()
                output << "    @Override"<< System.lineSeparator()
                output << "    public void set" + convertFirstCharToUpper(field.getName()) + "(" + tn + " " + field.getName() + ") " << validateString << "{"<< System.lineSeparator()
                output << "        if (proxee==null) load();"<< System.lineSeparator()
                output << "        proxee.set" + convertFirstCharToUpper(field.getName()) + "(" + field.getName() + ");"<< System.lineSeparator()
                output << "    }"<< System.lineSeparator()
            }
        }
    }

    public generateTail(def output, Class proto) {
        output << "" << System.lineSeparator()
        output << "    private void load() {" << System.lineSeparator()
        output << "        " << classNameImpl << " " << classNameLowerFirst << " = " << classNameLowerFirst <<"Repository.retrieve(repositoryId);" << System.lineSeparator()
        output << "        proxee = " <<  classNameLowerFirst << ";" << System.lineSeparator()
        output << "    }" << System.lineSeparator()

        output << "" << System.lineSeparator()
        output << "    public int hashCode() {" << System.lineSeparator()
        output << "        return repositoryId.hashCode();" << System.lineSeparator()
        output << "    }" << System.lineSeparator()
        output << "" << System.lineSeparator()

        output << "    public boolean equals(Object other) {" << System.lineSeparator()
        output << "        if (other instanceof " << classNameImpl << ") {" <<System.lineSeparator()
        output << "            " << classNameImpl << " " << classNameLowerFirst << "Impl = (" << classNameImpl << ")other;"<<System.lineSeparator()
        output << "            return " << classNameLowerFirst << "Impl.getRepositoryId().equals(getRepositoryId());" << System.lineSeparator()
        output << "        }" << System.lineSeparator()
        output << "" << System.lineSeparator()
        output << "        if (other instanceof " << classNameRef << ") {" <<System.lineSeparator()
        output << "            " << classNameRef << " reference = (" << classNameRef << ")other;"<<System.lineSeparator()
        output << "            return reference.getRepositoryId().equals(getRepositoryId());" << System.lineSeparator()
        output << "        }" << System.lineSeparator()
        output << "        return false;" << System.lineSeparator()
        output << "    }" << System.lineSeparator();
        output << "}" << System.lineSeparator()

    }


    /*
    private void load() {
        ContactDetailsImpl cd = contactDetailsRepository.retrieve(repositoryId);
        proxee = cd;
    }

    public int hashCode() {
        return repositoryId.hashCode();
    }

    public boolean equals(Object other) {
        if (other instanceof ContactDetailsImpl) {
            ContactDetailsImpl contactDetailsImpl = (ContactDetailsImpl)other;
            return contactDetailsImpl.getRepositoryId().equals(getRepositoryId());
        }

        if (other instanceof ContactDetailsReference) {
            ContactDetailsReference reference = (ContactDetailsReference)other;
            return reference.getRepositoryId().equals(getRepositoryId());
        }
        return false;
    }
     */
}


