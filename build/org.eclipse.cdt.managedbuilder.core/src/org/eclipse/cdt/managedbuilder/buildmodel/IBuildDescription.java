/*******************************************************************************
 * Copyright (c) 2006, 2010 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.buildmodel;

import java.net.URI;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;

/**
 *
 * This Build representation holds the hierarchy of inter-related
 * build actions and resources
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IBuildDescription {
	
	/**
	 * Returns the main input step
	 * 
	 * @return IBuildStep
	 */
	IBuildStep getInputStep();
	
	/**
	 * Returns the main output step
	 * 
	 * @return IBuildStep
	 */
	IBuildStep getOutputStep();

	/**
	 * Returns the Build resource for the given resource location
	 * 
	 * @return the IBuildResource or null if not found
	 */
	IBuildResource getBuildResource(IPath location);

	/**
	 * Returns the Build resource for the given resource
	 * 
	 * @return the IBuildResource or null if not found
	 */
	IBuildResource getBuildResource(IResource resource);

	/**
	 * Returns all resources used in the build
	 * 
	 * @return IBuildResource[]
	 */
	IBuildResource[] getResources();
	
	/**
	 * Returns all build steps used in the build
	 * 
	 * @return IBuildResource[]
	 */
	IBuildStep[] getSteps();
	
	/**
	 * Returns the build configuration this representation was created for
	 * 
	 * @return IProject
	 */
	IConfiguration getConfiguration();
	
	/**
	 * Returns the default build directory location
	 * @return IPath
	 */
	IPath getDefaultBuildDirLocation();

	/**
	 * Returns the default build directory location URI
	 * @return URI build dir location or null if one couldn't be found
	 * @since 6.0
	 */
	URI getDefaultBuildDirLocationURI();

	/**
	 * @return The Workspace FullPath of the build directory
	 */
	IPath getDefaultBuildDirFullPath();
}
