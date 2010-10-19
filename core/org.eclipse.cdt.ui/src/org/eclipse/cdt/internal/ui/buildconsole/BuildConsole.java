/*******************************************************************************
 * Copyright (c) 2002, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * Red Hat Inc. - Multiple build console support
 * Dmitry Kozlov (CodeSourcery) - Build error highlighting and navigation
 * Alex Collins (Broadcom Corp.) - Global build console
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.buildconsole;

import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.IBuildConsoleManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.console.AbstractConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.part.IPageBookViewPage;

public class BuildConsole extends AbstractConsole {

	/**
	 * Menu group identifier for the console view context menu and toolbar, for actions pertaining to
	 * error navigation (value <code>"errorGroup"</code>).
	 */
	public static final String ERROR_GROUP = "errorGroup"; //$NON-NLS-1$
		
	/**
	 * Property constant indicating the color of a stream has changed. 
	 */
	public static final String P_STREAM_COLOR = CUIPlugin.PLUGIN_ID  + ".CONSOLE_P_STREAM_COLOR";	 //$NON-NLS-1$

	/** The page containing this build console */
	private BuildConsolePage fBuildConsolePage;
	/** The page for the console currently being displayed by the UI */
	private static BuildConsolePage fCurrentBuildConsolePage;

	private IBuildConsoleManager fConsoleManager;
	private String fConsoleName;
	private String fConsoleId;
	private Color fBackground;

	public BuildConsole(IBuildConsoleManager manager, String name, String id) {
		super(name, CPluginImages.DESC_BUILD_CONSOLE);
		fConsoleManager = manager;
		fConsoleName = name;
		fConsoleId = id;
	}

	public IPageBookViewPage createPage(IConsoleView view) {
		fBuildConsolePage = new BuildConsolePage(view, this, fConsoleId); 
		fCurrentBuildConsolePage = fBuildConsolePage;
		return fBuildConsolePage;
	}
	
	BuildConsolePage getPage() {
		return fBuildConsolePage;
	}

	static BuildConsolePage getCurrentPage() {
		return fCurrentBuildConsolePage;
	}

	static void setCurrentPage(BuildConsolePage page) {
		fCurrentBuildConsolePage = page;
	}

	public void setTitle(IProject project) {
		String title = fConsoleName;
		if (project != null) {
			title += " [" + project.getName() + "]"; //$NON-NLS-1$ //$NON-NLS-2$
		}
		setName(title);
	}

	public IBuildConsoleManager getConsoleManager() {
		return fConsoleManager;
	}

	public void setBackground(Color background) {
		if (fBackground == null) {
			if (background == null) {
				return;
			}
		} else if (fBackground.equals(background)){
			return;
		}
		Color old = fBackground;
		fBackground = background;
		firePropertyChange(this, IConsoleConstants.P_BACKGROUND_COLOR, old, fBackground);
	}

	public Color getBackground() {
		return fBackground;
	}
}
