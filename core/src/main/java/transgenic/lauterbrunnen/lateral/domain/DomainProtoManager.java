package transgenic.lauterbrunnen.lateral.domain;

import transgenic.lauterbrunnen.lateral.domain.internal._Sequence;
import transgenic.lauterbrunnen.lateral.plugin.AnnotationScanner;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * Created by stumeikle on 13/01/17.
 */
public class DomainProtoManager {

    private static final String internalPackage="transgenic.lauterbrunnen.lateral.domain.internal";
    private Properties properties;
    private String entityPackage;
    private List<Class> protoClasses;
    private List<Class> externalProtoClasses;

    public DomainProtoManager(Properties properties) {
        this.properties = properties;

        //find the classes and add in any system classes
        entityPackage = (String) properties.get("domain.proto.package");
        protoClasses = new ArrayList<>();
        protoClasses.addAll(PackageScanner.getClasses(entityPackage));

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
    }

    public List<Class> getProtoClasses() {
        return protoClasses;
    }

    public boolean  containsClass(Class c) {
        return protoClasses.contains(c);
    }

    public String stripPackageName(Class c) {
        String ep = entityPackage + ".";
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
