package transgenic.lauterbrunnen.lateral.domain;

import com.google.common.reflect.ClassPath;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by stumeikle on 29/05/16.
 */
public class PackageScanner {

    public static List<Class> getClasses(String packageName) {
        List<Class> retval= new ArrayList<>();

        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try {

            String safePackageName = packageName;

            if (!safePackageName.endsWith(".")) {
                safePackageName = packageName + ".";
            }

            ClassPath classpath = ClassPath.from(loader); // scans the class path used by classloader
            for (ClassPath.ClassInfo classInfo : classpath.getAllClasses()) { //classpath.getTopLevelClasses(packageName)) {

                if (!classInfo.getName().startsWith(safePackageName)) continue;
                if(!classInfo.getSimpleName().endsWith("_")){
                    retval.add(classInfo.load());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return retval;
    }
}
