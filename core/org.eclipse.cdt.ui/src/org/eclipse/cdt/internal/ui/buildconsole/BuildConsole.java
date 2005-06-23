/*******************************************************************************
 * Copyright (c) 2002, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.buildconsole;

import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.IBuildConsoleManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.ui.console.AbstractConsole;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.part.IPageBookViewPage;

public class BuildConsole extends AbstractConsole {
	
	/**
	 * Property constant indicating the color of a stream has changed. 
	 */
	public static final String P_STREAM_COLOR = CUIPlugin.PLUGIN_ID  + ".CONSOLE_P_STREAM_COLOR";	 //$NON-NLS-1$

	private IBuildConsoleManager fConsoleManager;

	public BuildConsole(IBuildConsoleManager manager) {
		super(ConsoleMessages.getString("BuildConsole.buildConsole"), CPluginImages.DESC_BUILD_CONSOLE); //$NON-NLS-1$
		fConsoleManager = manager;
	}

	public IPageBookViewPage createPage(IConsoleView view) {
		return new BuildConsolePage(view, this);
	}

	public void setTitle(IProject project) {
		String title = ConsoleMessages.getString("BuildConsole.buildConsole"); //$NON-NLS-1$
		if (project != null) {
			title += " [" + project.getName() + "]"; //$NON-NLS-1$ //$NON-NLS-2$
		}
		setName(title);
	}

	public IBuildConsoleManager getConsoleManager() {
	    return fConsoleManager;
	}
}
