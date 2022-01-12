/*******************************************************************************
 * Copyright (c) 2013 Mentor Graphics and others.
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
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.internal.ui.launching;

import java.io.File;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.internal.ui.commands.Messages;
import org.eclipse.cdt.dsf.gdb.launching.LaunchMessages;
import org.eclipse.cdt.dsf.gdb.service.SessionType;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
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

public class NewExecutableDialog extends TitleAreaDialog {

	private NewExecutableInfo fInfo = null;

	private Text fHostBinaryText;
	private Text fTargetBinaryText;
	private Text fArgumentsText;

	private Button fStopInMain;
	private Text fStopInMainSymbol;

	public NewExecutableDialog(Shell parentShell, NewExecutableInfo info) {
		super(parentShell);
		assert info != null;
		setShellStyle(getShellStyle() | SWT.RESIZE);
		fInfo = info;
	}

	@Override
	protected Control createContents(Composite parent) {
		Control control = super.createContents(parent);
		initialize();
		validate();
		return control;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		boolean remote = fInfo.getSessionType() == SessionType.REMOTE;

		getShell().setText(Messages.GdbDebugNewExecutableCommand_Debug_New_Executable);
		setTitle(Messages.GdbDebugNewExecutableCommand_Select_Binary);
		String message = (remote) ? Messages.GdbDebugNewExecutableCommand_Select_binaries_on_host_and_target
				: Messages.GdbDebugNewExecutableCommand_Select_binary_and_specify_arguments;
		setMessage(message);

		Composite control = (Composite) super.createDialogArea(parent);
		Composite comp = new Composite(control, SWT.NONE);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		GridLayout layout = new GridLayout(3, false);
		comp.setLayout(layout);
		comp.setLayoutData(gd);

		new Label(comp, SWT.None).setText(remote ? Messages.GdbDebugNewExecutableCommand_Binary_on_host
				: Messages.GdbDebugNewExecutableCommand_Binary);
		fHostBinaryText = new Text(comp, SWT.BORDER);
		fHostBinaryText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		fHostBinaryText.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				validate();
			}
		});
		Button browseButton = new Button(comp, SWT.PUSH);
		browseButton.setText(Messages.GdbDebugNewExecutableCommand_Browse);
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

		new Label(comp, SWT.None).setText(Messages.GdbDebugNewExecutableCommand_Arguments);
		fArgumentsText = new Text(comp, SWT.BORDER);
		fArgumentsText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

		createOptionsArea(comp);

		return control;
	}

	protected void createOptionsArea(Composite parent) {
		Composite optionsComp = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(3, false);
		layout.marginWidth = layout.marginHeight = 0;
		optionsComp.setLayout(layout);
		optionsComp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));

		fStopInMain = new Button(optionsComp, SWT.CHECK);
		fStopInMain.setText(LaunchMessages.getString("CDebuggerTab.Stop_at_main_on_startup")); //$NON-NLS-1$
		fStopInMain.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				fStopInMainSymbol.setEnabled(fStopInMain.getSelection());
				validate();
			}
		});
		fStopInMainSymbol = new Text(optionsComp, SWT.SINGLE | SWT.BORDER);
		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, false, false);
		gridData.widthHint = 100;
		fStopInMainSymbol.setLayoutData(gridData);
		fStopInMainSymbol.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent evt) {
				validate();
			}
		});
		fStopInMainSymbol.getAccessible().addAccessibleListener(new AccessibleAdapter() {
			@Override
			public void getName(AccessibleEvent e) {
				e.result = LaunchMessages.getString("CDebuggerTab.Stop_at_main_on_startup"); //$NON-NLS-1$
			}
		});

	}

	@Override
	protected void okPressed() {
		fInfo.setHostPath(fHostBinaryText.getText().trim());
		String targetPath = fTargetBinaryText != null ? fTargetBinaryText.getText().trim() : null;
		fInfo.setAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_REMOTE_BINARY, targetPath);
		String args = fArgumentsText.getText().trim();
		fInfo.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, args);
		saveOptions();
		super.okPressed();
	}

	protected void initialize() {
		fHostBinaryText.setText(fInfo.getHostPath());
		if (fTargetBinaryText != null) {
			String targetPath = fInfo.getTargetPath();
			fTargetBinaryText.setText(targetPath != null ? targetPath : ""); //$NON-NLS-1$
		}
		fArgumentsText.setText(fInfo.getArguments());
		if (fStopInMain != null && fStopInMainSymbol != null) {
			fStopInMain.setSelection(
					(Boolean) fInfo.getAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN));
			fStopInMainSymbol.setText(
					(String) fInfo.getAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN_SYMBOL));
		}
	}

	protected void validate() {
		boolean remote = fInfo.getSessionType() == SessionType.REMOTE;
		StringBuilder sb = new StringBuilder();
		String hostBinary = fHostBinaryText.getText().trim();
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
		if (fTargetBinaryText != null) {
			if (fTargetBinaryText.getText().trim().length() == 0) {
				if (sb.length() != 0) {
					sb.append("\n "); //$NON-NLS-1$
				}
				sb.append(Messages.GdbDebugNewExecutableCommand_Binary_on_target_must_be_specified);
			}
		}
		if (fStopInMain != null && fStopInMainSymbol != null) {
			// The "Stop on startup at" field must not be empty
			String mainSymbol = fStopInMainSymbol.getText().trim();
			if (fStopInMain.getSelection() && mainSymbol.length() == 0) {
				if (sb.length() > 0) {
					sb.append("\n "); //$NON-NLS-1$
				}
				sb.append(LaunchMessages.getString("CDebuggerTab.Stop_on_startup_at_can_not_be_empty")); //$NON-NLS-1$
			}
		}

		setErrorMessage((sb.length() != 0) ? sb.toString() : null);
		getButton(IDialogConstants.OK_ID).setEnabled(getErrorMessage() == null);
	}

	protected void saveOptions() {
		if (fStopInMain != null && fStopInMainSymbol != null) {
			fInfo.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN,
					Boolean.valueOf(fStopInMain.getSelection()));
			fInfo.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN_SYMBOL,
					fStopInMainSymbol.getText().trim());
		}
	}
}