/*******************************************************************************
 * Copyright (c) 2016 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.eclipse.cdt.internal.meson.ui.tests.utils;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.junit.Rule;
import org.junit.rules.ExternalResource;

/**
 * A JUnit {@link Rule} to open the Project Explorer view.
 */
public class ProjectExplorerViewRule extends ExternalResource {

	private SWTBotView projectExplorerBotView;

	public static final String PROJECT_EXPLORER_VIEW_ID = "org.eclipse.ui.navigator.ProjectExplorer";

	@Override
	protected void before() {
		final SWTWorkbenchBot bot = new SWTWorkbenchBot();
		SWTUtils.syncExec(() -> {
			try {
				return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
						.showView(PROJECT_EXPLORER_VIEW_ID);
			} catch (PartInitException e) {
				e.printStackTrace();
				return null;
			}
		});
		this.projectExplorerBotView = bot.viewById(PROJECT_EXPLORER_VIEW_ID);
		this.projectExplorerBotView.setFocus();
	}

	public SWTBotView getProjectExplorerBotView() {
		return this.projectExplorerBotView;
	}
}
