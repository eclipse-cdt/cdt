/**********************************************************************
 * Copyright (c) 2002,2003 Timesys Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Based on org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationManager
 * Timesys - Initial API and implementation
 **********************************************************************/

package org.eclipse.cdt.core.builder.model;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

/**
 * The build configuration manager manages the set of registered build
 * configurations. Clients interested in build configuration change
 * notification may register with the build configuration manager.
 * <p>
 * Clients are not intended to implement this interface.
 * </p>
 * @see ICBuildConfigListener
 */
public interface ICBuildConfigManager {
	
	/**
	 * Adds the given listener to the collection of registered
	 * configuration listeners. Has no effect if an identical
	 * listener is already registerd.
	 *
	 * @param listener the listener to register
	 */
	public void addListener(ICBuildConfigListener listener);

	/**
	 * Removes the given listener from the collection of registered
	 * configuration listeners.  Has no effect if an identical listener
	 * is not already registerd.
	 *
	 * @param listener the listener to deregister
	 */
	public void removeListener(ICBuildConfigListener listener);

	/**
	 * Adds the specified configuration and notifies listeners. Has no
	 * effect if an identical configuration is already registered.
	 * 
	 * @param configuration the configuration to add
	 */
	public void addConfiguration(ICBuildConfig configuration);	

	/**
	 * Removes the specified configuration and notifies listeners.
	 * Has no effect if an identical configuration is not already
	 * registered.
	 *
	 * @param configuration the configuration to remove
	 * @since 2.0
	 */
	public void removeConfiguration(ICBuildConfig configuration);
	
	/**
	 * Returns all build configurations associated with a project.
	 * Returns an zero-length array if no configurations are associated
	 * with the project.
	 * 
	 * @param project project to retrieve build configurations for.
	 * @return all build configurations of the specified type for the project.
	 * @exception CoreException if an error occurs while retreiving a build configuration
	 */
	public ICBuildConfig[] getConfigurations(IProject project) throws CoreException;
	
	/**
	 * Returns a handle to the configuration contained in the specified
	 * file. The file is not verified to exist or contain a proper
	 * configuration.
	 * 
	 * @param file configuration file
	 * @return a handle to the configuration contained in the specified file
	 */
	public ICBuildConfig getConfiguration(IFile file);
	
	/**
	 * Returns a handle to the configuration specified by the given
	 * memento. The configuration may not exist.
	 * 
	 * @return a handle to the configuration specified by the given memento
	 * @exception CoreException if the given memento is invalid or
	 *  an exception occurs parsing the memento
	 */
	public ICBuildConfig getConfiguration(String memento) throws CoreException;

	/**
	 * Returns a handle to a newly created build configuration.
	 * 
	 * @param name Name of new configuration.
	 * @return a handle to a new configuration instance.
	 */
	public ICBuildConfigWorkingCopy getConfiguration(IProject project, String name);
	
	/**
	 * Return <code>true</code> if there is a configuration with the specified name, 
	 * <code>false</code> otherwise.
	 * 
	 * @param name the name of the configuration whose existence is being checked
	 * @exception CoreException if unable to retrieve existing configuration names
	 */
	public boolean isExistingConfigurationName(IProject project, String name) throws CoreException;

	/**
	 * Return a String that can be used as the name of a configuration.  The name
	 * is guaranteed to be unique (no existing configurations will have this name).
	 * The name that is returned uses the <code>namePrefix</code> as a starting point.
	 * If there is no existing configuration with this name, then <code>namePrefix</code>
	 * is returned.  Otherwise, the value returned consists of the specified prefix plus
	 * some suffix that guarantees uniqueness.
	 * 
	 * @param namePrefix the String that the returned name must begin with
	 */
	public String generateUniqueConfigurationNameFrom(IProject project, String namePrefix);
}
