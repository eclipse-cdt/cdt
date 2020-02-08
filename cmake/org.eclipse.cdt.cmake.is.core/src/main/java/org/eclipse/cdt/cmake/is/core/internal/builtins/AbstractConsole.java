/*******************************************************************************
 * Copyright (c) 2018 Martin Weber.
 *
 * Content is provided to you under the terms and conditions of the Eclipse Public License Version 2.0 "EPL".
 * A copy of the EPL is available at http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.cmake.is.core.internal.builtins;

import org.eclipse.cdt.core.ConsoleOutputStream;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.ui.IBuildConsoleManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Martin Weber
 */
public abstract class AbstractConsole implements IConsole {

	private IConsole console;

	/**
	 * Gets a console manager that is configured to the console`s display-name, the
	 * ID and icon.
	 *
	 * @see org.eclipse.cdt.ui.CUIPlugin#getConsoleManager(String, String)
	 */
	protected abstract IBuildConsoleManager getConsoleManager();

	@Override
	public ConsoleOutputStream getOutputStream() throws CoreException {
		return console.getOutputStream();
	}

	@Override
	public ConsoleOutputStream getInfoStream() throws CoreException {
		return console.getInfoStream();
	}

	@Override
	public ConsoleOutputStream getErrorStream() throws CoreException {
		return console.getErrorStream();
	}

	@Override
	public void start(IProject project) {
		IBuildConsoleManager consoleManager = getConsoleManager();
		console = consoleManager.getConsole(project);
		console.start(project);
	}

}