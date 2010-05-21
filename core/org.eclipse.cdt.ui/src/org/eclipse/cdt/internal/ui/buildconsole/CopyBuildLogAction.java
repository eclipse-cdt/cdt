/*******************************************************************************
 * Copyright (c) 2010 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev (Quoin Inc.) - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.buildconsole;

import java.net.URI;

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

import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.IBuildConsoleManager;

import org.eclipse.cdt.internal.ui.CPluginImages;

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
		IProject project = fConsolePage.getProject();
		if (!project.isAccessible())
			return;

		IBuildConsoleManager consoleManager = CUIPlugin.getDefault().getConsoleManager();

		IConsole console = consoleManager.getConsole(project);
		if (console instanceof BuildConsolePartitioner) {
			Shell shell = Display.getCurrent().getActiveShell();
			URI srcURI = ((BuildConsolePartitioner)console).getLogURI();
			if (srcURI==null) {
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
						ConsoleMessages.CopyLog_UnableToAccess+srcURI);
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
			if (destLocation!=null) {
				URI destURI = URIUtil.toURI(destLocation);
				if (destURI==null) {
					MessageDialog.openError(shell, ConsoleMessages.CopyLog_UnavailableLog,
							ConsoleMessages.CopyLog_InvalidDestination+destLocation);
					return;
				}
				try {
					IFileStore destStore = EFS.getStore(destURI);
					srcStore.copy(destStore, EFS.OVERWRITE, null);
				} catch (CoreException e) {
					CUIPlugin.log(e);
					MessageDialog.openError(shell, ConsoleMessages.CopyLog_ErrorCopyingFile,
							ConsoleMessages.CopyLog_ErrorWhileCopyingLog+e.getLocalizedMessage());
				} finally {
					BuildConsoleManager.refreshWorkspaceFiles(destURI);
				}
			}
		}
	}

}
