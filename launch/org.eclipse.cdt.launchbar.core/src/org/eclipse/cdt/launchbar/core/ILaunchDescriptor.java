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
 * Represents a thing that can be launched.
 */
public interface ILaunchDescriptor {

	/**
	 * Name to show in the launch descriptor selector.
	 * 
	 * @return name of the launch descriptor
	 */
	String getName();

	/**
	 * Unique id of the descriptor (globally)
	 * 
	 * @return the non null string representing id of the launch descriptor
	 */
	String getId();

	/**
	 * The type of launch descriptor.
	 * 
	 * @return provider
	 */
	ILaunchDescriptorType getType();

	/**
	 * Descriptor considered open when it is visible to user, and closed otherwise
	 */
	boolean isOpen();

}
