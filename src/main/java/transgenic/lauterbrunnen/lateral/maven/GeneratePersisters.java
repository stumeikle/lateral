package transgenic.lauterbrunnen.lateral.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import transgenic.lauterbrunnen.lateral.entity.generator.GenerateEntityTask;
import transgenic.lauterbrunnen.lateral.persist.hazelcast.generator.GenerateHazelcastCachePersist;
import transgenic.lauterbrunnen.lateral.persist.zerocache.generator.GenerateZeroCachePersist;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Created by stumeikle on 21/06/16.
 */
@Mojo(name="generatePersisters")
public class GeneratePersisters extends AbstractMojo{

    @Parameter( property="generatePersisters.srcpath", defaultValue = "src/main/java")
    protected String srcPath;

    @Parameter( property="generatePersisters.resourcespath", defaultValue = "src/main/resources")
    protected String resourcesPath;

    @Parameter( property="generatePersisters.gendsrcpath", defaultValue = "target/generated-sources")
    protected String generatedSourcesPath;

    @Parameter(defaultValue = "${project}")
    protected MavenProject project;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info( "Generated sources path = " + generatedSourcesPath);

        File f= new File(resourcesPath + "/generate.properties");

        GenerateHazelcastCachePersist ghccp = new GenerateHazelcastCachePersist();
        ghccp.setGeneratedSourcesPath(generatedSourcesPath);
        ghccp.setPropertyFile(f);
        ghccp.setGenerateDirect(true);
        ghccp.generate();

        /*
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
        }*/

        GenerateZeroCachePersist gzccp = new GenerateZeroCachePersist();
        gzccp.setGeneratedSourcesPath(generatedSourcesPath);
        gzccp.setPropertyFile(f);
        gzccp.setGenerateDirect(true);
        gzccp.setClassLoader(this.getClass().getClassLoader());
        gzccp.generate();

        //(4) add all the files to be compiled by maven
        project.addCompileSourceRoot(generatedSourcesPath);
    }
}
