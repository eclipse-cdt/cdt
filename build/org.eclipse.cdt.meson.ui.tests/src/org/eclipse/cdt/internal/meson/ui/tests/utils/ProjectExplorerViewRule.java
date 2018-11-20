/*******************************************************************************
 * Copyright (c) 2016 Red Hat.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
