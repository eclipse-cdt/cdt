/*******************************************************************************
 * Copyright (c) 2013 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.preferences;

import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.internal.ui.dialogs.ResizableStatusDialog;
import org.eclipse.cdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.SelectionButtonDialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.StringDialogField;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

public class HeaderSubstitutionEditDialog extends ResizableStatusDialog {
	private final StringDialogField fSourceField;
	private final StringDialogField fTargetField;
	private final SelectionButtonDialogField fRequiredSubstitutionCheckBox;

	public HeaderSubstitutionEditDialog(Shell shell, HeaderSubstitutionRule rule) {
		super(shell);
		if (rule == null || rule.getSource().isEmpty()) {
			setTitle(PreferencesMessages.HeaderSubstitutionEditDialog_new_title);
		} else {
			setTitle(PreferencesMessages.HeaderSubstitutionEditDialog_edit_title);
		}

		IDialogFieldListener listener = new IDialogFieldListener() {
			@Override
			public void dialogFieldChanged(DialogField field) {
				validate();
			}
		};
		fSourceField = new StringDialogField();
		fSourceField.setLabelText(PreferencesMessages.HeaderSubstitutionEditDialog_source);
		fSourceField.setDialogFieldListener(listener);
		fTargetField = new StringDialogField();
		fTargetField.setLabelText(PreferencesMessages.HeaderSubstitutionEditDialog_target);
		fTargetField.setDialogFieldListener(listener);
		fRequiredSubstitutionCheckBox = new SelectionButtonDialogField(SWT.CHECK);
		fRequiredSubstitutionCheckBox
				.setLabelText(PreferencesMessages.HeaderSubstitutionEditDialog_required_substitution);

		if (rule != null) {
			fSourceField.setText(rule.getSource());
			fTargetField.setText(rule.getTarget());
			fRequiredSubstitutionCheckBox.setSelection(rule.isUnconditionalSubstitution());
		}

		validate();
	}

	public HeaderSubstitutionRule getResult() {
		return new HeaderSubstitutionRule(fSourceField.getText().trim(), fTargetField.getText().trim(),
				fRequiredSubstitutionCheckBox.isSelected());
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(newShell, ICHelpContextIds.HEADER_SUBSTITUTION_EDIT_DIALOG);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);

		PixelConverter conv = new PixelConverter(composite);

		Composite inner = new Composite(composite, SWT.NONE);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		int len = PreferencesMessages.HeaderSubstitutionEditDialog_enter_target.length() + 5;
		gridData.widthHint = conv.convertWidthInCharsToPixels(len);
		inner.setLayoutData(gridData);
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		inner.setLayout(layout);

		fSourceField.doFillIntoGrid(inner, 2);
		fTargetField.doFillIntoGrid(inner, 2);
		fRequiredSubstitutionCheckBox.doFillIntoGrid(composite, 2);

		applyDialogFont(composite);

		validate();

		return composite;
	}

	private void validate() {
		IStatus status = StatusInfo.OK_STATUS;
		String source = fSourceField.getText().trim();
		String target = fTargetField.getText().trim();
		if (source.isEmpty()) {
			status = new StatusInfo(IStatus.INFO, PreferencesMessages.HeaderSubstitutionEditDialog_enter_source);
		} else if (!isValidHeader(source)) {
			status = new StatusInfo(IStatus.WARNING, PreferencesMessages.HeaderSubstitutionEditDialog_invalid_source);
		} else if (target.isEmpty()) {
			status = new StatusInfo(IStatus.INFO, PreferencesMessages.HeaderSubstitutionEditDialog_enter_target);
		} else if (!isValidHeader(target)) {
			status = new StatusInfo(IStatus.WARNING, PreferencesMessages.HeaderSubstitutionEditDialog_invalid_target);
		} else if (target.equals(source)) {
			status = new StatusInfo(IStatus.WARNING,
					PreferencesMessages.HeaderSubstitutionEditDialog_error_replacement_by_itself);
		}
		updateStatus(status);
	}

	@Override
	protected void updateButtonsEnableState(IStatus status) {
		// OK button is disabled unless the status is OK.
		super.updateButtonsEnableState(status.isOK() ? status : new StatusInfo(IStatus.ERROR, null));
	}

	private boolean isValidHeader(String header) {
		if (header.isEmpty())
			return false;
		if (header.startsWith("<") != header.endsWith(">")) //$NON-NLS-1$//$NON-NLS-2$
			return false;
		if (header.startsWith("<") && header.length() < 3) //$NON-NLS-1$
			return false;
		return true;
	}
}
