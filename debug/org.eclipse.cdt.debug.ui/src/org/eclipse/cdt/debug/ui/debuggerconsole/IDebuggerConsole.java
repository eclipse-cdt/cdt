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
	 * This console has become selected, the implementation shall use this
	 * notification to e.g. keep other views in sync with the context of the console
	 */
	void consoleSelected();

	/**
	 * Stop processing but don't dispose this console yet,
	 * i.e. It's desirable to keep the last I/O information available to the user
	 * @since 8.2
	 */
	default void stop() {
		// Nothing to do by default
	}
}
