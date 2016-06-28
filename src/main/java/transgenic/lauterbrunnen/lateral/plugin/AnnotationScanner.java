package transgenic.lauterbrunnen.lateral.plugin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by Stuart.meikle on 05/05/2016.
 */
public enum AnnotationScanner {

    INSTANCE;

    private final Log LOG = LogFactory.getLog(AnnotationScanner.class);
    private final Map<Class, List<Class>> annotatedClassMap;
    private volatile boolean scanComplete = false;

    private AnnotationScanner() {
        annotatedClassMap = new ConcurrentHashMap<>();
        try {
            getClasses("",".ejb.", "transgenic.lauterbrunnen" );//transgenic.lauterbrunnen", ".ejb.");
            getClasses("transgenic.lauterbrunnen",".ejb.", "transgenic.lauterbrunnen" );
            scanComplete = true;
        } catch (IOException e) {
            LOG.error("Exception thrown when scanning for annotations");
        }
    }

    public List<Class> get(Class note) {
        return annotatedClassMap.get(note);
    }

    private void getClasses(String packageName, String filter, String noteFilter) throws IOException {
        if (scanComplete) return;

        //attempt to add in the jars we need to the system classpath
        //my note. getClass().getProtectionDomain().getCodeSource().getLocation()
        ClassLoader systemClassLoader = Thread.currentThread().getContextClassLoader();
        ClassLoader classLoader = systemClassLoader;

        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        List<String> dirs = new ArrayList<>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();

            //COnverting to file is fine for normal files but for jars it fails.
            //So. just store the string
            dirs.add(resource.getFile());
        }
        ArrayList<String> classes = new ArrayList<String>();
        for (String directory : dirs) {
            classes.addAll(findClasses(directory, packageName, filter));
        }

        for(String cn: classes) {
            try {
                if (cn.startsWith(".")) cn = cn.substring(1);
                Class c = Class.forName(cn, false, classLoader);

                for( Annotation note: c.getAnnotations()) {
                    if (!(note.annotationType().getName().contains(noteFilter))) continue;

                    List<Class>  list = annotatedClassMap.get(note.annotationType());
                    if (list==null) {
                        list = new ArrayList<Class>();
                        annotatedClassMap.put(note.annotationType(), list);
                    }

                    list.add(c);
                }
            } catch (Throwable e) {
                LOG.error("Unable to load class: " + cn, e);
            }
        }
    }

    private List<String> findClasses(String directory, String packageName, String filter)  {

        //tidy
        if (directory.startsWith("jar:")) {
            directory=directory.substring(4);
        }
        if (directory.startsWith("file:")) {
            directory=directory.substring(5);
        }

        //need to decide if we have been passed a jar or a real directory here
        if (directory.contains(".jar!")) {
            String separator = FileSystems.getDefault().getSeparator();
            String path =separator + packageName.replace('.', separator.charAt(0));

            //check if the path is at the end
            if (directory.lastIndexOf(path) == directory.length()-path.length()) {
                //yes. so strip it off
                directory = directory.substring(0,directory.length()-path.length()-1);
            }
        }

        //if it is a directory handle 1 way, if a jar handle another
        File workingDir = new File(directory);
        if (workingDir.isDirectory()) {
            return findClassesInDir(workingDir, packageName, filter);
        } else {
            return findClassesInJar(workingDir, packageName, filter);
        }
    }

    private List<String> findClassesInDir(File directory, String packageName, String filter)
    {
        List<String> classes = new ArrayList<String>();
        if (!directory.exists()) {
            return classes;
        }

        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                classes.addAll(findClasses(file.getPath(), packageName + "." + file.getName(), filter));
            } else if (file.getName().endsWith(".class")) {
                String name = null;
                name = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
                if (!name.contains(filter))
                    classes.add(name);
            }
        }
        return classes;
    }

    private List<String> findClassesInJar(File jarFile, String packageName, String filter) {
        List<String> classNames = new ArrayList<String>();

        try {
            ZipInputStream zip = new ZipInputStream(new FileInputStream(jarFile));

            for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
                if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
                    // This ZipEntry represents a class. Now, what class does it represent?
                    String className = entry.getName().replace('/', '.'); // including ".class"
                    className = className.substring(0, className.length() - ".class".length());
                    if (!className.contains(filter))
                        classNames.add(className);
                }
            }
        } catch (IOException e) {
        }

        return classNames;
    }
}
