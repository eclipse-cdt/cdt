/*******************************************************************************
 * Copyright (c) 2002, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.buildconsole;

import java.net.URL;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.cdt.core.ConsoleOutputStream;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.IBuildConsoleManager;

import org.eclipse.cdt.internal.core.ICConsole;

/**
 * CDT console adaptor providing output streams. The adaptor provides means of
 * access to UI plugin console.
 */
public class CBuildConsole implements ICConsole {
	IProject project;
	IBuildConsoleManager fConsoleManager;

	/**
	 * Constructor for BuildConsole.
	 */
	public CBuildConsole() {
	}

	@Override
	public void init(String contextId, String name, URL iconUrl) {
		if (contextId==null)
			fConsoleManager = CUIPlugin.getDefault().getConsoleManager();
		else
			fConsoleManager = CUIPlugin.getDefault().getConsoleManager(name, contextId, iconUrl); // careful with order of arguments
	}

	/**
	 * Start the console for a given project.
	 *
	 * @param project - the project to start the console.
	 */
	@Override
	public void start(IProject project) {
		this.project = project;
		fConsoleManager.getConsole(project).start(project);
	}

	/**
	 * @throws CoreException
	 * @see org.eclipse.cdt.core.resources.IConsole#getOutputStream()
	 */
	@Override
	public ConsoleOutputStream getOutputStream() throws CoreException {
		Assert.isNotNull(project, ConsoleMessages.CBuildConsole_Console_Must_Be_Started_First);
		return fConsoleManager.getConsole(project).getOutputStream();
	}

	@Override
	public ConsoleOutputStream getInfoStream() throws CoreException {
		Assert.isNotNull(project, ConsoleMessages.CBuildConsole_Console_Must_Be_Started_First);
		return fConsoleManager.getConsole(project).getInfoStream();
	}

	@Override
	public ConsoleOutputStream getErrorStream() throws CoreException {
		Assert.isNotNull(project, ConsoleMessages.CBuildConsole_Console_Must_Be_Started_First);
		return fConsoleManager.getConsole(project).getErrorStream();
	}
}
