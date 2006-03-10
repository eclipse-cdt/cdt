/*******************************************************************************
 * Copyright (c) 2006 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.buildmodel;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.core.runtime.IPath;

/**
 *
 * This Build representation holds the hierarchy of inter-related
 * build actions and resources
 *
 */
public interface IBuildDescription {
	
	/**
	 * Returns the main input action
	 * 
	 * @return IBuildAction
	 */
	IBuildStep getInputStep();
	
	/**
	 * Returns the main output action
	 * 
	 * @return IBuildAction
	 */
	IBuildStep getOutputStep();

	/**
	 * Returns the Build resource for the given resource location
	 * 
	 * @param location 
	 * 
	 * @return the IBuildResource or null if not found
	 */
	IBuildResource getResourceForLocation(IPath location);
	
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
	 * 
	 * @return IPath
	 */
	IPath getDefaultBuildDirLocation();
}
