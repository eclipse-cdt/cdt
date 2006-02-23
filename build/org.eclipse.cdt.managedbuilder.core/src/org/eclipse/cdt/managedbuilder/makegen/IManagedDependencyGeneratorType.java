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
package org.eclipse.cdt.managedbuilder.makegen;

/**
 * @since 3.1
 * 
 * IManagedDependencyGenerator (deprecated) and IManagedDependencyGenerator2 
 * extend this interface. 
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

public interface IManagedDependencyGeneratorType {
	/**
	 *  Constants returned by getCalculatorType
	 */
	public int TYPE_NODEPS = 0;			//  Deprecated - use TYPE_NODEPENDENCIES
	public int TYPE_COMMAND = 1;		//  Deprecated - use TYPE_BUILD_COMMANDS
	public int TYPE_INDEXER = 2;		//  Deprecated - use TYPE_CUSTOM
	public int TYPE_EXTERNAL = 3;		//  Deprecated - use TYPE_CUSTOM
	public int TYPE_OLD_TYPE_LIMIT = 3;
	
	//  Use these types
	public int TYPE_NODEPENDENCIES = 4;
	public int TYPE_BUILD_COMMANDS = 5;
	public int TYPE_PREBUILD_COMMANDS = 6;
	public int TYPE_CUSTOM = 7;
	
	/**
	 * Returns the type of dependency generator that is implemented.  
	 * 
	 *   TYPE_NODEPENDENCIES indicates that no dependency generator is 
	 *     supplied or needed.
	 *   TYPE_CUSTOM indicates that a custom, "build file generation time"
	 *     dependency calculator is implemented.  Note that the dependency
	 *     calculator will be called when the makefile is generated, and 
	 *     for every source file that is built by this tool in the build 
	 *     file, not just for those that have changed since the last build
	 *     file generation.
	 *   TYPE_BUILD_COMMANDS indicates that command lines or options  will 
	 *     be returned to be used to calculate dependencies.  These 
	 *     commands/options are added to the build file to perform dependency
	 *     calculation at "build time".  This currently supports 
	 *     compilers/tools that generate .d files either as a
	 *     side-effect of tool invocation, or as a separate step that is
	 *     invoked immediately before or after the tool invocation. 
	 *   TYPE_PREBUILD_COMMANDS indicates that a separate build step is 
	 *     invoked, prior to the the normal build steps, to update the 
	 *     dependency information.  These commands are added to the build
	 *     file to perform dependency calculation at "build time".  Note 
	 *     that this step will be invoked every time a build is done in 
	 *     order to determine if dependency files need to be re-generated.
	 * 
	 * @return int
	 */
	public int getCalculatorType();
}
