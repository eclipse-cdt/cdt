/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
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

import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.core.runtime.IPath;
import org.eclipse.cdt.managedbuilder.core.ITool;

/**
 * @since 3.1
 *
 * A Tool dependency calculator must implement this interface.  This interface
 * replaces IManagedDependencyGenerator which is deprecated.
 *        
 * Discussion of Dependency Calculation:
 *
 * There are two major, and multiple minor, modes of dependency calculation 
 * supported by the MBS.  The major modes are:
 * 
 *  1. The build file generator invokes tool integrator provided methods 
 *     that calculate all dependencies using whatever method the tool
 *     integrator wants.  The build file generator then adds the dependencies
 *     to the build file using the appropriate build file syntax.
 *     This is a TYPE_CUSTOM dependency calculator as defined below.  
 *     See the IManagedDependencyCalculator interface for more information.
 *     
 *  2. The build file generator and the tool-chain cooperate in creating and 
 *     using separate "dependency" files.  In this case, dependency calculation
 *     is done at "build time", rather than at "build file generation time" as
 *     in mode #1.  This currently supports the GNU concept of using .d files 
 *     in GNU make.  
 *     This is either a TYPE_BUILD_COMMANDS dependency calculator or a 
 *     TYPE_PREBUILD_COMMANDS dependency calculator as defined below.  
 *     See the IManagedDependencyCommands and IManagedDependencyPreBuild 
 *     interfaces for more information.
 *     
 */

public interface IManagedDependencyGenerator2 extends IManagedDependencyGeneratorType {
	
	/**
	 * Returns an instance of IManagedDependencyInfo for this source file.
	 * IManagedDependencyCalculator, IManagedDependencyCommands
	 * and IManagedDependencyPreBuild are all derived from
	 * IManagedDependencyInfo, and any one of the three can be returned.
	 * This is called when getCalculatorType returns TYPE_BUILD_COMMANDS, 
	 * TYPE_CUSTOM or TYPE_PREBUILD_COMMANDS.
     *
     * @param source  The source file for which dependencies should be calculated
     *    The IPath can be either relative to the project directory, or absolute in the file system.
     * @param buildContext  The IConfiguration or IResourceConfiguration that
     *   contains the context in which the source file will be built
     * @param tool  The tool associated with the source file
     * @param topBuildDirectory  The top build directory of the configuration.  This is
     *   the working directory for the tool.  This IPath is relative to the project directory.
	 * @return IManagedDependencyInfo    
	 */
	public IManagedDependencyInfo getDependencySourceInfo(
		IPath source,
		IBuildObject buildContext,
		ITool tool,
		IPath topBuildDirectory);

	/**
	 * Returns the file extension used by dependency files created
	 * by this dependency generator.
	 * This is called when getCalculatorType returns TYPE_BUILD_COMMANDS or 
	 * TYPE_PREBUILD_COMMANDS.
	 *    
     * @param buildContext  The IConfiguration that contains the context of the build
     * @param tool  The tool associated with the dependency generator. 
     *
	 * @return String
	 */
	public String getDependencyFileExtension(
		IConfiguration buildContext,
		ITool tool);

	/**
	 * Called to allow the dependency calculator to post-process dependency files.
	 * This method is called after the build has completed for at least every 
	 * dependency file that has changed, and possibly for those that have not
	 * changed as well.  It may also be called with dependency files created by
	 * another tool.  This method should be able to recognize dependency files
	 * that don't belong to it, or that it has already post-processed.
	 * This is called when getCalculatorType returns TYPE_BUILD_COMMANDS or 
	 * TYPE_PREBUILD_COMMANDS.
	 *    
     * @param dependencyFile  The dependency file  
     *    The IPath can be either relative to the top build directory, or absolute in the file system.
     * @param buildContext  The IConfiguration that contains the context of the build
     * @param tool  The tool associated with the dependency generator.  Note that this is
     *    not necessarily the tool that created the dependency file
     * @param topBuildDirectory  The top build directory of the project.  This is
     *   the working directory for the tool.
     *
	 * @return boolean  True if the method modified the dependency (e.g., .d) file
	 */
	public boolean postProcessDependencyFile(
		IPath dependencyFile,
		IConfiguration buildContext,
		ITool tool,
		IPath topBuildDirectory);
}
