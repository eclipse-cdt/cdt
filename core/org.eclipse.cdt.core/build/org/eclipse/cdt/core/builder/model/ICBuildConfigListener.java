/**********************************************************************
 * Copyright (c) 2002,2003 Timesys Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Based on org.eclipse.debug.core.ILaunchConfigurationListener
 * Timesys - Initial API and implementation
 **********************************************************************/

package org.eclipse.cdt.core.builder.model;

/**
 * A build configuration listener is notified of build
 * configurations as they are added and removed from the
 * build configuration manager.
 * <p>
 * Clients may implement this interface.
 */
public interface ICBuildConfigListener {

	/**
	 * Notifies this listener that the specified
	 * configuration has been removed.
	 *
	 * @param configuration the removed configuration
	 */
	public void configurationRemoved(ICBuildConfig configuration);

	/**
	 * Notifies this listener that the specified configuration
	 * has been added.
	 * 
	 * @param configuration the newly added configuration
	 */
	public void configurationAdded(ICBuildConfig configuration);

	/**
	 * Notifies this listener that the specified configuration
	 * has changed.
	 * 
	 * @param configuration the changed configuration
	 */
	public void configurationChanged(ICBuildConfig configuration);
}
