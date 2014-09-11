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

public interface ILaunchBarManager {

	/**
	 * A launch object has been added. Create a matching launch descriptor if available.
	 * 
	 * @param element launch object
	 * @return the launch descriptor that got created, null of none was
	 * @throws CoreException
	 */
	ILaunchDescriptor launchObjectAdded(Object launchObject) throws CoreException;

	/**
	 * A launch object has been removed. Remove the associated launch descriptor if there is one.
	 * 
	 * @param element launch object
	 * @throws CoreException
	 */
	void launchObjectRemoved(Object launchObject) throws CoreException;

	/**
	 * A launch object has changed in some way that affects the launch bar.
	 * 
	 * @param launchObject
	 * @throws CoreException
	 */
	void launchObjectChanged(Object launchObject) throws CoreException;

	/**
	 * A new launch target has been added.
	 * 
	 * @param target launch target
	 * @throws CoreException 
	 */
	void launchTargetAdded(ILaunchTarget target) throws CoreException;

	/**
	 * A launch target has been removed.
	 * 
	 * @param target launch target
	 * @throws CoreException 
	 */
	void launchTargetRemoved(ILaunchTarget target) throws CoreException;

	/**
	 * The launch target has changed in some way that affects the
	 * launch bar.
	 * 
	 * @param target launch target
	 */
	void launchTargetChanged(ILaunchTarget target);

	// TODO API for adding and removing types.

}
