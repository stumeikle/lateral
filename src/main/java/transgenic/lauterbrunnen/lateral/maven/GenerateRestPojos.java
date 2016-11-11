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

    @Parameter(defaultValue = "${project}")
    private MavenProject project;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info( "Generated sources path = " + generatedSourcesPath);

        File f= new File(resourcesPath + "/generate.properties");

        GenerateRestPojosTask get = new GenerateRestPojosTask();
        get.setGeneratedSourcesPath(generatedSourcesPath);
        get.setPropertyFile(f);
        get.generate();

        //(4) add all the files to be compiled by maven
        project.addCompileSourceRoot(generatedSourcesPath);
    }
}