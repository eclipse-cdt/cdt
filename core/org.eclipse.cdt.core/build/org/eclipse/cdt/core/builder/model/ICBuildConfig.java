/**********************************************************************
 * Copyright (c) 2002,2003 Timesys Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Based on org.eclipse.debug.core.ILaunchConfiguration
 * Timesys - Initial API and implementation
 **********************************************************************/

package org.eclipse.cdt.core.builder.model;

import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * A build configuration describes how to build a project.  It
 * is a collection of the various tool- and toolchain-specific
 * settings used to process the files in a project and produce
 * some end result.
 * <p>
 * A build configuration may be shared in a repository via
 * standard VCM mechanisms.
 * <p>
 * A build configuration is a handle to its underlying storage.
 * <p>
 * A build configuration is modified by obtaining a working copy
 * of a build configuration, modifying the working copy, and then
 * saving the working copy.
 * <p>
 * This interface is not intended to be implemented by clients.
 * <p>
 * @see ICBuildConfigWorkingCopy
 */
public interface ICBuildConfig extends IAdaptable {

	/*
	 * TBD: add convenience methods for accessing standard elements?
	 * 
	 * String[] getIncludePaths();
	 * String[] getLibPaths();
	 * String[] getLibs();
	 * String[] getOptimizationFlags();
	 * String[] getDebugFlags();
	 * String[] getWarningFlags();
	 */

	/**
	 * The file extension for build configuration files
	 * (value <code>"config"</code>).
	 * <p>
	 * CONSIDER: perhaps better to have a ".cdtconfig" file containing
	 * all build configuratons for the project in one spot?
	 */
	public static final String BUILD_CONFIGURATION_FILE_EXTENSION = "build"; //$NON-NLS-1$

	/**
	 * Configuration version.  Text string.
	 */
	public final static String CONFIG_VERSION = "config.version";

	/**
	 * Configuration name.  Text string.
	 */
	public final static String CONFIG_NAME = "config.name";

	/**
	 * Builds this configuration.
	 * 
	 * @param monitor progress monitor, or <code>null</code>
	 */
	public void build(IProgressMonitor monitor) throws CoreException;

	/**
	 * Returns the name of this build configuration.
	 * 
	 * @return the name of this build configuration
	 */
	public String getName();

	/**
	 * Returns the location of this build configuration as a
	 * path.
	 * 
	 * @return the location of this build configuration as a
	 *  path
	 */
	public IPath getLocation();

	/**
	 * Returns whether this build configuration's underlying
	 * storage exists.
	 * 
	 * @return whether this build configuration's underlying
	 *  storage exists
	 */
	public boolean exists();

	/**
	 * Returns the integer-valued attribute with the given name.  
	 * Returns the given default value if the attribute is undefined.
	 *
	 * @param attributeName the name of the attribute
	 * @param defaultValue the value to use if no value is found
	 * @return the value or the default value if no value was found.
	 * @exception CoreException if this method fails. Reasons include:
	 * <ul>
	 * <li>An exception occurs while retrieving the attribute from
	 *  underlying storage.</li>
	 * <li>An attribute with the given name exists, but does not
	 *  have an integer value</li>
	 * </ul>
	 */
	public int getAttribute(String attributeName, int defaultValue)
		throws CoreException;

	/**
	 * Returns the string-valued attribute with the given name.  
	 * Returns the given default value if the attribute is undefined.
	 *
	 * @param attributeName the name of the attribute
	 * @param defaultValue the value to use if no value is found
	 * @return the value or the default value if no value was found.
	 * @exception CoreException if this method fails. Reasons include:
	 * <ul>
	 * <li>An exception occurs while retrieving the attribute from
	 *  underlying storage.</li>
	 * <li>An attribute with the given name exists, but does not
	 *  have a String value</li>
	 * </ul>
	 */
	public String getAttribute(String attributeName, String defaultValue)
		throws CoreException;

	/**
	 * Returns the boolean-valued attribute with the given name.  
	 * Returns the given default value if the attribute is undefined.
	 *
	 * @param attributeName the name of the attribute
	 * @param defaultValue the value to use if no value is found
	 * @return the value or the default value if no value was found.
	 * @exception CoreException if this method fails. Reasons include:
	 * <ul>
	 * <li>An exception occurs while retrieving the attribute from
	 *  underlying storage.</li>
	 * <li>An attribute with the given name exists, but does not
	 *  have a boolean value</li>
	 * </ul>
	 */
	public boolean getAttribute(String attributeName, boolean defaultValue)
		throws CoreException;

	/**
	 * Returns the <code>java.util.List</code>-valued attribute with the given name.  
	 * Returns the given default value if the attribute is undefined.
	 *
	 * @param attributeName the name of the attribute
	 * @param defaultValue the value to use if no value is found
	 * @return the value or the default value if no value was found.
	 * @exception CoreException if this method fails. Reasons include:
	 * <ul>
	 * <li>An exception occurs while retrieving the attribute from
	 *  underlying storage.</li>
	 * <li>An attribute with the given name exists, but does not
	 *  have a List value</li>
	 * </ul>
	 */
	public List getAttribute(String attributeName, List defaultValue)
		throws CoreException;

	/**
	 * Returns the <code>java.util.Map</code>-valued attribute with the given name.  
	 * Returns the given default value if the attribute is undefined.
	 *
	 * @param attributeName the name of the attribute
	 * @param defaultValue the value to use if no value is found
	 * @return the value or the default value if no value was found.
	 * @exception CoreException if this method fails. Reasons include:
	 * <ul>
	 * <li>An exception occurs while retrieving the attribute from
	 *  underlying storage.</li>
	 * <li>An attribute with the given name exists, but does not
	 *  have a Map value</li>
	 * </ul>
	 */
	public Map getAttribute(String attributeName, Map defaultValue)
		throws CoreException;

	/**
	 * Returns the file this build configuration is stored
	 * in, or <code>null</code> if this configuration is stored
	 * locally with the workspace.
	 * 
	 * @return the file this build configuration is stored
	 *  in, or <code>null</code> if this configuration is stored
	 *  locally with the workspace
	 */
	public IFile getFile();

	/**
	 * Returns the project this build configuration is stored
	 * in.
	 * 
	 * @return the file this build configuration is stored in.
	 */
	public IProject getProject();

	/**
	 * Returns whether this build configuration is stored
	 * locally with the workspace.
	 * 
	 * @return whether this build configuration is stored
	 *  locally with the workspace
	 */
	public boolean isLocal();

	/**
	 * Returns a working copy of this build configuration.
	 * Changes to the working copy will be applied to this
	 * build configuration when saved. The working copy will
	 * refer to this build configuration as its original
	 * build configuration.
	 * 
	 * @return a working copy of this build configuration
	 * @exception CoreException if this method fails. Reasons include:
	 * <ul>
	 * <li>An exception occurs while initializing the contents of the
	 * working copy from this configuration's underlying storage.</li>
	 * </ul>
	 * @see ICBuildConfigWorkingCopy#getOriginal()
	 */
	public ICBuildConfigWorkingCopy getWorkingCopy() throws CoreException;

	/**
	 * Returns a copy of this build configuration, as a
	 * working copy, with the specified name. The new
	 * working copy does not refer back to this configuration
	 * as its original build configuration (the working copy
	 * will return <code>null</code> for <code>getOriginal()</code>).
	 * When the working copy is saved it will not effect this
	 * build configuration.
	 * 
	 * @param name the name of the copy
	 * @return a copy of this build configuration
	 * @exception CoreException if this method fails. Reasons include:
	 * <ul>
	 * <li>An exception occurs while initializing the contents of the
	 * working copy from this configuration's underlying storage.</li>
	 * </ul>
	 * @see ICBuildConfigWorkingCopy#getOriginal()
	 */
	public ICBuildConfigWorkingCopy copy(String name) throws CoreException;

	/**
	 * Returns whether this build configuration is a working
	 * copy.
	 * 
	 * @return whether this build configuration is a working
	 *  copy
	 */
	public boolean isWorkingCopy();

	/**
	 * Deletes this build configuration. This configuration's underlying
	 * storage is deleted. Has no effect if this configuration
	 * does not exist.
	 * 
	 * @exception CoreException if this method fails. Reasons include:
	 * <ul>
	 * <li>An exception occurs while deleting this configuration's
	 *  underlying storage.</li>
	 * </ul>
	 */
	public void delete() throws CoreException;

	/**
	 * Returns a memento for this build configuration, or <code>null</code>
	 * if unable to generate a memento for this configuration. A memento
	 * can be used to re-create a build configuration, via the
	 * build manager.
	 * 
	 * @return a memento for this configuration
	 * @see ICBuildConfigManager#getConfiguration(IProject, String)
	 * @exception CoreException if an exception occurs generating this
	 *  build configuration's memento 
	 */
	public String getMemento() throws CoreException;

	/**
	 * Returns whether the contents of this build configuration are 
	 * equal to the contents of the given build configuration.
	 * 
	 * @return whether the contents of this build configuration are equal to the contents
	 * of the specified build configuration.
	 */
	public boolean contentsEqual(ICBuildConfig configuration);
}
