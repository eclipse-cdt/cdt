package org.eclipse.cdt.qt.core;

import java.nio.file.Path;

import org.eclipse.cdt.core.build.ICBuildConfiguration;

public interface IQtBuildConfiguration extends ICBuildConfiguration {

	Path getBuildDirectory();

	Path getQmakeCommand();

	String getQmakeConfig();

	Path getProgramPath();

	String getLaunchMode();

}
