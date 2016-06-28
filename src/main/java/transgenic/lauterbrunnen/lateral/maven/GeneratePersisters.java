package transgenic.lauterbrunnen.lateral.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import transgenic.lauterbrunnen.lateral.entity.generator.GenerateEntityTask;
import transgenic.lauterbrunnen.lateral.persist.hazelcast.generator.GenerateHazelcastCachePersist;

import java.io.File;

/**
 * Created by stumeikle on 21/06/16.
 */
@Mojo(name="generatePersisters")
public class GeneratePersisters extends AbstractMojo{

    @Parameter( property="generatePersisters.srcpath", defaultValue = "src/main/java")
    private String srcPath;

    @Parameter( property="generatePersisters.resourcespath", defaultValue = "src/main/resources")
    private String resourcesPath;

    @Parameter( property="generatePersisters.gendsrcpath", defaultValue = "target/generated-sources")
    private String generatedSourcesPath;

    @Parameter(defaultValue = "${project}")
    private MavenProject project;


    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info( "Generated sources path = " + generatedSourcesPath);

        File f= new File(resourcesPath + "/generate.properties");

        GenerateHazelcastCachePersist ghccp = new GenerateHazelcastCachePersist();
        ghccp.setGeneratedSourcesPath(generatedSourcesPath);
        ghccp.setPropertyFile(f);
        ghccp.generate();

        //(4) add all the files to be compiled by maven
        project.addCompileSourceRoot(generatedSourcesPath);
    }
}
