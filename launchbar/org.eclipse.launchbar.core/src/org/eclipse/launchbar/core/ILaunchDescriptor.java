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

import org.eclipse.core.runtime.IAdaptable;

/**
 * Represents a thing that can be launched. It is good practice that the
 * descriptor is adaptable to the launch object it is representing.
 */
public interface ILaunchDescriptor extends IAdaptable {

	/**
	 * Name to show in the launch descriptor selector. Names must be unique for
	 * all descriptors of a given type.
	 *
	 * @return name of the launch descriptor
	 */
	String getName();

	/**
	 * The type of launch descriptor.
	 *
	 * @return provider
	 */
	ILaunchDescriptorType getType();

}
