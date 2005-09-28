/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM   - Initial API and implementation of IManagedDependencyGenerator
 * Intel - Initial API and implementation of IManagedDependencyGenerator2
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.makegen;

import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.core.runtime.IPath;
import org.eclipse.cdt.managedbuilder.core.ITool;

/**
 * @since NOT YET
 * This interface is PROPOSED and not yet used.
 *
 * A Tool dependency calculator must implement this interface.  This interface
 * replaces IManagedDependencyGenerator which is deprecated.
 * 
 * Note:  The IPath arguments to the methods below can be either relative to
 *        the project directory, or absolute in the file system.
 */
public interface IManagedDependencyGenerator2 {
	/**
	 *  Constants returned by getCalculatorType
	 */
	public int TYPE_NODEPS = 0;
	public int TYPE_COMMAND = 1;
	public int TYPE_INDEXER = 2;
	public int TYPE_EXTERNAL = 3;
	
	/**
	 * Returns the type of dependency generator that is implemented.  
	 * 
	 *   TYPE_NODEPS indicates a NULL dependency generator
	 *   TYPE_COMMAND indicates that a command line will be returned to be 
	 *     used to calculate dependencies.  This currently supports compilers
	 *     that generate .d files.
	 *   TYPE_INDEXER indicates that the CDT indexer should be used to
	 *     calculate the dependencies.
	 *   TYPE_EXTERNAL indicates that a custom dependency calculator is
	 *     implemented.  
	 * 
	 * @return int
	 */
	public int getCalculatorType();
	
	/**
	 * Returns the list of dependencies for this source file.  
	 * The paths can be either relative to the project directory, or absolute 
	 * in the file system.
     *
     * @param source  The source file for which dependencies should be calculated
     * @param info  The IManagedBuildInfo of the project
     * @param tool  The tool associated with the source file
     * @param topBuildDirectory  The top build directory of the project.  This is
     *   the working directory for the tool.
	 * @return IPath[]    
	 */
	public IPath[] findDependencies(
		IPath source,
		IManagedBuildInfo info,
		ITool tool,
		IPath topBuildDirectory);
		
	/**
	 * 
	 * Returns the command line to be used to calculate dependencies.  
	 * This currently supports compilers that generate .d files 
	 *    
     * @param source  The source file for which dependencies should be calculated
     * @param info  The IManagedBuildInfo of the project
     * @param tool  The tool associated with the source file
     * @param topBuildDirectory  The top build directory of the project.  This is
     *   the working directory for the tool.
     *
	 * @return String
	 */
	public String getDependencyCommand(
		IPath source,
		IManagedBuildInfo info,
		ITool tool,
		IPath topBuildDirectory);
}
