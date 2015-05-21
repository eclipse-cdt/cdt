/*******************************************************************************
 * Copyright (c) 2015 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Red Hat Inc. - Initial API and implementation
 *******************************************************************************/
package org.eclipse.remote.console;

import java.util.List;

import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.internal.console.TerminalConsoleFactory;
import org.eclipse.ui.console.IConsole;

/**
 * A collection of public API utility methods to open
 * consoles to IRemoteConnection objects
 */
public class TerminalConsoleUtility {
	/**
	 * Opens a dialog to allow selection of an IRemoteConnection,
	 * encoding, etc. and then open a console to it.
	 */
	public void openConsole() {
		new TerminalConsoleFactory().openConsole();
	}

	/**
	 * Open a specific IRemoteConnection and encoding combination.
	 * 
	 * @param connection
	 * @param encoding
	 */
	public static void openConsole(final IRemoteConnection connection, final String encoding) {
		TerminalConsoleFactory.openConsole(connection, encoding);
	}

	/**
	 * Find an existing console for the given IRemoteConnection
	 * 
	 * @param connection
	 * @return
	 */
	public static List<IConsole> findConsole(IRemoteConnection connection) {
		return TerminalConsoleFactory.findConsole(connection);
	}
}
