/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.launchbar.core.target;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.launchbar.core.internal.target.LaunchTarget;

/**
 * A launch target is a thing that a launch will run on. Launch targets are
 * simple objects with the intention that the launch delegates and launches will
 * adapt this object to an object that will assist in performing the launch.
 *
 * @noimplement not to be implemented by clients
 */
public interface ILaunchTarget extends IAdaptable {
	public static final ILaunchTarget NULL_TARGET = new LaunchTarget("null", "null", "---");

	/**
	 * The id for the target. It is unique for each type.
	 * 
	 * @return id for the target.
	 */
	String getId();

	/**
	 * The user consumable name of the target.
	 *
	 * @return name of the target
	 */
	String getName();

	/**
	 * The type of the target.
	 *
	 * @return type of the target
	 */
	String getTypeId();

}
