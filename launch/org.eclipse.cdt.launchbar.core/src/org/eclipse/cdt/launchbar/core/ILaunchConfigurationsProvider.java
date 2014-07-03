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

/**
 * Provides the list of launch configurations
 *
 */
public interface ILaunchConfigurationsProvider {

	/**
	 * Called after existing launch configs have been added. The provider
	 * can now add any more that they'd like to have.
	 */
	void init(ILaunchBarManager manager);

	/**
	 * If the provider has a better descriptor than the suggested one, return a better one.
	 * Otherwise, return the one that was passed in.
	 * 
	 * @param descriptor candidate descriptor
	 * @return the best descriptor
	 */
	ILaunchConfigurationDescriptor filterDescriptor(ILaunchBarManager manager, ILaunchConfigurationDescriptor descriptor);

}
