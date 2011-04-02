package org.junithelper.mavenplugin;

import org.apache.maven.plugin.MojoExecutionException;
import org.junithelper.command.ForceJUnitVersion3Command;
import org.junithelper.command.MakeTestCommand;

/**
 * @goal force3
 * @phase process-sources
 */
public class Force3Mojo extends AbstractJUnitHelperMojo {

    @Override
    public void execute() throws MojoExecutionException {

        try {
            ForceJUnitVersion3Command.config = loadConfig();
            ForceJUnitVersion3Command.main(new String[]{});
        } catch (Exception e) {
            throw new MojoExecutionException("junithelper force4 error!", e);
        }
    }

}
