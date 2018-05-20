package sic.plugin;

import com.sun.tools.javadoc.Main;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * This is the main executor class of SIC
 */
@Mojo(name = "scan")
@SuppressWarnings("unused")
public class SicMain extends AbstractMojo {

    @SuppressWarnings("unused")
    @Parameter(property = "scan.javaSourceLocation", defaultValue = "./src/main/java")
    private String javaSourceLocation;
    @SuppressWarnings("unused")
    @Parameter(property = "scan.javaSubPackageLocation", defaultValue = "./src/main/java")
    private String javaSubPackageLocation;
    @SuppressWarnings("unused")
    @Parameter(property = "scan.layerRuleFileLocation", defaultValue = "/")
    private String layerRuleFileLocation;

    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("scanning the project ......");
        Support.layerRuleFileLocation = layerRuleFileLocation;

        Main.execute(new String[]{"-doclet", "sic.plugin.CustomExceptionsCheck", "-docletpath"
                , "."
                , "-sourcepath"
                , javaSourceLocation
                , "-subpackages"
                , javaSubPackageLocation});

        Main.execute(new String[]{"-doclet", "sic.plugin.DuplicateFunctionalityCheck", "-docletpath"
                , "."
                , "-sourcepath"
                , javaSourceLocation
                , "-subpackages"
                , javaSubPackageLocation});

        Main.execute(new String[]{"-doclet", "sic.plugin.SimpleLayerAccessCheck", "-docletpath"
                , "."
                , "-sourcepath"
                , javaSourceLocation
                , "-subpackages"
                , javaSubPackageLocation});

        if (!Support.isBuildPassed) {
            throw new MojoFailureException(Support.buildFailureMsg);
        }
    }
}