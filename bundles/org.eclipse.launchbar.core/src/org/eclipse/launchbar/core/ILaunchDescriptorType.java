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

/**
 * Provides mapping between launch objects and launch descriptors.
 * 
 * It is strongly recommended to extend AbstarctLaunchDescriptorType instead of implementing this directly
 */
public interface ILaunchDescriptorType {
	/**
	 * Does this type own this launch object?
	 * 
	 * The main checking should be done in enablement expression of extension declaring the type,
	 * if enablement expression if defined this method can return true. 
	 * This also can used for fine-tuning of ownership
	 * which is hard to declared in xml. 
	 * 
	 * @param element
	 * @return owns element
	 * @throws CoreException 
	 */
	boolean ownsLaunchObject(Object launchObject) throws CoreException;

	/**
	 * Return a descriptor for the given launch object.
	 * 
	 * May return null to essentially eat the element so no other types
	 * create a descriptor for it.
	 * 
	 * @param descriptor launch object for descriptor
	 * @return the best descriptor
	 * @throws CoreException 
	 */
	ILaunchDescriptor getDescriptor(Object launchObject) throws CoreException;

}
