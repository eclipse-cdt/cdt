/*******************************************************************************
 * Copyright (c) 2014, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - initial
 *******************************************************************************/
package org.eclipse.launchbar.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.launchbar.core.target.ILaunchTarget;

/**
 * The provider of launch configurations of a given type for a given descriptor
 * type and a given target type.
 *
 * It is recommended to extend {@link AbstractLaunchConfigProvider} or one of
 * it's subclasses instead of implementing this directly.
 */
public interface ILaunchConfigurationProvider {
	/**
	 * Does this config provider provide launch configurations for the
	 * combination of descriptor and target.
	 *
	 * Note: this is called when filtering targets for a descriptor. Processing
	 * should be minimal.
	 *
	 * @param descriptor
	 * @param target
	 * @return true if target is supported, false otherwise.
	 */
	boolean supports(ILaunchDescriptor descriptor, ILaunchTarget target) throws CoreException;

	/**
	 * Return the launch configuation type for the descriptor and target.
	 *
	 * @param descriptor
	 * @param target
	 *            launch configuration type or null if not supported
	 * @return
	 * @throws CoreException
	 */
	ILaunchConfigurationType getLaunchConfigurationType(ILaunchDescriptor descriptor, ILaunchTarget target)
			throws CoreException;

	/**
	 * Create a launch configuration for the descriptor to launch on the target.
	 *
	 * @param descriptor
	 *            the descriptor to create the config for
	 * @param target
	 *            the target to launch the config on
	 * @return launch configuration
	 * @throws CoreException
	 */
	ILaunchConfiguration getLaunchConfiguration(ILaunchDescriptor descriptor, ILaunchTarget target)
			throws CoreException;

	/**
	 * Return true if given descriptor is associated with given launch configuration.
	 * I.e. if getLaunchConfiguration(descriptor, target) returns a launch configuration,
	 * calling launchDescriptorMatches(descriptor, configuration, target) should return true.
	 *
	 * This method is used to activate descriptor when launch happens, return false all the time would mean
	 * launch would track launches of this specific descriptor (if they occur outside of launchbar).
	 * @param configuration
	 *            launch configuration in question
	 * @param target - target to match, it can be null if launch bar cannot determine target,
	 * 			in this case it should act as wildcard
	 *
	 * @return launch configuration
	 * @throws CoreException
	 */
	boolean launchDescriptorMatches(ILaunchDescriptor descriptor, ILaunchConfiguration configuration,
			ILaunchTarget target) throws CoreException;

	/**
	 * A launch configuration has been added. Provider can inspect it and
	 * associate with internal map. Provider should make sure it owns this
	 * launch configuration or it can modify it to take over.
	 *
	 * @return true of provider owns this launch configuration
	 */
	boolean launchConfigurationAdded(ILaunchConfiguration configuration) throws CoreException;

	/**
	 * A launch configuration has been removed. This notification can be used to
	 * purge internal cache for example. This method is called after launch
	 * configuration has been removed from file system, so accessing its
	 * attributes won't work. If provider cannot determine if it owns it it
	 * should return false.
	 *
	 * @param configuration
	 * @return true if provider owns this launch configuration
	 * @throws CoreException
	 */
	boolean launchConfigurationRemoved(ILaunchConfiguration configuration) throws CoreException;

	/**
	 * A launch configuration has been changed. Provider can inspect it to
	 * re-evaluate its internal map. Provider should make sure it owns this
	 * launch configuration or it can modify it to take over.
	 *
	 * @return true of provider owns this launch configuration
	 */
	boolean launchConfigurationChanged(ILaunchConfiguration configuration) throws CoreException;

	/**
	 * A launch descriptor has been removed. Remove any launch configurations
	 * that were created for it.
	 *
	 * @param descriptor
	 * @throws CoreException
	 */
	void launchDescriptorRemoved(ILaunchDescriptor descriptor) throws CoreException;

	/**
	 * A launch target has been removed. Remove any launch configurations that
	 * were created for it.
	 *
	 * @param target
	 * @throws CoreException
	 */
	void launchTargetRemoved(ILaunchTarget target) throws CoreException;

}
