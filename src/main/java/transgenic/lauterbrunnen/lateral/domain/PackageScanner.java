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

            ClassPath classpath = ClassPath.from(loader); // scans the class path used by classloader
            for (ClassPath.ClassInfo classInfo : classpath.getTopLevelClasses(packageName)) {
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
