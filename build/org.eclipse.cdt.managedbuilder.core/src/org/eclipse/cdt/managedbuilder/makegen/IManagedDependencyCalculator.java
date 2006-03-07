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
 * IManagedDependencyCommands or IManagedDependencyPreBuild.
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
 *     This type of dependency calculator implements the
 *     IManagedDependencyCalculator interface defined in this module.
 *     
 *     One minor mode of this mode is to use a dependency calculator provided
 *     by a language integration (e.g. C, C++ or Fortran) that uses the 
 *     language's parsing support to return information regarding source file 
 *     dependencies.  An example of this is using the C/C++ Indexer to
 *     compute dependencies.
 *     
 *  2. The build file generator and the tool-chain cooperate in creating and 
 *     using separate "dependency" files.  In this case, dependency calculation
 *     is done at "build time", rather than at "build file generation time" as
 *     in mode #1.  This currently supports the GNU concept of using .d files 
 *     in GNU make.  See the IManagedDependencyCommands and
 *     IManagedDependencyPreBuild interfaces for more information.
 *     
 */

public interface IManagedDependencyCalculator extends IManagedDependencyInfo {
	
	/**
	 * Returns the list of source file specific dependencies.
	 *   
	 * The paths can be either relative to the project directory, or absolute 
	 * in the file system.
	 *
	 * @return IPath[]    
	 */
	public IPath[] getDependencies();
	
	/**
	 * Returns the list of source file specific additional targets that the 
	 * source file creates.  Most source files will return null.  An example 
	 * of where additional targets should be returned is for a Fortran 90 
	 * source file that creates one or more Fortran Modules.
	 * 
	 * Note that these output files that are dependencies to other invocations
	 * of the same tool can be specified here, or as another output type 
	 * of the tool.  If the output file can be used as the input of a different
	 * tool, then use the output type mechanism.
	 *   
	 * The paths can be either relative to the top build directory, or absolute 
	 * in the file system.
     *
	 * @return IPath[]    
	 */
	public IPath[] getAdditionalTargets();
}
