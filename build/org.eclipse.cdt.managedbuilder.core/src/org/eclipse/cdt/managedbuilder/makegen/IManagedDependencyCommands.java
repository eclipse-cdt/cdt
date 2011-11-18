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
 * IManagedDependencyCalculator or IManagedDependencyPreBuild.
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
 *         See the IManagedDependencyPreBuild interface for more information.
 *         
 *     b.  In other cases (e.g., C/C++) the dependency files can be created as
 *         a side effect of the main build.  This implies that the up to date 
 *         dependency files are not required for the current build, but for 
 *         the next build.  C/C++ builds can be treated in this manner as is 
 *         described in the following link: 
 *         http://sourceware.org/automake/automake.html#Dependency-Tracking-Evolution
 *
 *         Use the IManagedDependencyCommands interface defined in this file 
 *         for this mode.
 *         
 *         Two sub-scenarios of this mode are to:
 *         
 *         Create dependency files in the same invocation of the tool that 
 *         creates the tool's build artifact - by adding additional options 
 *         to the tool invocation command line.
 *         
 *         Create dependency files in a separate invocation of the tool, or
 *         by the invocation of another tool.
 *         
 *     MBS can also help in the generation of the dependency files.  Prior to
 *     CDT 3.1, MBS and gcc cooperated in generating dependency files using the
 *     following steps:
 *     
 *     1.  Gcc is invoked to perform the compilation that generates the object 
 *         file.
 *         
 *     2.  An "echo" command creates the .d file, adding the name of the .d
 *         file to the beginning of the newly created .d file.  Note that this
 *         causes problems with some implementations of "echo" that don't
 *         work exactly the way that we want (e.g., it doesn't support the  -n
 *         switch).

 *     3.  Gcc is invoked again with the appropriate additional command line
 *         options to append its dependency file information to the .d file 
 *         that was created by "echo".
 *         
 *     4.  Steps 1 - 3 are invoked in the make file.  Step 4 occurs after the
 *         make invocation has finished.  In step 4, MBS code post-processes 
 *         the .d files to add a dummy dependency for each header file, for 
 *         the reason explained in the link above.
 *         
 *     This mode is no longer used by the default gcc implementation, but can 
 *     still be used by selecting the DefaultGCCDependencyCalculator.    
 * 
 *         
 * Note for GNU make: these separate dependency files are "include"d by
 *         a main makefile.  Therefore, if the dependency files are required to 
 *         be up to date before the main build begins, they must be updated by
 *         a separate invocation of make.  Also, the configuration "clean" step
 *         must be invoked by a separate invocation of make.  This is so that
 *         we can exclude the dependency files for a "make clean" invocation 
 *         using syntax like:
 *         
 *         ifneq ($(MAKECMDGOALS), clean)
 *         -include $(DEPS)
 *         endif
 *         
 *         Otherwise, because GNU make attempts to re-make make files, we
 *         can end up with out of date or missing dependency files being 
 *         re-generated and then immediately "clean"ed.
 */

public interface IManagedDependencyCommands extends IManagedDependencyInfo {
	
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
	 * Returns the command lines to be invoked before the normal tool invocation 
	 * to calculate dependencies.
     *
	 * @return String[]  This can be null or an empty array if no dependency 
	 *                   generation command needs to be invoked before the normal 
	 *                   tool invocation.
	 */
	public String[] getPreToolDependencyCommands();
	
	/**
	 * Returns the command line options to be used to calculate dependencies.
	 * The options are added to the normal tool invocation. 
     *
	 * @return String[]  This can be null or an empty array if no additional 
	 *                   arguments need to be added to the tool invocation.
	 *         SHOULD THIS RETURN AN IOption[]?
	 */
	public String[] getDependencyCommandOptions();
	//  IMPLEMENTATION NOTE:  This should be called from addRuleFromSource for both resconfig & non-resconfig
	
	/**
	 * Returns the command lines to be invoked after the normal tool invocation  
	 * to calculate dependencies.  
     *
	 * @return String[]  This can be null or an empty array if no dependency 
	 *                   generation commands needs to be invoked after the normal 
	 *                   tool invocation
	 */
	public String[] getPostToolDependencyCommands();
	
	/**
	 * Returns true if the command lines and/or options returned by this interface
	 * are not specific to the particular source file, but are only specific to,
	 * at most, the configuration and tool.  If the build context is a resource 
	 * configuration, this method should return false if any of the command lines 
	 * and/or options are different than if the build context were the parent 
	 * configuration.  This can be used by the build file generator in helping
	 * to determine if a "pattern" (generic) rule can be used.
     *
	 * @return boolean
	 */
	public boolean areCommandsGeneric();
}
