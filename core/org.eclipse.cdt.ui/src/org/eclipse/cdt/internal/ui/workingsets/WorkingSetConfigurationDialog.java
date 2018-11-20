/*******************************************************************************
 * Copyright (c) 2009 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.workingsets;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * A dialog for management of working set configurations. These collect the selection of project
 * configurations for the member projects of the working sets into named presets.
 *
 * @author Christian W. Damus (cdamus)
 *
 * @since 6.0
 */
public class WorkingSetConfigurationDialog extends TrayDialog {

	private WorkingSetConfigurationBlock block;

	/**
	 * Initializes me with my shell.
	 *
	 * @param shell
	 */
	public WorkingSetConfigurationDialog(Shell shell) {
		super(shell);
	}

	/**
	 * Initializes me with my shell provider.
	 *
	 * @param parentShell
	 */
	public WorkingSetConfigurationDialog(IShellProvider parentShell) {
		super(parentShell);
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	protected void configureShell(Shell newShell) {
		setHelpAvailable(false);

		super.configureShell(newShell);

		newShell.setText(WorkingSetMessages.WSConfigDialog_title);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite result = (Composite) super.createDialogArea(parent);

		block = new WorkingSetConfigurationBlock(WorkingSetConfigurationManager.getDefault().createWorkspaceSnapshot());
		Control contents = block.createContents(result);
		contents.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		return result;
	}

	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			if (!block.build()) {
				// user cancelled: don't save, and don't close the dialog
				return;
			}

			block.save();
		}

		super.buttonPressed(buttonId);
	}
}
