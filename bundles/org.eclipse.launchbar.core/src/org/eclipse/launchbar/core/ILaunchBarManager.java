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
	 * A launch object has been added. Create a matching launch descriptor if available.
	 *
	 * @param element
	 *            launch object
	 * @return the launch descriptor that got created, null of none was
	 * @throws CoreException
	 */
	ILaunchDescriptor launchObjectAdded(Object launchObject) throws CoreException;

	/**
	 * A launch object has been removed. Remove the associated launch descriptor if there is one.
	 *
	 * @param element
	 *            launch object
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
	 * Return the type id for the given launch descriptor type. This is defined in the extension
	 * point that defines the type.
	 * 
	 * @param descType
	 *            descriptor type
	 * @return the type id for the descriptor type
	 */
	String getDescriptorTypeId(ILaunchDescriptorType descType) throws CoreException;

	ILaunchDescriptor getActiveLaunchDescriptor() throws CoreException;
	
	ILaunchMode getActiveLaunchMode() throws CoreException;

	ILaunchTarget getActiveLaunchTarget() throws CoreException;
	
	ILaunchConfiguration getActiveLaunchConfiguration() throws CoreException;

	ILaunchConfiguration getLaunchConfiguration(ILaunchDescriptor desc, ILaunchTarget target) throws CoreException;
	
	ILaunchConfigurationType getLaunchConfigurationType(ILaunchDescriptor desc, ILaunchTarget target) throws CoreException;
	
	ILaunchDescriptor[] getLaunchDescriptors() throws CoreException;
	
	void setActiveLaunchDescriptor(ILaunchDescriptor desc) throws CoreException;

	ILaunchMode[] getLaunchModes() throws CoreException;
	
	void setActiveLaunchMode(ILaunchMode mode) throws CoreException;

	ILaunchTarget[] getLaunchTargets(ILaunchDescriptor desc) throws CoreException;
	
	void setActiveLaunchTarget(ILaunchTarget target) throws CoreException;
	
}
