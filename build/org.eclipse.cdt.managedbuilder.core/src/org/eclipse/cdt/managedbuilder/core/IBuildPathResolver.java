/*******************************************************************************
 * Copyright (c) 2005 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.core;

/**
 * this interface is to be implemented by the tool-integrator to provide some specific
 * logic for resolving the build path variable values to the build paths
 * @since 3.0
 */
public interface IBuildPathResolver {

	/**
	 *
	 * @param pathType one of the IEnvVarBuildPath.BUILDPATH _xxx
	 * @param variableName represents the name of the variable that holds the build paths
	 * @param variableValue represents the value of the value specified with the 
	 *     variableName argument
	 * @param configuration represents configuration for which the build paths are requested
	 */
	String[] resolveBuildPaths(
			int pathType,
			String variableName, 
			String variableValue,
			IConfiguration configuration);
}

