/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.rse.internal.importexport.files;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.wizard.WizardDialog;

/**
 * This class represents the action to bring up the remote file system export wizard
 * and export the contents of a project or projects to a remote folder.
 */
public class RemoteFileExportFromProjectActionDelegate extends RemoteFileImportExportActionDelegate {
	/**
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		RemoteExportWizard wizard = new RemoteExportWizard();
		wizard.init(getWorkbench(), getSelection());
		WizardDialog dialog = new WizardDialog(getShell(), wizard);
		dialog.create();
		dialog.open();
	}
}
