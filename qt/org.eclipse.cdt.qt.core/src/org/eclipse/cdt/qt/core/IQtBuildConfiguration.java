/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.qt.core;

import java.nio.file.Path;

import org.eclipse.cdt.core.build.ICBuildConfiguration;
import org.eclipse.core.runtime.CoreException;

/**
 * Qt specific build configuration settings.
 * 
 * @noimplement
 */
public interface IQtBuildConfiguration extends ICBuildConfiguration {

	Path getBuildDirectory() throws CoreException;
	
	Path getQmakeCommand();

	String[] getQmakeConfig();

	/**
	 * @deprecated use getBuildOutput() instead
	 */
	@Deprecated
	Path getProgramPath() throws CoreException;

	String getLaunchMode();

	IQtInstall getQtInstall();

}
