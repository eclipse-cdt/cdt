/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.debug.ui.debuggerconsole;

import org.eclipse.ui.console.IConsoleListener;

/**
 * @since 8.1
 */
public interface IDebuggerConsoleManager {
	/**
	 * Registers the given listener for console notifications. Has
	 * no effect if an identical listener is already registered.
	 *
	 * @param listener listener to register
	 */
	public void addConsoleListener(IConsoleListener listener);

	/**
	 * Unregisters the given listener for console notifications. Has
	 * no effect if listener is not already registered.
	 *
	 * @param listener listener to unregister
	 */
	public void removeConsoleListener(IConsoleListener listener);

	/**
	 * Adds the given console to the console manager. Has no effect for
	 * equivalent consoles already registered.
	 *
	 * @param console console to add
	 */
	public void addConsole(IDebuggerConsole console);

	/**
	 * Removes the given console from the console manager.
	 *
	 * @param console console to remove
	 */
	public void removeConsole(IDebuggerConsole console);

	/**
	 * Returns a array of consoles registered with the console manager.
	 *
	 * @return an array of consoles registered with the console manager
	 */
	public IDebuggerConsole[] getConsoles();

	/**
	 * Shows the console view, by opening it if necessary.
	 * Once open, or if the view was already open, it is brought to the front.
	 */
	public void showConsoleView();

	/**
	 * Opens the console view but does not bring it to the front.
	 * Does nothing if the view is already open.
	 */
	public void openConsoleView();
}
