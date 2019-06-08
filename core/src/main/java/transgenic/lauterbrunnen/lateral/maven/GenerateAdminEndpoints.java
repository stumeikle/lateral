package transgenic.lauterbrunnen.lateral.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import transgenic.lauterbrunnen.lateral.persist.hazelcast.generator.GenerateHazelcastAdminEndpoints;
import transgenic.lauterbrunnen.lateral.persist.hazelcast.generator.GenerateHazelcastCachePersist;

import java.io.File;

/**
 * Created by stumeikle on 01/12/16.
 * Deprecated
 */
//@Mojo(name="generateAdminEndpoints")
public class GenerateAdminEndpoints extends AbstractMojo {
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

        //20171028 looks like we need to tweak the paths
        srcPath = CompileHelper.fixPath(srcPath, project);
        generatedSourcesPath = CompileHelper.fixPath(generatedSourcesPath, project);
        resourcesPath = CompileHelper.fixPath(resourcesPath, project);

        getLog().info( "Generated sources path = " + generatedSourcesPath);

        File f= new File(resourcesPath + "/generate.properties");

        GenerateHazelcastAdminEndpoints ghae = new GenerateHazelcastAdminEndpoints();
        ghae.setGeneratedSourcesPath(generatedSourcesPath);
        ghae.setPropertyFile(f);
        ghae.generate();

        //(4) add all the files to be compiled by maven
        project.addCompileSourceRoot(generatedSourcesPath);
    }
}
