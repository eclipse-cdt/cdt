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
	 * Get the id for the target. The id of the active target is
	 * stored in the preference store.
	 * 
	 * @return id
	 */
	String getId();

	/**
	 * Returns a name to show in the UI for this target.
	 * 
	 * @return name
	 */
	String getName();

	/**
	 * Returns the type for this target.
	 * 
	 * @return target type
	 */
	ILaunchTargetType getType();

	/**
	 * This target has been made active.
	 */
	void setActive();

}
