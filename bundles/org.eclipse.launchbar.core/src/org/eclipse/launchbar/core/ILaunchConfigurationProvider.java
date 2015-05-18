/*******************************************************************************
 * Copyright (c) 2014, 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial
 *******************************************************************************/
package org.eclipse.launchbar.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.remote.core.IRemoteConnection;

/**
 * The provider of launch configurations of a given type for a given descriptor type
 * and a given target type. 
 * 
 * It is recommended to extend {@link AbstractLaunchConfigProvider} or one of it's
 * subclasses instead of implementing this directly.
 */
public interface ILaunchConfigurationProvider {

	/**
	 * Does this config provider provide launch configurations for the combination
	 * of descriptor and target.
	 * 
	 * Note: this is called when filtering targets for a descriptor. Processing
	 * should be minimal.
	 * 
	 * @param descriptor
	 * @param target
	 * @return
	 */
	boolean supports(ILaunchDescriptor descriptor, IRemoteConnection target) throws CoreException;

	/**
	 * Return the launch configuation type for the descriptor and target.
	 * 
	 * @param descriptor
	 * @param target launch configuration type or null if not supported
	 * @return
	 * @throws CoreException
	 */
	ILaunchConfigurationType getLaunchConfigurationType(ILaunchDescriptor descriptor,
			IRemoteConnection target) throws CoreException;

	/**
	 * Create a launch configuration for the descriptor to launch on the target.
	 *
	 * @param descriptor the descriptor to create the config for
	 * @param target the target to launch the config on
	 * @return launch configuration
	 * @throws CoreException 
	 */
	ILaunchConfiguration getLaunchConfiguration(ILaunchDescriptor descriptor, IRemoteConnection target)
			throws CoreException;

	/**
	 * Does this provider own the launch configuration.
	 * 
	 * @param configuration launch configuration
	 * @return true if this provider owns the launch configuration
	 * @throws CoreException
	 */
	boolean ownsLaunchConfiguration(ILaunchConfiguration configuration) throws CoreException;
	
	/**
	 * A launch configuration has been added.
	 * Return the launch object associated with this configuration and the launch bar manager
	 * will ensure the descriptor is created for it.
	 * 
	 * @param configuration
	 * @return whether this provider owns this launch configuration
	 * @throws CoreException
	 */
	Object launchConfigurationAdded(ILaunchConfiguration configuration) throws CoreException;

	/**
	 * A launch configuration has been removed. 
	 * This notification can be used to purge internal cache for example. 
	 * This method is called after launch configuration has been removed from file system,
	 * so accessing its attributes won't work.
	 * If provider cannot determine if it owns it it should return false. 
	 * 
	 * @param configuration
	 * @return true if provider owns this launch configuration
	 * @throws CoreException
	 */
	boolean launchConfigurationRemoved(ILaunchConfiguration configuration) throws CoreException;

	/**
	 * A launch descriptor has been removed. Remove any launch configurations that were
	 * created for it.
	 * 
	 * @param descriptor
	 * @throws CoreException
	 */
	void launchDescriptorRemoved(ILaunchDescriptor descriptor) throws CoreException;

	/**
	 * A launch target has been removed. Remove any launch configurations that were created
	 * for it.
	 * 
	 * @param target
	 * @throws CoreException
	 */
	void launchTargetRemoved(IRemoteConnection target) throws CoreException;

}
