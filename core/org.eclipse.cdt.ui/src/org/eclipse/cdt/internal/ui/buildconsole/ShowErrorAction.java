/*******************************************************************************
 * Copyright (c) 2010, 2011 CodeSourcery and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Dmitry Kozlov (CodeSourcery) - Initial API and implementation
 * Alex Collins (Broadcom Corp.)
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.buildconsole;

import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.ui.IBuildConsoleManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * Set whether to show error in editor when moving to next/prev error in Build Console
 */
public class ShowErrorAction extends Action {

	private BuildConsolePage fConsolePage;

	public ShowErrorAction(BuildConsolePage page) {
		super(ConsoleMessages.ShowErrorAction_Tooltip);
		fConsolePage = page;
		setChecked(true);
		setToolTipText(ConsoleMessages.ShowErrorAction_Tooltip);
		ISharedImages images = PlatformUI.getWorkbench().getSharedImages();
		setImageDescriptor(images.getImageDescriptor(ISharedImages.IMG_ELCL_SYNCED));
		setDisabledImageDescriptor(images.getImageDescriptor(ISharedImages.IMG_ELCL_SYNCED_DISABLED));
	}

	/**
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	@Override
	public void run() {
		super.run();
		if (isChecked()) {
			IBuildConsoleManager consoleManager = fConsolePage.getConsole().getConsoleManager();
			IProject project = fConsolePage.getProject();
			IConsole console = consoleManager.getProjectConsole(project);
			if (console instanceof BuildConsolePartitioner) {
				BuildConsolePartitioner par = (BuildConsolePartitioner) console;
				fConsolePage.showError(par, true);
			}
		}
	}

}
