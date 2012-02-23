/*******************************************************************************
 * Copyright (c) 2012 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alex Ruiz (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.externaltool;

/**
 * Prints the output of an external tool to an Eclipse console. It uses the name of the external
 * tool as the console ID.
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 *
 * @author alruiz@google.com (Alex Ruiz)
 *
 * @since 2.1
 */
public interface IConsolePrinter {
	/**
	 * Clears the contents of the console.
	 */
	public void clear();

	/**
	 * Prints the specified message to the console, followed by a line separator string.
	 * @param message the message to print.
	 */
	public void println(String message);

	/**
	 * Prints a line separator to the console.
	 */
	public void println();

	/**
	 * Closes the output stream of the console.
	 */
	public void close();
}
