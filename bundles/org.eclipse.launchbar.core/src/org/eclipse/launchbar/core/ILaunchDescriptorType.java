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

	/**
	 * Does this descriptor type support launching on targets other than Local?
	 *
	 * @return supports targets
	 * @throws CoreException
	 * @since 2.1
	 */
	default boolean supportsTargets() throws CoreException {
		return true;
	}

}
