/*
 * (c) Copyright QNX Software System Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.internal.ui;

import org.eclipse.cdt.core.ConsoleOutputStream;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.core.resources.MakeUtil;
import org.eclipse.cdt.internal.ui.preferences.CPluginPreferencePage;
import org.eclipse.core.resources.IProject;

public class CConsole implements IConsole {

	/**
	 * Constructor for CConsole.
	 */
	public CConsole() {
		super();
	}

	public void start(IProject project ) {
		if (CPluginPreferencePage.isClearBuildConsole()
			&& MakeUtil.getSessionConsoleMode(project)) {
			clear();
		}
	}
	
	/**
	 * @see org.eclipse.cdt.core.resources.IConsole#clear()
	 */
	public void clear() {
		CPlugin.getDefault().getConsole().clear();
	}

	/**
	 * @see org.eclipse.cdt.core.resources.IConsole#getOutputStream()
	 */
	public ConsoleOutputStream getOutputStream() {
		return CPlugin.getDefault().getConsole().getOutputStream();
	}
}
