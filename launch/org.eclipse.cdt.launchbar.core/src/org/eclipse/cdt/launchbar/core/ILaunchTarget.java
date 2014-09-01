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

import org.eclipse.core.runtime.IAdaptable;

public interface ILaunchTarget extends IAdaptable {

	/**
	 * Returns the name of this target.
	 * Names must be unique across all targets of a given type.
	 * 
	 * @return name of the target
	 */
	String getName();

	/**
	 * Returns the type for this target.
	 * 
	 * @return type of the target 
	 */
	ILaunchTargetType getType();

	/**
	 * The active state of this target has changed.
	 * 
	 * @param active active state of the target
	 */
	void setActive(boolean active);

}
