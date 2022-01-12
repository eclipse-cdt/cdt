/*******************************************************************************
 * Copyright (c) 2013, 2014 Mentor Graphics and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Mentor Graphics - Initial API and implementation
 * Red Hat Inc. - modified for use in Standalone Debugger
 *******************************************************************************/

package org.eclipse.cdt.debug.application;

import java.io.File;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class CoreFileDialog extends TitleAreaDialog {

	public static final int REMOTE = 0x1;

	private int fFlags = 0;
	private CoreFileInfo fInfo = null;

	private Text fHostBinaryText;
	private Text fTargetBinaryText;
	private Text fCoreFileText;

	private final String fHostBinary;
	private final String fCoreFile;

	public CoreFileDialog(Shell parentShell) {
		this(parentShell, 0);
	}

	public CoreFileDialog(Shell parentShell, int flags) {
		this(parentShell, flags, null, null);
	}

	public CoreFileDialog(Shell parentShell, int flags, String hostBinary, String coreFile) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		fFlags = flags;
		fHostBinary = hostBinary;
		fCoreFile = coreFile;
	}

	@Override
	protected Control createContents(Composite parent) {
		Control control = super.createContents(parent);
		validate();
		return control;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		boolean remote = (fFlags & REMOTE) > 0;

		getShell().setText(Messages.GdbDebugCoreFileCommand_Debug_Core_File);
		setTitle(Messages.GdbDebugNewExecutableCommand_Select_Binary);
		String message = (remote) ? Messages.GdbDebugNewExecutableCommand_Select_binaries_on_host_and_target
				: Messages.GdbDebugCoreFileCommand_Select_binary_and_specify_corefile;
		setMessage(message);

		Composite control = (Composite) super.createDialogArea(parent);
		Composite comp = new Composite(control, SWT.NONE);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		GridLayout layout = new GridLayout(3, false);
		comp.setLayout(layout);
		comp.setLayoutData(gd);

		new Label(comp, SWT.None).setText(remote ? Messages.GdbDebugNewExecutableCommand_Binary_on_host
				: Messages.GdbDebugExecutableCommand_Binary);
		fHostBinaryText = new Text(comp, SWT.BORDER);
		if (fHostBinary != null)
			fHostBinaryText.setText(fHostBinary);
		fHostBinaryText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		fHostBinaryText.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				validate();
			}
		});
		Button browseButton = new Button(comp, SWT.PUSH);
		browseButton.setText(Messages.GdbDebugExecutableCommand_Browse);
		browseButton.setFont(JFaceResources.getDialogFont());
		setButtonLayoutData(browseButton);
		browseButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(getShell());
				dialog.setFileName(fHostBinaryText.getText());
				String result = dialog.open();
				if (result != null) {
					fHostBinaryText.setText(result);
				}
			}
		});

		if (remote) {
			new Label(comp, SWT.None).setText(Messages.GdbDebugNewExecutableCommand_Binary_on_target);
			fTargetBinaryText = new Text(comp, SWT.BORDER);
			fTargetBinaryText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			fTargetBinaryText.addModifyListener(new ModifyListener() {

				@Override
				public void modifyText(ModifyEvent e) {
					validate();
				}
			});
		}

		new Label(comp, SWT.None).setText(Messages.GdbDebugCoreFileCommand_CoreFile);
		fCoreFileText = new Text(comp, SWT.BORDER);
		if (fCoreFile != null)
			fCoreFileText.setText(fCoreFile);
		fCoreFileText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		fCoreFileText.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				validate();
			}
		});

		Button browseButton2 = new Button(comp, SWT.PUSH);
		browseButton2.setText(Messages.GdbDebugExecutableCommand_Browse);
		browseButton2.setFont(JFaceResources.getDialogFont());
		setButtonLayoutData(browseButton2);
		browseButton2.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(getShell());
				dialog.setFileName(fCoreFileText.getText());
				String result = dialog.open();
				if (result != null) {
					fCoreFileText.setText(result);
				}
			}
		});

		return control;
	}

	@Override
	protected void okPressed() {
		String targetPath = (fTargetBinaryText != null) ? fTargetBinaryText.getText().trim() : null;
		String coreFile = fCoreFileText.getText().trim();
		fInfo = new CoreFileInfo(fHostBinaryText.getText().trim(), targetPath, coreFile);
		super.okPressed();
	}

	public CoreFileInfo getCoreFileInfo() {
		return fInfo;
	}

	private void validate() {
		boolean remote = (fFlags & REMOTE) > 0;
		StringBuilder sb = new StringBuilder();
		String hostBinary = fHostBinaryText.getText().trim();
		String corefile = fCoreFileText.getText();
		if (hostBinary.isEmpty()) {
			sb.append((remote) ? Messages.GdbDebugNewExecutableCommand_Host_binary_must_be_specified
					: Messages.GdbDebugNewExecutableCommand_Binary_must_be_specified);
		} else {
			File file = new File(hostBinary);
			if (!file.exists()) {
				sb.append((remote) ? Messages.GdbDebugNewExecutableCommand_Host_binary_file_does_not_exist
						: Messages.GdbDebugNewExecutableCommand_Binary_file_does_not_exist);
			} else if (file.isDirectory()) {
				sb.append((remote) ? Messages.GdbDebugNewExecutableCommand_Invalid_host_binary
						: Messages.GdbDebugNewExecutableCommand_Invalid_binary);
			}
		}
		if (sb.length() == 0 && corefile.isEmpty()) {
			sb.append((remote) ? Messages.GdbDebugCoreFileCommand_Core_file_must_be_specified
					: Messages.GdbDebugCoreFileCommand_Core_file_must_be_specified);
		} else if (sb.length() == 0 && !corefile.isEmpty()) {
			File file = new File(corefile);
			if (!file.exists()) {
				sb.append(Messages.GdbDebugCoreFileCommand_Core_file_does_not_exist);
			} else if (file.isDirectory()) {
				sb.append(Messages.GdbDebugCoreFileCommand_Invalid_core_file);
			}
		}
		if (sb.length() == 0 && fTargetBinaryText != null) {
			if (fTargetBinaryText.getText().trim().length() == 0) {
				if (sb.length() != 0) {
					sb.append("\n "); //$NON-NLS-1$
				}
				sb.append(Messages.GdbDebugNewExecutableCommand_Binary_on_target_must_be_specified);
			}
		}
		setErrorMessage((sb.length() != 0) ? sb.toString() : null);
		getButton(IDialogConstants.OK_ID).setEnabled(getErrorMessage() == null);
	}
}