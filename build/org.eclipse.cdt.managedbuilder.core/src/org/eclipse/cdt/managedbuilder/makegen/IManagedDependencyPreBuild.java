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

import org.eclipse.core.runtime.IPath;

/**
 * @since 3.1
 *
 * A Tool dependency calculator may implement this interface or
 * IManagedDependencyCalculator or IManagedDependencyCommands.
 * An object implementing the interface is returned from a call to
 * IManagedDependencyGenerator2.getDependencySourceInfo.
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
 *     See the IManagedDependencyCalculator interface for more information.
 *     
 *  2. The build file generator and the tool-chain cooperate in creating and 
 *     using separate "dependency" files.  The build file generator calls
 *     the dependency calculator to get the dependency file names and to get
 *     commands that need to be added to the build file.  In this case, 
 *     dependency calculation is done at "build time", rather than at 
 *     "build file generation time" as in mode #1.  This currently 
 *     supports the GNU concept of using .d files in GNU make.
 *     
 *     There are multiple ways that these separate dependency files can
 *     be created by the tool-chain and used by the builder.  
 *     
 *     a.  In some cases (e.g., Fortran 90 using modules) the dependency files
 *         must be created/updated prior to invoking the build of the project 
 *         artifact (e.g., an application).  In this case, the dependency 
 *         generation step must occur separately before the main build.
 *         Use the IManagedDependencyPreBuild interface defined in this file 
 *         for this mode.
 *         
 *     b.  In other cases (e.g., C/C++) the dependency files can be created as
 *         a side effect of the main build.  This implies that the up to date 
 *         dependency files are not required for the current build, but for 
 *         the next build.  C/C++ builds can be treated in this manner as is 
 *         described in the following link: 
 *         http://sourceware.org/automake/automake.html#Dependency-Tracking-Evolution
 *         
 *         See the IManagedDependencyCommands interface for more information.
 *
 *         
 * Note for GNU make: these separate dependency files are "include"d by
 *         a main makefile.  Make performs special processing on make files:
 *         
 *         "To this end, after reading in all makefiles, make will consider 
 *         each as a goal target and attempt to update it. If a makefile has a
 *         rule which says how to update it (found either in that very 
 *         makefile or in another one)..., it will be updated if necessary. 
 *         After all makefiles have been checked, if any have actually been 
 *         changed, make starts with a clean slate and reads all the makefiles 
 *         over again."
 *         
 *         We can use this to ensure that the dependency files are up to date
 *         by adding rules to the make file for generating the dependency files.
 *         These rules are returned by the call to getDependencyCommands.
 *         However, this has a significant problem when we don�t want to build
 *         the build target, but only want to �clean� the configuration, 
 *         for example.  If we invoke make just to clean the configuration, 
 *         make will still update the dependency files if necessary, thereby 
 *         re-generating the dependency files only to immediately delete them.
 *         The workaround suggested by the make documentation is to check for 
 *         an invocation using the �clean� target, and to not include the 
 *         dependency files it that case.  For example,
 *         
 *         ifneq ($(MAKECMDGOALS),clean)
 *         include $(DEPS)
 *         endif
 *         
 *         The restriction with this is that it only works if �clean� is the only 
 *         target specified on the make command line.  Therefore, the build 
 *         "clean" step must be invoked separately.
 */

public interface IManagedDependencyPreBuild extends IManagedDependencyInfo {
	
	/**
	 * Returns the list of generated dependency files.
	 *   
	 * The paths can be either relative to the top build directory, or absolute 
	 * in the file system.
	 *
	 * @return IPath[]    
	 */
	public IPath[] getDependencyFiles();

	/**
	 * Returns the name to be used in the build file to identify the separate
	 * build step.  Note that this name should be unique to the tool since
	 * multiple tools in a tool-chain may be using this method of
	 * dependency calculation.
	 *    
	 * @return String  
	 */
	public String getBuildStepName();

	/**
	 * Returns the command line(s) to be invoked in the separate 
	 * dependencies pre-build step.
     *
	 * @return String[]
	 */
	public String[] getDependencyCommands();
	
	/**
	 * Returns true if the command lines returned by this interface
	 * are not specific to the particular source file, but are only specific to,
	 * at most, the configuration and tool.  If the build context is a resource 
	 * configuration, this method should return false if any of the command lines 
	 * are different than if the build context were the parent configuration.  
	 * This can be used by the build file generator in helping to determine if 
	 * a "pattern" (generic) rule can be used.
     *
	 * @return boolean
	 */
	public boolean areCommandsGeneric();
}
