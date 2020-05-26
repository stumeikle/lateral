package transgenic.lauterbrunnen.lateral.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import transgenic.lauterbrunnen.lateral.domain.generator.GenerateDomainTask;
import transgenic.lauterbrunnen.lateral.domain.generator.GenerateDomainTaskPt2;

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
 *
 * This file changes today. Now we need to compile the proto, generate some impl
 * Compile that and then generate the Default Factory Impl, so we have an extra step
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

        //20200503 Make sure the compilation only includes the proto definition here
        File g= new File(project.getBasedir() + "/" + resourcesPath + "/generate.properties");
        Properties properties = new Properties();
        try {
            InputStream inputStream = new FileInputStream(g);
            properties.load(inputStream);
            inputStream.close();
        } catch (Exception e) {
            System.out.println("Unable to load properties file. Path is " + g);
            System.exit(0);
        }
        String protoPackage = properties.getProperty("domain.proto.package");
        String protoPathSuffix = "/" + protoPackage.replaceAll("\\.","/");

        //20171028 looks like we need to tweak the paths
        srcPath = CompileHelper.fixPath(srcPath, project);
        buildPath = CompileHelper.fixPath(buildPath, project);
        generatedSourcesPath = CompileHelper.fixPath(generatedSourcesPath, project);
        resourcesPath = CompileHelper.fixPath(resourcesPath, project);

        //20200503
        srcPath = srcPath + protoPathSuffix;
//        buildPath=buildPath + protoPathSuffix;

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
        URLClassLoader urlClassLoader = null;
        String url = "";
        try {
            ClassLoader currentThreadClassLoader = Thread.currentThread().getContextClassLoader();
            File f = new File(buildPath);
            url = "file:///" + f.getCanonicalPath().replaceAll("\\\\", "/");

            //MUST have a / on the end
            if (!url.endsWith("/")) url = url + "/";

            urlClassLoader = new URLClassLoader(new URL[]{new URL(url)}, currentThreadClassLoader);
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
        gdt.setClassLoader(urlClassLoader);
        gdt.generate();

        getLog().info("About to compile again ...");

        //Compile again and then generate the factory
        CompileHelper.compile(generatedSourcesPath, buildPath, mavenClasspath + File.pathSeparatorChar + url);

        getLog().info("compiled... ");

        GenerateDomainTaskPt2 gdtp2 = new GenerateDomainTaskPt2();
        gdtp2.setGeneratedSourcesPath(generatedSourcesPath);
        gdtp2.setPropertyFile(f);
        gdtp2.setClassLoader(urlClassLoader);
        gdtp2.generate();

        //(4) instruct maven to compile it
        //project.addCompileSourceRoot(generatedSourcesPath);
    }

}
