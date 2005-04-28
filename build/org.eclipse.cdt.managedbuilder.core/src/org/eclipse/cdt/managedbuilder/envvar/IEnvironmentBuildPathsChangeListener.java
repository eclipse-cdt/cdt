/**********************************************************************
 * Copyright (c) 2005 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Intel Corporation - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.managedbuilder.envvar;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;

/**
 * 
 * listeners of the environment build path changes should implement this interface
 * 
 * @since 3.0
 */
public interface IEnvironmentBuildPathsChangeListener {

	/**
	 *
	 * @param configuration represent the configuration for which the paths were changed
	 * @param buildPathType set to one of 
	 * the IEnvVarBuildPath.BUILDPATH_xxx 
	 * (the IEnvVarBuildPath will represent the build environment variables, see also 
	 * the "Specifying the Includes and Library paths environment variables",
	 * the "envVarBuildPath schema" and the "Expected CDT/MBS code changes" sections)
	 */
	void buildPathsChanged(IConfiguration configuration, int buildPathType);
}

