package transgenic.lauterbrunnen.lateral.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import transgenic.lauterbrunnen.lateral.persist.hazelcast.generator.GenerateHazelcastCachePersist;

import java.io.File;

/**
 * Created by stumeikle on 01/12/16.
 */
@Mojo(name="generateRemoteRetrievers")
public class GenerateRemoteRetrievers extends GeneratePersisters {

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info( "Generated sources path = " + generatedSourcesPath);

        File f= new File(resourcesPath + "/generate.properties");

        GenerateHazelcastCachePersist ghccp = new GenerateHazelcastCachePersist();
        ghccp.setGeneratedSourcesPath(generatedSourcesPath);
        ghccp.setPropertyFile(f);
        ghccp.setGenerateDirect(false);
        ghccp.generate();

        //(4) add all the files to be compiled by maven
        project.addCompileSourceRoot(generatedSourcesPath);
    }
}
