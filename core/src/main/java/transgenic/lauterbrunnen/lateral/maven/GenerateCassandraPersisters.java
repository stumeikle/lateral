package transgenic.lauterbrunnen.lateral.maven;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import transgenic.lauterbrunnen.lateral.persist.GenerateCassandraPersistCommon;
import transgenic.lauterbrunnen.lateral.persist.GeneratePersistCommon;
import transgenic.lauterbrunnen.lateral.persist.hazelcast.generator.GenerateCassandraHazelcastPersist;
import transgenic.lauterbrunnen.lateral.persist.hazelcast.generator.GenerateHazelcastCachePersist;
import transgenic.lauterbrunnen.lateral.persist.zerocache.generator.GenerateCassandraZCPersist;
import transgenic.lauterbrunnen.lateral.persist.zerocache.generator.GenerateZeroCachePersist;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Properties;

/**
 * Created by stumeikle on 06/07/20.
 */
@Mojo(name="generateCassandraPersisters")
public class GenerateCassandraPersisters extends AbstractMojo {


    @Parameter( property="generatePersisters.srcpath", defaultValue = "src/main/java")
    protected String srcPath;

    @Parameter( property="generatePersisters.resourcespath", defaultValue = "src/main/resources")
    protected String resourcesPath;

    @Parameter( property="generatePersisters.gendsrcpath", defaultValue = "target/generated-sources")
    protected String generatedSourcesPath;

    @Parameter( property="generatePersisters.buildpath", defaultValue = "target/classes")
    private String buildPath;

    @Parameter(defaultValue = "${project}")
    protected MavenProject project;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        //20171028 looks like we need to tweak the paths
        srcPath = CompileHelper.fixPath(srcPath, project);
        generatedSourcesPath = CompileHelper.fixPath(generatedSourcesPath, project);
        resourcesPath = CompileHelper.fixPath(resourcesPath, project);
        buildPath = CompileHelper.fixPath(buildPath,project);

        getLog().info( "Generated sources path = " + generatedSourcesPath);

        //As for generate entities, loop over the generate.properties files
        //Can we find all the generate*.properties files here?
        File dir = new File(resourcesPath+"/");
        FileFilter fileFilter = new WildcardFileFilter("generate*.properties");
        File[] files = dir.listFiles(fileFilter);
        //If there's more than one ensure each has a lateral.di.context
        //line --> update, let's just say all files need to define the context
        boolean allFilesContainContext=true;
        for (int i = 0; i < files.length; i++) {
            getLog().info("Found generate properties file = " + files[i].getName());

            Properties properties = new Properties();
            try {
                InputStream inputStream = new FileInputStream(files[i]);
                properties.load(inputStream);
                if (properties.getProperty("lateral.di.context")==null) {
                    allFilesContainContext=false;
                    getLog().error("File '"+ files[i].getName() + "' should define lateral.di.context. I can't proceed without this");
                    System.exit(0);
                    break;
                }
            } catch (Exception e) {
                allFilesContainContext=false;
                break;
            }
        }

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


        for(File f: files) {

            GenerateCassandraHazelcastPersist ghccp = new GenerateCassandraHazelcastPersist();
            ghccp.setGeneratedSourcesPath(generatedSourcesPath);
            ghccp.setPropertyFile(f);
            ghccp.setGenerateDirect(true);
            ghccp.generate();

            //TODO TODO TODO this not updated for cassandra
            GenerateCassandraZCPersist gzccp = new GenerateCassandraZCPersist();
            gzccp.setGeneratedSourcesPath(generatedSourcesPath);
            gzccp.setPropertyFile(f);
            gzccp.setGenerateDirect(true);
            gzccp.setClassLoader(this.getClass().getClassLoader());
            gzccp.generate();

            //all new for the transaction manager
            GenerateCassandraPersistCommon gpc = new GenerateCassandraPersistCommon();
            gpc.setGeneratedSourcesPath(generatedSourcesPath);
            gpc.setPropertyFile(f);
            gpc.generate();

            //(4) add all the files to be compiled by maven TODO why do i do this so many times
            project.addCompileSourceRoot(generatedSourcesPath);
        }
    }
}
