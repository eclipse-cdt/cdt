/*
 * (c) Copyright QNX Software System Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.internal.ui;

import org.eclipse.cdt.core.ConsoleOutputStream;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.ui.*;
import org.eclipse.core.resources.IProject;

public class BuildConsole implements IConsole {
	IProject project;
	IBuildConsoleManager fConsoleManager;
	
	/**
	 * Constructor for BuildConsole.
	 */
	public BuildConsole() {
		fConsoleManager = CUIPlugin.getDefault().getConsoleManager();
	}

	public void start(IProject project ) {
		this.project = project;
		fConsoleManager.getConsole(project).start(project);
	}
	
	/**
	 * @see org.eclipse.cdt.core.resources.IConsole#clear()
	 */
	public void clear() {
		fConsoleManager.getConsole(project).clear();
	}

	/**
	 * @see org.eclipse.cdt.core.resources.IConsole#getOutputStream()
	 */
	public ConsoleOutputStream getOutputStream() {
		return fConsoleManager.getConsole(project).getOutputStream();
	}
}
