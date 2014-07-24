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

/**
 * Provides mapping between launch objects and launch descriptors.
 * 
 * It is strongly recommended to extend AbstarctLaunchDescriptorType instead of implementing this directly
 */
public interface ILaunchDescriptorType {

	/**
	 * The id for the provider.
	 * 
	 * @return provider id
	 */
	String getId();

	/**
	 * Called after existing launch configs have been added. The provider
	 * can now add any more that they'd like to have.
	 */
	void init(ILaunchBarManager manager);

	/**
	 * Does this type own this launch element.
	 * 
	 * @param element
	 * @return owns element
	 * @throws CoreException 
	 */
	boolean ownsLaunchObject(Object element) throws CoreException;
	
	/**
	 * Return a descriptor for the given element. The element can be a launch
	 * configuration, a project, or anything else that gets fed to the
	 * launch bar manager.
	 * 
	 * May return null to essentially eat the element so no other types
	 * create a descriptor for it.
	 * 
	 * @param descriptor candidate descriptor
	 * @return the best descriptor
	 * @throws CoreException 
	 */
	ILaunchDescriptor getDescriptor(Object element) throws CoreException;

	/**
	 * Return a handle to the launch bar manager.
	 * 
	 * @return launchbar manager
	 */
	ILaunchBarManager getManager();

}
