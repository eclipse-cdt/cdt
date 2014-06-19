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

public interface ILaunchConfigurationDescriptor {

	/**
	 * Name to show in the launch configuration selector.
	 * 
	 * @return name of the launch configuration
	 */
	String getName();

	/**
	 * The type of launch configuration supported by this descriptor.
	 * 
	 * @return
	 */
	ILaunchConfigurationType getLaunchConfigurationType() throws CoreException;

	/**
	 * The corresponding launch configuration.
	 * If this launch config hasn't been created yet, it will be
	 * 
	 * @return the corresponding launch configuration
	 * @throws CoreException 
	 */
	ILaunchConfiguration getLaunchConfiguration() throws CoreException;

	/**
	 * Is this launch configuration managed by this descriptor.
	 * 
	 * @param launchConfiguration
	 * @return
	 */
	boolean matches(ILaunchConfiguration launchConfiguration);
	
}
