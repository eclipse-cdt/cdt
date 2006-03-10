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

import org.eclipse.core.runtime.IPath;

/**
 *
 * This interface represents the resource used in the build
 *
 */
public interface IBuildResource {
	/**
	 * Returns the resource location
	 * 
	 * @return IPath
	 */
	IPath getLocation();
	
	/**
	 * Returns the resource path related to the project directory
	 * or an absolute path(location) in case the resource 
	 * is located outside the project directory
	 * 
	 * @return IPath
	 */
	IPath getFullPath();
	
	/**
	 * Returns the output argument of the action
	 * That generates this resource.
	 * In case the resource is the project source,
	 * The main input action is returned
	 * @see IBuildRepresentation.getInputAction()
	 * 
	 * @return IBuildArgument
	 */
	IBuildIOType getProducerIOType();
	
	/**
	 * Returns an array of arguments where this resource is used as an input
	 * 
	 * @return IBuildArgument[]
	 */
	IBuildIOType[] getDependentIOTypes();
	
	/**
	 * Returns true if the resource needs rebuild
	 * 
	 * @return boolean
	 */
	boolean needsRebuild();
	
	/**
	 * Returns true if this resource belongs to the project
	 * 
	 * @return boolean
	 * 
	 */
	boolean isProjectResource();
	
	/**
	 * Returns true if the resource was removed from the build
	 * Note: the removed state represents is BUILD state rather than
	 * a file system state.
	 * If the build resouces is marked as removed that does not mean the 
	 * resource is removed in the file system  
	 * The removed state specifies that the resource is no longer used in the 
	 * build process. 
	 * E.g. the object file could be marked as removed if the source file was deleted
	 * in the file system
	 * The removed state information is used primarily for calculation
	 * of the project part that is to be rebuild
	 * 
	 * @return boolean
	 */
	boolean isRemoved();
	
	/**
	 * returns a build description that holds this step
	 * 
	 * @return IBuildDescription
	 */
	IBuildDescription getBuildDescription();
}
