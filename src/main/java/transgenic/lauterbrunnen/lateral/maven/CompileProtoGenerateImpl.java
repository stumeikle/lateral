package transgenic.lauterbrunnen.lateral.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import transgenic.lauterbrunnen.lateral.domain.generator.GenerateDomainTask;

import javax.tools.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Created by stumeikle on 20/06/16.
 */
@Mojo(name="compileProtoGenerateImpl")
public class CompileProtoGenerateImpl extends AbstractMojo {

    @Parameter( property="compileProtoGenerateImpl.srcpath", defaultValue = "src/main/java")
    private String srcPath;

    @Parameter( property="compileProtoGenerateImpl.resourcespath", defaultValue = "src/main/resources")
    private String resourcesPath;

    @Parameter( property="compileProtoGenerateImpl.buildpath", defaultValue = "target/classes")
    private String buildPath;

    @Parameter( property="compileProtoGenerateImpl.gendsrcpath", defaultValue = "target/generated-sources")
    private String generatedSourcesPath;

    @Parameter(defaultValue = "${project}")
    private MavenProject project;

    @Parameter(defaultValue = "${maven.compile.classpath}")
    private String mavenClasspath;


    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info( "Source path = " +  srcPath);
        getLog().info( "Build path = "  +  buildPath);
        getLog().info( "Generated sources path = " + generatedSourcesPath);

        //(1) ensure that the build path exists and create if not
        File buildDirs = new File(buildPath);
        buildDirs.mkdirs();

        //(2) compile the source to the destination
        //getLog().info("Maven classpath=" + mavenClasspath);
        CompileHelper.compile(srcPath, buildPath, mavenClasspath);

        //(2.5) add the compiled sources to the classpath
        try {
            ClassLoader currentThreadClassLoader = Thread.currentThread().getContextClassLoader();
            File f = new File(buildPath);
            String url = "file:///" + f.getCanonicalPath().replaceAll("\\\\", "/");

            //MUST have a / on the end
            if (!url.endsWith("/")) url = url + "/";

            URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{new URL(url)}, currentThreadClassLoader);
            getLog().info("Extending classpath to include " + url);

            Thread.currentThread().setContextClassLoader(urlClassLoader);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //(3) generate the impl to generated-sources
        File f= new File(resourcesPath + "/generate.properties");

        GenerateDomainTask gdt = new GenerateDomainTask();
        gdt.setGeneratedSourcesPath(generatedSourcesPath);
        gdt.setPropertyFile(f);
        gdt.generate();

        //(4) instruct maven to compile it
        //project.addCompileSourceRoot(generatedSourcesPath);
    }

}
