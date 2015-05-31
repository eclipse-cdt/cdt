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
 */
public interface ILaunchDescriptorType {

	/**
	 * Return a descriptor for the given launch object.
	 * 
	 * May return null to essentially eat the element so no other types create a
	 * descriptor for it.
	 * 
	 * The enablement expression for a given launch object must pass for this
	 * clause to be executed.
	 * 
	 * @param descriptor
	 *            launch object for descriptor
	 * @return the best descriptor
	 * @throws CoreException
	 */
	ILaunchDescriptor getDescriptor(Object launchObject) throws CoreException;

}
