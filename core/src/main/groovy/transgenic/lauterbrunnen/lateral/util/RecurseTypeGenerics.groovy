package transgenic.lauterbrunnen.lateral.util

import transgenic.lauterbrunnen.lateral.domain.generator.GenerateConverterName

import java.lang.reflect.Field
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * Created by stumeikle on 13/11/16.
 *
 * Utility. Given a type like:
 *
 * List<List<Map<String,MyType1>>>  var1;
 *
 * I need to be able to convert it to
 * List<List<Map<String,MyOtherType>> var1;
 *
 * and I need to be able to get/set it thus, mapping on the way. ie
 *
 * setVar1( other.stream().map( otherList1var ->
 *                  otherList1var.stream().map( otherList2var ->
 *
 *                      otherList2Var.entrySet().stream().collect(Collectors.toMap(
 *                          entry ->
 *                              entry.getKey()
 *                         ,entry ->
 *                              myConversion( entry.getValue() )
 *                      )
 *
 *                  ).collect(Collectors.toList());
 *        ).collect(Collectors.toList())
 *
 * we may need to swap int to Integer and so on on the way too
 * and apply type conversion.
 *
 * 20161115 think this is overly complex for today. for now will proceed with copy paste
 */
class RecurseTypeGenerics {

    //transformer = output string
    //field = destination field. ie the variable which will be set.... NO the SOURCE FIELD
    //firstUpperFN = the name of the field with the first character capitalised
    //
    //needs: String sourceObject; == 'impl' in this example
    //needs: String destinationObject: == 'entity' in this example
    //needs: Boolean testForNull. True in this example. ie prefer the xxx maybe thats a feature of the
    //       calling code, come to think of it
    //
    //needs: A method to handle types which are in our domain. cut1 below
    //needs: domain package name
    //
    public void convertArbitraryInstanceVar(def transformer, Field field, String firstUpperFN) {
        Type type = field.getGenericType();

        //need to transform the fieldname
        String fufn = "get" + firstUpperFN;
        if (field.getType().getTypeName().equalsIgnoreCase("Boolean"))
        {
            fufn = field.getName();
        }

        String fieldName = "impl." + fufn + "()";

        //At this point fieldName is the expression to extract a field of the same name
        //as the destination from the source. eg impl.getName()

        //prefix need to be controlled by annotations TODO
        transformer << "        ";
        if (!field.getType().isPrimitive())
            transformer << "if (impl." << fufn << "()!=null) "
        transformer << "entity.set" + firstUpperFN + "("

        recurseSetLogicImplToEntity(transformer, type, fieldName);

        transformer << ");" << System.lineSeparator();
    }

    private void recurseSetLogicImplToEntity(def transformer, Type type, String fieldName) {

        String entityType = swapType(type);

        String typeName= type.getTypeName();
        if (type instanceof ParameterizedType) {
            typeName = ((ParameterizedType)type).getRawType().getTypeName();
        }

        if (typeName.contains(implPackage)) {

            //------------------ cut1 --------------------------------------------------
            String ref = type.getTypeName() + "Reference";

            //sometimes the repository id is eg unique id but we need to convert it to eg byte[]
            String reptype =idFields.get(type.getTypeName());
            String converter = properties.get("entity.type.converter." + reptype);
            if (converter!=null) {
                //transformer << converter;

                //getting pretty hideous
                //we need to
                //(1) establish the name of a linking method for the conversion
                //(2) add the name to a set of such names
                //(3) use that method plus the config to convert the type
                GenerateConverterName converterName = GenerateConverterName.createHook( reptype, entityType );

                //add to the set of known converternames
                converterNames.add(converterName);
                transformer << converterName.getConverterMethodName() << "( " <<
                        "((" + ref + ")" + fieldName + ").getRepositoryId()" << ", " <<
                        converter << ")";

            } else {
                //no conversion needed
                transformer << "((" + ref + ")" + fieldName + ").getRepositoryId()";
            }

            //cut1
            //-------------------------------------------------------------------------------

        } else {

            if (type instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) type;
                String rawname = pt.getRawType().getTypeName();

                handleListImplToEntity(transformer, rawname, fieldName, pt);
                handleMapImplToEntity( transformer, rawname, fieldName, pt);

            }
            else {
                String converter = properties.get("entity.type.converter." + type.getTypeName());
                if (converter != null) {
                    //we need the type associated with fieldName ...
                    GenerateConverterName converterName = GenerateConverterName.createHook( type.getTypeName(), entityType );
                    converterNames.add(converterName);
                    transformer << converterName.getConverterMethodName() << "( " <<
                            fieldName << ", " << converter << ")";
                } else {
                    transformer << fieldName;
                }
            }
        }
    }

    private void handleListImplToEntity( def transformer, String rawName, String fieldName, ParameterizedType parameterizedType) {
        try {

            Class c = Class.forName(rawName);
            boolean list = false;
            if (List.class.getName().equals(c.getName())) list = true;
            for(Class iface: c.getInterfaces()) {
                if (List.class.getName().equals(iface.getName())) {
                    list = true;
                    break;
                }
            }

            if (list) {
                //preamble
                transformer << fieldName << " == null ? null : "+ fieldName + ".stream().map( item -> " << System.lineSeparator();
                transformer << "            ";

                //then recurse into the type
                for (Type t : parameterizedType.getActualTypeArguments()) {
                    recurseSetLogicImplToEntity(transformer,t, "item");
                }

                //then postamble
                transformer << System.lineSeparator() << "        ).collect(Collectors.toList())"

            }

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void handleMapImplToEntity( def transformer, String rawName, String fieldName, ParameterizedType parameterizedType) {
        try {
            Class c = Class.forName(rawName);
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
                transformer << fieldName + " ==null ? null : " << fieldName + ".entrySet().stream().collect(Collectors.toMap( " << System.lineSeparator()
                transformer << "            e -> "

                //then recurse into the type
                Type keytype = parameterizedType.getActualTypeArguments()[0];
                recurseSetLogicImplToEntity(transformer, keytype, "e.getKey()");
                transformer << "," << System.lineSeparator()
                transformer << "            e-> ";
                Type valuetype = parameterizedType.getActualTypeArguments()[1];
                recurseSetLogicImplToEntity(transformer, valuetype,"e.getValue()");

                //then postamble
                transformer << "))"

            }

        } catch(Exception e) {}
    }


}
