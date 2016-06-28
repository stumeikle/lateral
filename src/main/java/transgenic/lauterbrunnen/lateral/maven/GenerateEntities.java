package transgenic.lauterbrunnen.lateral.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import transgenic.lauterbrunnen.lateral.entity.generator.GenerateEntityTask;

import java.io.File;

/**
 * Created by stumeikle on 21/06/16.
 */
@Mojo(name="generateEntities")
public class GenerateEntities extends AbstractMojo {

    @Parameter( property="generateEntities.srcpath", defaultValue = "src/main/java")
    private String srcPath;

    @Parameter( property="generateEntities.resourcespath", defaultValue = "src/main/resources")
    private String resourcesPath;

    @Parameter( property="generateEntities.gendsrcpath", defaultValue = "target/generated-sources")
    private String generatedSourcesPath;

    @Parameter(defaultValue = "${project}")
    private MavenProject project;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info( "Generated sources path = " + generatedSourcesPath);

        File f= new File(resourcesPath + "/generate.properties");
        File pers = new File(resourcesPath + "/META-INF/persistence.xml");

        GenerateEntityTask get = new GenerateEntityTask();
        get.setGeneratedSourcesPath(generatedSourcesPath);
        get.setPersistenceFile(pers);
        get.setPropertyFile(f);
        get.generate();

        //(4) add all the files to be compiled by maven
        project.addCompileSourceRoot(generatedSourcesPath);
    }
}
