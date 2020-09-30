package transgenic.lauterbrunnen.lateral.domain;

import transgenic.lauterbrunnen.lateral.domain.internal._Sequence;
import transgenic.lauterbrunnen.lateral.plugin.AnnotationScanner;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Created by stumeikle on 13/01/17.
 */
public class DomainProtoManager {

    private static final String internalPackage="transgenic.lauterbrunnen.lateral.domain.internal";
    private Properties properties;
    private String protoPackage;
    private List<Class> protoClasses;
    private List<Class> externalProtoClasses;
    private Set<String> protoSubPackages;
    private Map<String, String> proto2SubPackageMap = new HashMap<>();

    public DomainProtoManager(Properties properties) {
        this.properties = properties;

        //find the classes and add in any system classes
        protoPackage = (String) properties.get("domain.proto.package");
        protoClasses = new ArrayList<>();
        protoClasses.addAll(PackageScanner.getClasses(protoPackage));

       //Skip the enums
        Iterator<Class> iterator = protoClasses.iterator();
        while (iterator.hasNext()) {
            Class c = iterator.next();
            if (c.isEnum()) {
                iterator.remove();
            }
        }

        externalProtoClasses = new ArrayList<>();
        externalProtoClasses.addAll(protoClasses);

        //Add the internal classes. TODO some of these will only be needed in particular circumstances
        //Could add a nice mechanism here to link annotation presense to inclusion of internal classes
        //For now lets take the simplest path
        protoClasses.addAll(PackageScanner.getClasses(internalPackage));
        if (!annotationExists(externalProtoClasses, Sequence.class)) {
            protoClasses.remove(_Sequence.class);
        }

        protoSubPackages = new HashSet<>();
        for(Class c: protoClasses) {
            String protoName = c.getName();
            protoName = protoName.replace(protoPackage + ".","");

            if (protoName.contains(".")) {
                protoName = protoName.replace("." + c.getSimpleName(),"");
                protoSubPackages.add(protoName);
                proto2SubPackageMap.put(c.getSimpleName(), protoName);
            }
        }
    }

    public String getSubPackageForProto(String proto) {
        String retval = proto2SubPackageMap.get(proto);
        if(retval==null) {
            retval="";
        }

        return retval;
    }

    public Set<String> getProtoSubPackages() {
        return this.protoSubPackages;
    }

    public List<Class> getProtoClasses() {
        return protoClasses;
    }

    public String getProtoPackage() {
        return this.protoPackage;
    }

    public boolean  containsClass(Class c) {
        return protoClasses.contains(c);
    }

    public String stripPackageName(Class c) {
        String ep = protoPackage + ".";
        String ip = internalPackage + ".";

        if (c.getName().startsWith(ep)) {
            return c.getName().replaceFirst(ep, "");
        }
        if (c.getName().startsWith(ip)) {
            return c.getName().replaceFirst(ip, "");
        }
        return "";
    }

    public List<Class> getProtoClassesNoInternals() {
        return externalProtoClasses;
    }

    public String getEntityName(Class proto) {
        if (proto.getSimpleName().startsWith("_")) {
            return proto.getSimpleName().replaceFirst("_", "Lateral");
        }
        return proto.getSimpleName();
    }

    private boolean annotationExists(List<Class> classes, Class annotationClass) {

        for(Class c: classes) {
            for(Annotation note: c.getAnnotations()) {
                if (note.annotationType().getName().equals(annotationClass.getName())) {
                    return true;
                }
            }

            //check the fields
            for(Field f: c.getDeclaredFields()) {
                for(Annotation note: f.getAnnotations()) {
                    if (note.annotationType().getName().equals(annotationClass.getName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
