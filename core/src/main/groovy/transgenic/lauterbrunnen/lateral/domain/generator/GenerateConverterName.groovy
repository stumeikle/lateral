package transgenic.lauterbrunnen.lateral.domain.generator

/**
 * Created by stumeikle on 05/11/16.
 */
class GenerateConverterName {

    private String converterMethodName;
    private String convertFrom;
    private String convertTo;
    private String shortFromClass;
    private String shortToClass;

    public int hashCode() { return converterMethodName.hashCode() };
    public boolean equals(Object other) {
        if (!(other instanceof GenerateConverterName)) return false;

        GenerateConverterName otherGcn = (GenerateConverterName)other;
        return converterMethodName.equals(otherGcn.converterMethodName);
    }

    public static GenerateConverterName createHook( String fromClass, String toClass ) {
        String shortFromClass = fromClass.substring(fromClass.lastIndexOf(".")+1);
        String shortToClass   = toClass.substring(toClass.lastIndexOf(".")+1);
        shortFromClass = shortFromClass.replaceAll(/\[\]/, "Array");
        shortToClass = shortToClass.replaceAll(/\[\]/, "Array");
        String converterName = "convert" + upperFirst(shortFromClass) + "To" + upperFirst(shortToClass);
        converterName = converterName.replaceAll( /\[\]/, "Array");

        GenerateConverterName retval = new GenerateConverterName();
        retval.converterMethodName = converterName;
        retval.convertFrom = fromClass;
        retval.convertTo = toClass;
        retval.shortFromClass = shortFromClass;
        retval.shortToClass = shortToClass;

        return retval;
    }

    public static String upperFirst(String input) {
        return input.substring(0,1).toUpperCase() + input.substring(1);
    }

    public String getConverterMethodName() {
        return converterMethodName;
    }

    public void writeHookMethod( def output ) {

        def fromVarname = shortFromClass.substring(0,1).toLowerCase() + shortFromClass.substring(1);

        output << "    public static " << convertTo << " " << converterMethodName <<
                "( " << convertFrom << " " << fromVarname << ", Function<" << convertFrom << ", " <<
                convertTo << "> f) {" << System.lineSeparator()
        output << "        return f.apply(" << fromVarname << ");" << System.lineSeparator()
        output << "    }" << System.lineSeparator()
        output << System.lineSeparator();
    }

}
