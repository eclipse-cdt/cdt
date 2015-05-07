/*******************************************************************************
 * Copyright (c) 2011 - 2015 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.terminal.view.core.interfaces;

import java.util.Map;

import org.eclipse.core.runtime.IStatus;

/**
 * Terminal service.
 */
public interface ITerminalService {

	/**
	 * Client call back interface.
	 */
	public interface Done {
		/**
		 * Called when the terminal service operation is done.
		 *
		 * @param status The status of the terminal service operation.
		 */
		public void done(IStatus status);
	}

	/**
	 * Opens a terminal asynchronously and invokes the given callback if done.
	 *
	 * @param properties The terminal properties. Must not be <code>null</code>.
	 * @param done The callback to invoke if finished or <code>null</code>.
	 */
	public void openConsole(Map<String, Object> properties, Done done);

	/**
	 * Close the terminal asynchronously and invokes the given callback if done.
	 *
	 * @param properties The terminal properties. Must not be <code>null</code>.
	 * @param done The callback to invoke if finished or <code>null</code>.
	 */
	public void closeConsole(Map<String, Object> properties, Done done);

	/**
	 * Terminate (disconnect) the terminal asynchronously and invokes the given callback if done.
	 *
	 * @param properties The terminal properties. Must not be <code>null</code>.
	 * @param done The callback to invoke if finished or <code>null</code>.
	 */
	public void terminateConsole(Map<String, Object> properties, Done done);

	/**
	 * Register the given listener to receive notifications about terminal events.
	 * Calling this method multiple times with the same listener has no effect.

	 * @param listener The terminal tab listener. Must not be <code>null</code>.
	 */
	public void addTerminalTabListener(ITerminalTabListener listener);

	/**
	 * Unregister the given listener from receiving notifications about terminal
	 * events. Calling this method multiple times with the same listener
	 * has no effect.
	 *
	 * @param listener The terminal tab listener. Must not be <code>null</code>.
	 */
	public void removeTerminalTabListener(ITerminalTabListener listener);
}
