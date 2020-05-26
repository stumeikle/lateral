package transgenic.lauterbrunnen.lateral.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import transgenic.lauterbrunnen.lateral.entity.generator.GenerateEntityTask;
import transgenic.lauterbrunnen.lateral.rest.generator.GenerateRestPojosTask;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Created by stumeikle on 10/11/16.
 */
@Mojo(name="generateRestPojos")
public class GenerateRestPojos extends AbstractMojo {

    @Parameter( property="generateRestPojos.srcpath", defaultValue = "src/main/java")
    private String srcPath;

    @Parameter( property="generateRestPojos.resourcespath", defaultValue = "src/main/resources")
    private String resourcesPath;

    @Parameter( property="generateRestPojos.gendsrcpath", defaultValue = "target/generated-sources")
    private String generatedSourcesPath;

    @Parameter( property="generateRestPojos.buildpath", defaultValue = "target/classes")
    private String buildPath;

    @Parameter(defaultValue = "${project}")
    private MavenProject project;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        //20171028 looks like we need to tweak the paths
        srcPath = CompileHelper.fixPath(srcPath, project);
        generatedSourcesPath = CompileHelper.fixPath(generatedSourcesPath, project);
        resourcesPath = CompileHelper.fixPath(resourcesPath, project);
        buildPath = CompileHelper.fixPath(buildPath,project);

        getLog().info( "Generated sources path = " + generatedSourcesPath);

        File f= new File(resourcesPath + "/generate.properties");

        //Extend the classpath ---------------------------------------
        String tmpBuildPath=buildPath;
        ClassLoader currentThreadClassLoader = Thread.currentThread().getContextClassLoader();
        URLClassLoader urlClassLoader =null;
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

        GenerateRestPojosTask get = new GenerateRestPojosTask();
        get.setGeneratedSourcesPath(generatedSourcesPath);
        get.setPropertyFile(f);
        get.generate();

        //(4) add all the files to be compiled by maven
        project.addCompileSourceRoot(generatedSourcesPath);
    }
}