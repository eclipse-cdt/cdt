/*******************************************************************************
 * Copyright (c) 2000, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Navid Mehregani (TI) - Bug 289526 - Migrate the Restart feature to the new one, as supported by the platform
 *******************************************************************************/

package org.eclipse.cdt.debug.core.model;

import org.eclipse.debug.core.DebugException;
/**
 * Provides the ability to restart a debug target.
 * 
 * @deprecated Use org.eclipse.debug.core.commands.IRestartHandler instead.  IRestartHandler
 * handles the call in an asynchronous fashion.
 */
public interface IRestart 
{
	/**
	 * Returns whether this element can currently be restarted.
	 *
	 * @return whether this element can currently be restarted
	 */
	public boolean canRestart();

	/**
	 * Causes this element to restart its execution.
	 *
	 * @exception DebugException on failure. Reasons include:
	 */
	public void restart() throws DebugException;
}

