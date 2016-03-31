package org.eclipse.cdt.qt.core;

import java.nio.file.Path;

import org.eclipse.cdt.core.build.ICBuildConfiguration;
import org.eclipse.launchbar.core.target.ILaunchTarget;

public interface IQtBuildConfiguration extends ICBuildConfiguration {

	Path getBuildDirectory();

	Path getQmakeCommand();

	String getQmakeConfig();

	Path getProgramPath();

	boolean supports(ILaunchTarget target, String launchMode);

}
