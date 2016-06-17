/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.debug.ui.debuggerconsole;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.part.IPageBookViewPage;

/**
 * @since 8.1
 */
public interface IDebuggerConsole extends IConsole {
	/**
	 * Returns the launch associated with this console.
	 * 
	 * @return the launch associated with this console.
	 */
	ILaunch getLaunch();
	
	/**
	 * Creates and returns a new page for this console. The page is displayed
	 * for this console in the console given view.
	 * 
	 * @param view the view in which the page is to be created
	 * @return a page book view page representation of this console
	 */
	IPageBookViewPage createDebuggerPage(IDebuggerConsoleView view);
	
	/**
	 * Request a re-computation of the name of the console.
	 */
	void resetName();
	
	/**
	 * Let the console know that it has become selected
	 */
	public void consoleSelected();
}
