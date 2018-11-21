/*******************************************************************************
 * Copyright (c) 2010, 2011 Andrew Gvozdev and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Gvozdev (Quoin Inc.) - Initial API and implementation
 *     Alex Collins (Broadcom Corp.) - Global console
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.buildconsole;

import java.net.URI;

import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.core.resources.ResourcesUtil;
import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.IBuildConsoleManager;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * Action to copy build log from working file to a user selected file.
 */
public class CopyBuildLogAction extends Action {
	private BuildConsolePage fConsolePage;

	public CopyBuildLogAction(BuildConsolePage page) {
		super();
		setToolTipText(ConsoleMessages.CopyLog_ActionTooltip);
		CPluginImages.setImageDescriptors(this, CPluginImages.T_LCL, CPluginImages.IMG_SAVE_CONSOLE);
		fConsolePage = page;
	}

	@Override
	public void run() {
		IBuildConsoleManager consoleManager = fConsolePage.getConsole().getConsoleManager();
		IProject project = fConsolePage.getProject();
		IConsole console = consoleManager.getProjectConsole(project);

		Shell shell = Display.getCurrent().getActiveShell();

		if (console instanceof BuildConsolePartitioner) {
			URI srcURI = ((BuildConsolePartitioner) console).getLogURI();
			if (srcURI == null) {
				MessageDialog.openWarning(shell, ConsoleMessages.CopyLog_UnavailableLog,
						ConsoleMessages.CopyLog_BuildNotLogged);
				return;
			}

			IFileStore srcStore = null;
			try {
				srcStore = EFS.getStore(srcURI);
			} catch (CoreException e) {
				CUIPlugin.log(e);
				MessageDialog.openError(shell, ConsoleMessages.CopyLog_UnavailableLog,
						ConsoleMessages.CopyLog_UnableToAccess + srcURI);
				return;
			}

			if (!srcStore.fetchInfo().exists()) {
				MessageDialog.openError(shell, ConsoleMessages.CopyLog_UnavailableLog,
						ConsoleMessages.CopyLog_LogFileIsNotAvailable);
				return;
			}

			// open file dialog
			FileDialog dialog = new FileDialog(shell, SWT.NONE);
			dialog.setText(ConsoleMessages.CopyLog_ChooseDestination);

			String destLocation = dialog.open();
			if (destLocation != null) {
				URI destURI = URIUtil.toURI(destLocation);
				if (destURI == null) {
					MessageDialog.openError(shell, ConsoleMessages.CopyLog_UnavailableLog,
							ConsoleMessages.CopyLog_InvalidDestination + destLocation);
					return;
				}
				try {
					IFileStore destStore = EFS.getStore(destURI);
					srcStore.copy(destStore, EFS.OVERWRITE, null);
				} catch (CoreException e) {
					CUIPlugin.log(e);
					MessageDialog.openError(shell, ConsoleMessages.CopyLog_ErrorCopyingFile,
							ConsoleMessages.CopyLog_ErrorWhileCopyingLog + e.getLocalizedMessage());
				} finally {
					ResourcesUtil.refreshWorkspaceFiles(destURI);
				}
			}
		} else {
			MessageDialog.openWarning(shell, ConsoleMessages.CopyLog_UnavailableLog,
					ConsoleMessages.CopyLog_BuildNotLogged);
		}
	}

}
