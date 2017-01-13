package transgenic.lauterbrunnen.lateral.domain;

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
        externalProtoClasses = new ArrayList<>();
        externalProtoClasses.addAll(protoClasses);

        //Add the internal classes. TODO some of these will only be needed in particular circumstances
        //protoClasses.addAll(PackageScanner.getClasses(internalPackage));

        //Skip the enums
        Iterator<Class> iterator = protoClasses.iterator();
        while (iterator.hasNext()) {
            Class c = iterator.next();
            if (c.isEnum()) {
                iterator.remove();
            }
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
}
