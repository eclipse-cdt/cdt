/*******************************************************************************
 * Copyright (c) 2010 CodeSourcery and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Dmitry Kozlov (CodeSourcery) - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.buildconsole;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.PreferencesUtil;

import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.IBuildConsoleManager;

import org.eclipse.cdt.internal.core.BuildOutputLogger;

import org.eclipse.cdt.internal.ui.CPluginImages;

/**
 * Save console content to a file
 */
public class SaveConsoleAction extends Action {

	private BuildConsolePage fConsolePage;

	public SaveConsoleAction(BuildConsolePage page) {
		super();
		setToolTipText(ConsoleMessages.SaveConsole_ActionTooltip);
		setChecked(false);
		CPluginImages.setImageDescriptors(this, CPluginImages.T_LCL, CPluginImages.IMG_SAVE_CONSOLE);
		fConsolePage = page;
	}

	@Override
	public void run() {
		IProject project = fConsolePage.getProject();
		if ( ! project.isAccessible() ) return;
		IBuildConsoleManager consoleManager = CUIPlugin.getDefault().getConsoleManager();
		IConsole console = consoleManager.getConsole(project);
		// If state is switched to save build log, but log file is not set
		// in project properties, show project properties dialog.
		if ( console != null && console instanceof BuildConsolePartitioner ) {
			BuildOutputLogger.SaveBuildOutputPreferences bp =
					BuildOutputLogger.readSaveBuildOutputPreferences(project);
			if ( isChecked() ) {
				if ( bp.fileName == null || bp.fileName.trim().length() == 0 ) {
					Shell shell = fConsolePage.getControl().getShell();
					String id = "org.eclipse.cdt.managedbuilder.ui.properties.Page_head_build"; //$NON-NLS-1$
					PreferenceDialog d = PreferencesUtil.createPropertyDialogOn(shell, project, id, new String[] { id }, null);
					d.open();
					BuildOutputLogger.SaveBuildOutputPreferences newBp =
						BuildOutputLogger.readSaveBuildOutputPreferences(project);
					setChecked(newBp.isSaving);
					return;
				}
			}
			bp.isSaving = isChecked();
			BuildOutputLogger.writeSaveBuildOutputPreferences(project,bp);
		}
	}

}
