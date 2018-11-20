/*******************************************************************************
 * Copyright (c) 2006, 2015 Wind River Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.examples.dsf.filebrowser;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.actions.ActionDelegate;

/**
 * Action that opens the File Browser example dialog.
 */
public class FileBrowserAction extends ActionDelegate implements IWorkbenchWindowActionDelegate {
	private IWorkbenchWindow fWindow;

	@Override
	public void run(IAction action) {
		if (fWindow != null) {
			// Create the dialog and open it.
			Dialog dialog = new FileBrowserDialog(fWindow.getShell());
			dialog.open();
		}
	}

	@Override
	public void init(IWorkbenchWindow window) {
		fWindow = window;
	}
}
