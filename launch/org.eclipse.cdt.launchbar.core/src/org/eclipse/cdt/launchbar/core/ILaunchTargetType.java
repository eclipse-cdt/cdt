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

public interface ILaunchTargetType {

	/**
	 * Add initial targets and set up any listeners.
	 * 
	 * @param manager
	 * @throws CoreException 
	 */
	void init(ILaunchBarManager manager) throws CoreException;

	/**
	 * Shutting down, remove any listeners
	 */
	void dispose();

}
