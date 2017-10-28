package transgenic.lauterbrunnen.lateral.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import transgenic.lauterbrunnen.lateral.cache.hazelcast.generator.GenerateHazelcastCache;
import transgenic.lauterbrunnen.lateral.cache.zero.generator.GenerateZeroCache;
import transgenic.lauterbrunnen.lateral.domain.generator.GenerateDomainTask;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Created by stumeikle on 21/06/16.
 * It is expected that this will be run (optionally perhaps) after the compileProtoGenerateImpl
 */
@Mojo(name="compileImplGenerateCache")
public class CompileImplGenerateCache extends AbstractMojo {

    @Parameter( property="compileImplGenerateCache.srcpath", defaultValue = "src/main/java")
    private String srcPath;

    @Parameter( property="compileImplGenerateCache.resourcespath", defaultValue = "src/main/resources")
    private String resourcesPath;

    @Parameter( property="compileImplGenerateCache.buildpath", defaultValue = "target/classes")
    private String buildPath;

    @Parameter( property="compileImplGenerateCache.gendsrcpath", defaultValue = "target/generated-sources")
    private String generatedSourcesPath;

    @Parameter(defaultValue = "${project}")
    private MavenProject project;

    @Parameter(defaultValue = "${maven.compile.classpath}")
    private String mavenClasspath;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        //20171028 looks like we need to tweak the paths
        srcPath = CompileHelper.fixPath(srcPath, project);
        buildPath = CompileHelper.fixPath(buildPath, project);
        generatedSourcesPath = CompileHelper.fixPath(generatedSourcesPath, project);
        resourcesPath = CompileHelper.fixPath(resourcesPath, project);

        getLog().info( "Source path = " +  srcPath);
        getLog().info( "Build path = "  +  buildPath);
        getLog().info( "Generated sources path = " + generatedSourcesPath);

        //(1) ensure that the build path exists and create if not
        File buildDirs = new File(buildPath);
        buildDirs.mkdirs();

        //(2) compile the source to the destination
        //getLog().info("Maven classpath=" + mavenClasspath);
        CompileHelper.compile(generatedSourcesPath, buildPath, mavenClasspath + File.pathSeparator + buildPath +"/");

        //(2.5) check if we can instantiate the new classes. The classloader changes made in cpgi should
        //have done the trick.
        URLClassLoader urlClassLoader =null;
        try {
            ClassLoader currentThreadClassLoader = Thread.currentThread().getContextClassLoader();
            File f = new File(buildPath);
            String url = "file:///" + f.getCanonicalPath().replaceAll("\\\\", "/");

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

        GenerateHazelcastCache ghcc = new GenerateHazelcastCache();
        ghcc.setClassLoader(urlClassLoader);
        ghcc.setGeneratedSourcesPath(generatedSourcesPath);
        ghcc.setPropertyFile(f);
        ghcc.generate();

        GenerateZeroCache gzc = new GenerateZeroCache();
        gzc.setClassLoader(urlClassLoader);
        gzc.setGeneratedSourcesPath(generatedSourcesPath);
        gzc.setPropertyFile(f);
        gzc.generate();

        //(4) add all the files to be compiled by maven
        project.addCompileSourceRoot(generatedSourcesPath);
    }
}
