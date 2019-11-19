/*******************************************************************************
 * Copyright (c) 2014 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Doug Schaefer
 *******************************************************************************/
package org.eclipse.launchbar.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationListener;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchMode;
import org.eclipse.launchbar.core.target.ILaunchTarget;

/**
 * Interface to the Launch Bar Manager.
 *
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ILaunchBarManager extends ILaunchConfigurationListener {

	/**
	 * A launch object has been added. Create a matching launch descriptor if
	 * available.
	 *
	 * @param element launch object
	 * @return the launch descriptor that got created, null of none was
	 * @throws CoreException
	 */
	ILaunchDescriptor launchObjectAdded(Object launchObject) throws CoreException;

	/**
	 * A launch object has been removed. Remove the associated launch descriptor if
	 * there is one.
	 *
	 * @param element launch object
	 * @throws CoreException
	 */
	void launchObjectRemoved(Object launchObject) throws CoreException;

	/**
	 * A launch object has changed in some way that affects the launch bar.
	 *
	 * @param launchObject
	 * @throws CoreException
	 */
	void launchObjectChanged(Object launchObject) throws CoreException;

	/**
	 * Add a listener that can react to launch bar changes
	 * 
	 * @param listener
	 */
	void addListener(ILaunchBarListener listener);

	/**
	 * Remove a listener
	 * 
	 * @param listener
	 */
	void removeListener(ILaunchBarListener listener);

	/**
	 * Return the type id for the given launch descriptor type. This is defined in
	 * the extension point that defines the type.
	 * 
	 * @param descType descriptor type
	 * @return the type id for the descriptor type
	 */
	String getDescriptorTypeId(ILaunchDescriptorType descType) throws CoreException;

	/**
	 * Returns the active launch descriptor.
	 * 
	 * @return active launch descriptor
	 * @throws CoreException
	 */
	ILaunchDescriptor getActiveLaunchDescriptor() throws CoreException;

	/**
	 * Returns the active launch mode.
	 * 
	 * @return active launch mode
	 * @throws CoreException
	 */
	ILaunchMode getActiveLaunchMode() throws CoreException;

	/**
	 * Returns the active launch target.
	 * 
	 * @return active launch target
	 * @throws CoreException
	 */
	ILaunchTarget getActiveLaunchTarget() throws CoreException;

	/**
	 * Returns the active launch configuration as derived from the active descriptor
	 * and target.
	 * 
	 * @return active launch configuration
	 * @throws CoreException
	 */
	ILaunchConfiguration getActiveLaunchConfiguration() throws CoreException;

	/**
	 * Returns the launch configuration derived from the given launch descriptor and
	 * target.
	 * 
	 * @param desc   launch descriptor
	 * @param target launch target
	 * @return launch configuration
	 * @throws CoreException
	 */
	ILaunchConfiguration getLaunchConfiguration(ILaunchDescriptor desc, ILaunchTarget target) throws CoreException;

	/**
	 * Returns the launch configuration type used for configurations that are
	 * derived from the given launch descriptor and launch target without creating a
	 * launch configuration.
	 * 
	 * @param desc   launch descriptor
	 * @param target launch target
	 * @return launch configuration type
	 * @throws CoreException
	 */
	ILaunchConfigurationType getLaunchConfigurationType(ILaunchDescriptor desc, ILaunchTarget target)
			throws CoreException;

	/**
	 * Returns all know launch descriptors.
	 * 
	 * @return launch descriptors
	 * @throws CoreException
	 */
	ILaunchDescriptor[] getLaunchDescriptors() throws CoreException;

	/**
	 * Set the active launch descriptor.
	 * 
	 * @param desc launch descriptor
	 * @throws CoreException
	 */
	void setActiveLaunchDescriptor(ILaunchDescriptor desc) throws CoreException;

	/**
	 * Return all launch modes for the active launch descriptor and active launch target.
	 * 
	 * @return launch modes
	 * @throws CoreException
	 */
	ILaunchMode[] getLaunchModes() throws CoreException;

	/**
	 * Set the active launch mode.
	 * 
	 * @param mode launch mode
	 * @throws CoreException
	 */
	void setActiveLaunchMode(ILaunchMode mode) throws CoreException;

	/**
	 * Return all launch targets supported by the given launch descriptor.
	 * 
	 * @param desc launch descriptor 
	 * @return launch targets
	 * @throws CoreException
	 */
	ILaunchTarget[] getLaunchTargets(ILaunchDescriptor desc) throws CoreException;

	/**
	 * Set the active launch target.
	 * 
	 * @param target launch target
	 * @throws CoreException
	 */
	void setActiveLaunchTarget(ILaunchTarget target) throws CoreException;

}
