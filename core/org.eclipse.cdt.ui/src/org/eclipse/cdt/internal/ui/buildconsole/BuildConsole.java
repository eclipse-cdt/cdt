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

import java.net.URL;

import org.eclipse.core.resources.IProject;
import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.console.AbstractConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.part.IPageBookViewPage;

import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.IBuildConsoleManager;

/**
 * CDT Build console.
 *
 */
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

	/**
	 * Constructor.
	 *
	 * @param manager - build console manager.
	 * @param name - name of console to appear in the list of consoles in context menu
	 *    in the Console view.
	 * @param contextId - context menu id in the Console view.
	 */
	public BuildConsole(IBuildConsoleManager manager, String name, String contextId) {
		this(manager, name, contextId, null);
	}

	/**
	 * Constructor.
	 *
	 * @param manager - build console manager.
	 * @param name - name of console to appear in the list of consoles in context menu
	 *    in the Console view.
	 * @param contextId - context menu id in the Console view.
	 * @param iconUrl - a {@link URL} of the icon for the context menu of the Console
	 *    view. The url is expected to point to an image in eclipse OSGi bundle.
	 *    {@code iconUrl} can be <b>null</b>, in that case the default image is used.
	 */
	public BuildConsole(IBuildConsoleManager manager, String name, String contextId, URL iconUrl) {
		super(name, CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_VIEW_BUILD_CONSOLE));
		if (iconUrl!=null) {
			CDTSharedImages.register(iconUrl);
			this.setImageDescriptor(CDTSharedImages.getImageDescriptor(iconUrl.toString()));
		}
		fConsoleManager = manager;
		fConsoleName = name;
		fConsoleId = contextId;
	}

	@Override
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
