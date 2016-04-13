/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.launchbar.core.target;

/**
 * Working copy to set attributes on a target and then save them.
 */
public interface ILaunchTargetWorkingCopy extends ILaunchTarget {

	/**
	 * Get the original launch target.
	 * 
	 * @return the original launch target
	 */
	ILaunchTarget getOriginal();

	/**
	 * Set an attribute.
	 * 
	 * @param key
	 *            key
	 * @param value
	 *            value
	 */
	void setAttribute(String key, String value);

	/**
	 * Save the changed attributes to the original working copy.
	 * 
	 * @return original launch target
	 */
	ILaunchTarget save();

}
