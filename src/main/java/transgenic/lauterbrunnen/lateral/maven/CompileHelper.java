package transgenic.lauterbrunnen.lateral.maven;

import javax.tools.*;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by stumeikle on 21/06/16.
 */
public class CompileHelper {

    public static void compile(String root, String dest, String classpath) {

        //find all the java files under 'root'
        List<String> javaFiles = findJavaFiles(root);

        for(String file: javaFiles) {
            System.out.println("" + file);
        }

        System.out.println("Destination =" + dest);

        try {
            //compile the given path to class files
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
            StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
            Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromStrings(
                    javaFiles);
            JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics,
                    Arrays.asList("-d",dest,"-cp",classpath),
                    null, compilationUnits);
            boolean success = task.call();
            fileManager.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private static List<String> findJavaFiles(String root) {
        List<String> retval = new ArrayList<>();
        Path path = FileSystems.getDefault().getPath(root);
        try {
            Files.walkFileTree(path, new FileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (file.toFile().getPath().endsWith(".java")) {
                        retval.add(file.toFile().getPath());
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch(Exception e) {}


        return retval;
    }
}
