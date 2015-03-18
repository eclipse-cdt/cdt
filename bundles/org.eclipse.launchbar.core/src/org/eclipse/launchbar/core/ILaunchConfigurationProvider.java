/*******************************************************************************
 * Copyright (c) 2014 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Doug Schaefer
 *******************************************************************************/
package org.eclipse.launchbar.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;

/**
 * The provider of launch configurations of a given type for a given descriptor type
 * and a given target type. 
 * 
 * It is recommended to extend {@link LaunchConfigurationProvider}
 * instead of implementing this directly.
 */
public interface ILaunchConfigurationProvider {

	/**
	 * Does this provider own this launch configuration. If so, make sure the launch descriptor
	 * is properly constructed by sending in a launch object to the launch manager.
	 * And return that object.
	 * 
	 * @param configuration
	 * @return launch object that relates to this config or null it does not own it. 
	 * @throws CoreException
	 */
	Object launchConfigurationAdded(ILaunchConfiguration configuration) throws CoreException;

	/**
	 * A launch configuration has been removed. 
	 * It it fired after launch configuration has been removed from file system, so accessing its attributes won't work.
	 * This notification can be used to purge internal cache for example. 
	 * If provider cannot determine if it owns it it should return false. 
	 * 
	 * @param configuration
	 * @return true if provider owns this launch configuration
	 * @throws CoreException
	 */
	boolean launchConfigurationRemoved(ILaunchConfiguration configuration) throws CoreException;

	/**
	 * Returns the launch configuration type for configurations created by this provider.
	 * 
	 * @return launch configuration type
	 * @throws CoreException 
	 */
	ILaunchConfigurationType getLaunchConfigurationType() throws CoreException;

	/**
	 * Create a launch configuration for the descriptor to launch on the target.
	 * 
	 * @param descriptor
	 * @param target
	 * @return launch configuration
	 * @throws CoreException 
	 */
	ILaunchConfiguration createLaunchConfiguration(ILaunchManager launchManager, ILaunchDescriptor descriptor) throws CoreException;

}
