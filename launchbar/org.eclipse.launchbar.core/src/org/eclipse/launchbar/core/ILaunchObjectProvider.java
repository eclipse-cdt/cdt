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
 * An extension that serves up objects to feed launch descriptors.
 *
 */
public interface ILaunchObjectProvider {

	/**
	 * Add initial launch objects and set up listeners for new ones.
	 *
	 * @param launchbar
	 *            manager
	 * @throws CoreException
	 */
	void init(ILaunchBarManager manager) throws CoreException;

	/**
	 * Shutting down, remove any listeners.
	 */
	void dispose();

}
