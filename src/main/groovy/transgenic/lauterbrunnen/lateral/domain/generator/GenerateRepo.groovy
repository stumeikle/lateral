package transgenic.lauterbrunnen.lateral.domain.generator

import transgenic.lauterbrunnen.lateral.domain.UniqueId

import java.lang.reflect.Field

/**
 * Created by stumeikle on 04/06/16.
 */
class GenerateRepo extends GenerateRef{

    public void generateRepo(Class proto) {
        def fn = basePath + "/" + outputPackage.replaceAll("\\.","/") + "/" + proto.getSimpleName() + "Repository.java";
        println "Writing " + fn;
        def output = new File(fn);

        output << "package " + outputPackage + ";" << System.lineSeparator()
        output << "" << System.lineSeparator();
        output << "//DO NOT MODIFY, this class was generated by xxx " << System.lineSeparator();
        output << ""<< System.lineSeparator();
        output << "import transgenic.lauterbrunnen.lateral.domain.CRUDRepository;" << System.lineSeparator()
        output << ""<< System.lineSeparator()

        //we need the idfield
        List<Field> allFields = getAllFields(proto);
        setIdField(allFields);
        String idfieldType = (idField!=null?swapType(idField.getGenericType()) : UniqueId.class.getName() );
        //and more
        if (idField!=null && idField.getType().isPrimitive()) {
            idfieldType = swapPrimitiveForNon(idField.getType());
        }

        output << "public interface " + proto.getSimpleName() + "Repository extends CRUDRepository<" + proto.getSimpleName() + "Impl," <<
                idfieldType << "> {"<< System.lineSeparator()
        output << "}" << System.lineSeparator()
    }
}
