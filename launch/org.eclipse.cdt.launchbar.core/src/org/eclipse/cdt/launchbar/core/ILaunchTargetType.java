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

public interface ILaunchTargetType {

	/**
	 * Called by the launchbar manager to initialize and pass a hendle to itself.
	 * 
	 * @param manager
	 */
	void init(ILaunchBarManager manager);

	/**
	 * The id of the target type.
	 * 
	 * @return target type id
	 */
	String getId();

	/**
	 * Return the list of targets for this type.
	 * 
	 * @return targets
	 */
	ILaunchTarget[] getTargets();

	/**
	 * Return the target with the specified id.
	 * 
	 * @param id
	 * @return target
	 */
	ILaunchTarget getTarget(String id);

}
