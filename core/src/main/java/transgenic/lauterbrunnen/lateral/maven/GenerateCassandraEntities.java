package transgenic.lauterbrunnen.lateral.maven;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import transgenic.lauterbrunnen.lateral.entity.generator.GenerateCassandraEntityTask;
import transgenic.lauterbrunnen.lateral.entity.generator.GenerateEntityTask;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Properties;

/**
 * Created by stumeikle on 01/07/20.
 */
@Mojo(name="generateCassandraEntities")
public class GenerateCassandraEntities extends AbstractMojo {

    @Parameter(property = "generateEntities.srcpath", defaultValue = "src/main/java")
    private String srcPath;

    @Parameter(property = "generateEntities.resourcespath", defaultValue = "src/main/resources")
    private String resourcesPath;

    @Parameter(property = "generateEntities.gendsrcpath", defaultValue = "target/generated-sources")
    private String generatedSourcesPath;

    @Parameter(property = "generateEntities.buildpath", defaultValue = "target/classes")
    private String buildPath;

    @Parameter(defaultValue = "${project}")
    private MavenProject project;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        //20171028 looks like we need to tweak the paths
        srcPath = CompileHelper.fixPath(srcPath, project);
        generatedSourcesPath = CompileHelper.fixPath(generatedSourcesPath, project);
        resourcesPath = CompileHelper.fixPath(resourcesPath, project);
        buildPath = CompileHelper.fixPath(buildPath, project);

        getLog().info("Generated sources path = " + generatedSourcesPath);

        //Can we find all the generate*.properties files here?
        File dir = new File(resourcesPath + "/");
        FileFilter fileFilter = new WildcardFileFilter("generate*.properties");
        File[] files = dir.listFiles(fileFilter);
        //If there's more than one ensure each has a lateral.di.context
        //line --> update, let's just say all files need to define the context
        boolean allFilesContainContext = true;
        for (int i = 0; i < files.length; i++) {
            getLog().info("Found generate properties file = " + files[i].getName());

            Properties properties = new Properties();
            try {
                InputStream inputStream = new FileInputStream(files[i]);
                properties.load(inputStream);
                if (properties.getProperty("lateral.di.context") == null) {
                    allFilesContainContext = false;
                    getLog().error("File '" + files[i].getName() + "' should define lateral.di.context. I can't proceed without this");
                    System.exit(0);
                    break;
                }
            } catch (Exception e) {
                allFilesContainContext = false;
                break;
            }
        }

        //Extend the classpath ---------------------------------------
        String tmpBuildPath = buildPath;
        ClassLoader currentThreadClassLoader = Thread.currentThread().getContextClassLoader();
        URLClassLoader urlClassLoader = null;
        try {
            File furl = new File(tmpBuildPath);
            String url = "file:///" + furl.getCanonicalPath().replaceAll("\\\\", "/");

//                MUST have a / on the end
            if (!url.endsWith("/")) url = url + "/";

            urlClassLoader = new URLClassLoader(new URL[]{new URL(url)}, currentThreadClassLoader);
            getLog().info("Extending classpath to include " + url);

            Thread.currentThread().setContextClassLoader(urlClassLoader);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Now repeat the next for each generate*.properties file which is present
        for (int i = 0; i < files.length; i++) {

            File f = files[i];

            GenerateCassandraEntityTask get = new GenerateCassandraEntityTask();
            get.setGeneratedSourcesPath(generatedSourcesPath);
            get.setPropertyFile(f);
            get.generate();

            //(4) add all the files to be compiled by maven
            project.addCompileSourceRoot(generatedSourcesPath);
        }

    }
}