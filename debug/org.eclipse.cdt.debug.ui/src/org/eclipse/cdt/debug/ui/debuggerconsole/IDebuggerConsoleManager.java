/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
	 * Terminates the given console and release associated resources
	 * but does not get removed from the view or console manager
	 * @param console console to terminate
	 */
	public void terminateConsole(IDebuggerConsole console);

	/**
	 * Returns a array of consoles registered with the console manager.
	 * 
	 * @return an array of consoles registered with the console manager
	 */
	public IDebuggerConsole[] getConsoles();
	
	/**
	 * Opens the console view and displays given the console.
	 * If the view is already open, it is brought to the front.
	 * Has no effect if the given console is not currently registered.
	 * 
	 * @param console console to display
	 */
	public void showConsoleView(IDebuggerConsole console);
}
