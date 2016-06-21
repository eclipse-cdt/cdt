/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.debug.ui.debuggerconsole;

import org.eclipse.ui.IViewPart;

/**
 * @since 8.1
 */
public interface IDebuggerConsoleView extends IViewPart {
	/**
	 * Displays the page for the given console in this console view.
	 *
	 * @param console console to display, cannot be <code>null</code>
	 */
	void display(IDebuggerConsole console);

	/**
	 * Returns the console currently being displayed, or <code>null</code>
	 * if none
	 *
	 * @return the console currently being displayed, or <code>null</code>
	 *  if none
	 */
	IDebuggerConsole getCurrentConsole();
}
