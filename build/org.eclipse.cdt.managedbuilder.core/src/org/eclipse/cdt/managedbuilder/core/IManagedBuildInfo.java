package org.eclipse.cdt.managedbuilder.core;

/**********************************************************************
 * Copyright (c) 2003,2004 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
 ***********************************************************************/

import java.util.List;

import org.eclipse.cdt.managedbuilder.makegen.IManagedDependencyGenerator;

public interface IManagedBuildInfo {
	public static final String DEFAULT_CONFIGURATION = "defaultConfig";	//$NON-NLS-1$
	public static final String DEFAULT_TARGET = "defaultTarget";	//$NON-NLS-1$

	/**
	 * Add a new target to the build information for the receiver
	 * 
	 * @param target
	 */
	public void addTarget(ITarget target);
		
	/**
	 * Answers <code>true</code> if the build system knows how to 
	 * build a file with the extension passed in the argument.
	 *  
	 * @param srcExt
	 * @return
	 */
	public boolean buildsFileType(String srcExt);

	
	/**
	 * Answers the file extension for the receivers build goal without a separator.
	 * 
	 * @return the extension or an empty string if none is defined
	 */
	public String getBuildArtifactExtension();
	
	/**
	 * Returns the name of the artifact to build for the receiver.
	 * 
	 * @return
	 */
	public String getBuildArtifactName();

	/**
	 * Answers the command needed to remove files on the build machine
	 * 
	 * @return
	 */
	public String getCleanCommand();

	/**
	 * Answers the name of the default configuration, for example <code>Debug</code>  
	 * or <code>Release</code>.
	 * 
	 * @return
	 */
	public String getConfigurationName();
	
	/**
	 * Answers a <code>String</code> array containing the names of all the configurations
	 * defined for the project's current target.
	 *  
	 * @return
	 */
	public String[] getConfigurationNames();

	/**
	 * Get the default configuration associated with the receiver
	 * 
	 * @return
	 */
	public IConfiguration getDefaultConfiguration(ITarget target);
	
	
	/**
	 * Returns the default target in the receiver.
	 * 
	 * @return
	 */
	public ITarget getDefaultTarget();
	
	/**
	 * @param sourceExtension
	 * @return
	 */
	public IManagedDependencyGenerator getDependencyGenerator(String sourceExtension);
	
	/**
	 * Returns a <code>String</code> containing the flags, including 
	 * those overridden by the user, for the tool that handles the 
	 * type of source file defined by the argument.
	 * 
	 * @param extension
	 * @return
	 */
	public String getFlagsForSource(String extension);

	/**
	 * Returns a <code>String</code> containing the flags, including 
	 * those overridden by the user, for the tool that handles the 
	 * type of target defined by the argument.
	 * 
	 * @param extension
	 * @return
	 */
	public String getFlagsForTarget(String extension);

	/**
	 * Answers the libraries the project links in.
	 * 
	 * @param extension
	 * @return
	 */
	public String[] getLibsForTarget(String extension);

	/**
	 * Answers a <code>String</code> containing the arguments to be passed to make. 
	 * For example, if the user has selected a build that keeps going on error, the 
	 * answer would contain {"-k"}.
	 * 
	 * @return String
	 */
	public String getMakeArguments();

	/**
	 * Answers a <code>String</code> containing the make command invocation 
	 * for the default target/configuration.
	 */
	public String getMakeCommand();
	
	/**
	 * Answers the extension that will be built by the current configuration
	 * for the extension passed in the argument or <code>null</code>.
	 * 
	 * @param resourceName
	 * @return
	 */
	public String getOutputExtension(String resourceExtension);
	
	/**
	 * Answers the flag to be passed to the build tool to produce a specific output 
	 * or an empty <code>String</code> if there is no special flag. For example, the
	 * GCC tools use the '-o' flag to produce a named output, for example
	 * 		gcc -c foo.c -o foo.o
	 * 
	 * @param outputExt
	 * @return
	 */
	public String getOutputFlag(String outputExt);
	
	/**
	 * Answers the prefix that should be prepended to the name of the build 
	 * artifact. For example, a library foo, should have the prefix 'lib' and 
	 * the extension '.a', so the final goal would be 'libfoo.a' 
	 * 
	 * @param extension
	 * @return the prefix or an empty string
	 */
	public String getOutputPrefix(String outputExtension);
	
	/**
	 * Get the currently selected target.  This is used while the project
	 * property pages are displayed
	 * 
	 * @return target
	 */
	public ITarget getSelectedTarget();

	/**
	 * Get the target specified in the argument.
	 * 
	 * @param id
	 * @return
	 */
	public ITarget getTarget(String id);

	/**
	 * Get all of the targets associated with the receiver.
	 * 
	 * @return
	 */
	public List getTargets();

	/**
	 * Returns a <code>String</code> containing the command-line invocation 
	 * for the tool associated with the source extension.
	 * 
	 * @param sourceExtension the file extension of the file to be built
	 * @return a String containing the command line invocation for the tool
	 */
	public String getToolForSource(String sourceExtension);

	/**
	 * Returns a <code>String</code> containing the command-line invocation 
	 * for the tool associated with the target extension.
	 * 
	 * @param extension the file extension of the build goal
	 * @return a String containing the command line invocation for the tool
	 */
	public String getToolForTarget(String extension);
	
	/**
	 * Answers a <code>String</code> array containing the contents of the 
	 * user objects option, if one is defined for the target.
	 * 
	 * @param extension the file ecxtension of the build target
	 * @return
	 */
	public String[] getUserObjectsForTarget(String extension);

	
	/**
	 * Answers the version of the build information in the format 
	 * @return a <code>String</code> containing the build information 
	 * version
	 */
	public String getVersion();
	
	/**
	 * Answers true if the build model has been changed by the user.
	 * 
	 * @return boolean
	 */
	public boolean isDirty();
	
	/**
	 * Answers <code>true</code> if the extension matches one of the special 
	 * file extensions the tools for the target consider to be a header file. 
	 * 
	 * @param ext the file extension of the resource
	 * @return boolean
	 */
	public boolean isHeaderFile(String ext);

	
	/**
	 * Answers whether the receiver has been changed and requires the 
	 * project to be rebuilt. When a project is first created, it is 
	 * assumed that the user will need it to be fully rebuilt. However 
	 * only option and tool command changes will trigger the build 
	 * information for an existing project to require a rebuild.
	 * <p>
	 * Clients can reset the state to force or clear the rebuild status 
	 * using <code>setRebuildState()</code>
	 * @see ManagedBuildInfo#setRebuildState(boolean)
	 * 
	 * @return <code>true</code> if the resource managed by the 
	 * receiver needs to be rebuilt
	 */
	public boolean needsRebuild();

	public void removeTarget(String id);
	
	/**
	 * Set the primary configuration for the receiver.
	 * 
	 * @param configuration The <code>IConfiguration</code> that will be used as the default
	 * for all building.
	 */
	public void setDefaultConfiguration(IConfiguration configuration);
	
	/**
	 * 
	 * @param configuration
	 * @return
	 */
	public boolean setDefaultConfiguration(String configName);
	
	/**
	 * Set the primary target for the receiver.
	 * 
	 * @param target
	 */
	public void setDefaultTarget(ITarget target);
	
	/**
	 * Set the dirty flag for the build model to the value of the argument.
	 * 
	 * @param isDirty
	 */
	public void setDirty(boolean isDirty);
	
	/**
	 * Sets the rebuild state in the receiver to the value of the argument. 
	 * This is a potentially expensive option, so setting it to true should 
	 * only be done if a project resource or setting has been modified in a 
	 * way that would invalidate the previous build.  
	 *  
	 * @param <code>true</code> will force a rebuild the next time the project builds
	 */
	public void setRebuildState(boolean rebuild);
	
	/**
	 * Set the currently selected target. This is used while the project 
	 * property pages are displayed
	 * 
	 * @param target the user selection
	 */
	public void setSelectedTarget(ITarget target);
}
