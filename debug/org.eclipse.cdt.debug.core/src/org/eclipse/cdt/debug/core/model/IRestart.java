/*******************************************************************************
 * Copyright (c) 2000, 2010 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Navid Mehregani (TI) - Bug 289526 - Migrate the Restart feature to the new one, as supported by the platform
 *******************************************************************************/

package org.eclipse.cdt.debug.core.model;

import org.eclipse.debug.core.DebugException;

/**
 * Provides the ability to restart a debug target.
 * <p>
 * Note: Debug elements which support restart should implement this interface.
 * Adopting to this interface is not enough.
 * </p>
 * <p>
 * Note 2: Debugger can also implement the asynchronous
 * {@link org.eclipse.debug.core.commands.IRestartHandler}.
 * </p>
 *
 * @see org.eclipse.debug.core.commands.IRestartHandler
 */
public interface IRestart {
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
