/*******************************************************************************
 *  Copyright (c) 2009 Wind River Systems and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *      Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.service.command;

import org.eclipse.cdt.dsf.concurrent.ConfinedToDsfExecutor;

/**
 * Synchronous listener for events issued from the debugger.  All
 * registered listeners will be called in the same dispatch cycle.
 *
 * @since 1.0
 */
@ConfinedToDsfExecutor("")
public interface IEventListener {
	/**
	 * Notifies that the given asynchronous output was received from the
	 * debugger.
	 * @param output output that was received from the debugger.  Format
	 * of the output data is debugger specific.
	 */
	public void eventReceived(Object output);
}
