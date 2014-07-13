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
package org.eclipse.cdt.launchbar.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;

public interface ILaunchConfigurationProvider {

	/**
	 * Do any initialization.
	 * 
	 * @param manager
	 * @throws CoreException
	 */
	void init(ILaunchBarManager manager) throws CoreException;
	
	/**
	 * Does this provider own this launch configuration. If so, make sure the launch descriptor
	 * is properly constructed by sending in a launch object to the launch manager.
	 * 
	 * @param configuration
	 * @return
	 * @throws CoreException
	 */
	boolean ownsLaunchConfiguration(ILaunchConfiguration configuration) throws CoreException;
	
	/**
	 * Returns the launch configuration type used to launch the descriptor on this target type.
	 * 
	 * @param descriptor
	 * @param target
	 * @return launch configuration type
	 * @throws CoreException 
	 */
	ILaunchConfigurationType getLaunchConfigurationType(ILaunchDescriptor descriptor) throws CoreException;

	/**
	 * Create a launch configuration for the descriptor to launch on the target.
	 * 
	 * @param descriptor
	 * @param target
	 * @return launch configuration
	 * @throws CoreException 
	 */
	ILaunchConfiguration getLaunchConfiguration(ILaunchDescriptor descriptor) throws CoreException;

	/**
	 * A launch configuration has been removed.
	 * 
	 * @param configuration
	 * @throws CoreException
	 */
	void launchConfigurationRemoved(ILaunchConfiguration configuration) throws CoreException;

}
