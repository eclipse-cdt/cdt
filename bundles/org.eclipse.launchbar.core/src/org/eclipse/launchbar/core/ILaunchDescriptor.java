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
