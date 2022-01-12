/*******************************************************************************
 * Copyright (c) 2014 Google, Inc and others.
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
package org.eclipse.cdt.internal.ui.dialogs;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * Asks user how formatting of an empty selection should be interpreted.
 */
public class FormattingScopeDialog extends StatusDialog {
	private Button fDoNotAskAgainCheckBox;
	private Button fScopeFileRadio;
	private Button fScopeStatementRadio;
	private IPreferenceStore preferenceStore;

	public FormattingScopeDialog(Shell shell) {
		super(shell);
		setTitle(Messages.FormattingScopeDialog_title);
		setHelpAvailable(false);
		preferenceStore = CUIPlugin.getDefault().getPreferenceStore();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);

		PixelConverter converter = new PixelConverter(composite);

		createLabel(composite, Messages.FormattingScopeDialog_message);
		int indent = converter.convertHorizontalDLUsToPixels(8);
		fScopeFileRadio = createRadioButton(composite, Messages.FormattingScopeDialog_format_file, indent);
		fScopeStatementRadio = createRadioButton(composite, Messages.FormattingScopeDialog_format_statement, indent);
		createLabel(composite, ""); // Separator //$NON-NLS-1$
		fDoNotAskAgainCheckBox = createCheckBox(composite, Messages.FormattingScopeDialog_do_not_ask_again);

		String scope = preferenceStore.getString(PreferenceConstants.FORMATTING_SCOPE_FOR_EMPTY_SELECTION);
		if (PreferenceConstants.FORMATTING_SCOPE_DOCUMENT.equals(scope)) {
			fScopeFileRadio.setSelection(true);
		} else {
			fScopeStatementRadio.setSelection(true);
		}
		applyDialogFont(composite);
		return composite;
	}

	@Override
	protected void okPressed() {
		String scope = fScopeFileRadio.getSelection() ? PreferenceConstants.FORMATTING_SCOPE_DOCUMENT
				: PreferenceConstants.FORMATTING_SCOPE_STATEMENT;
		preferenceStore.setValue(PreferenceConstants.FORMATTING_SCOPE_FOR_EMPTY_SELECTION, scope);
		if (fDoNotAskAgainCheckBox.getSelection()) {
			preferenceStore.setValue(PreferenceConstants.FORMATTING_CONFIRM_SCOPE_FOR_EMPTY_SELECTION, false);
		}
		super.okPressed();
	}

	private Button createRadioButton(Composite container, String text, int indent) {
		return createButton(SWT.RADIO, container, text, indent);
	}

	private Button createCheckBox(Composite container, String text) {
		return createButton(SWT.CHECK, container, text, 0);
	}

	private Button createButton(int style, Composite container, String text, int indent) {
		Button button = new Button(container, style);
		GridData layoutData = new GridData();
		layoutData.horizontalIndent = indent;
		button.setLayoutData(layoutData);
		button.setText(text);
		return button;
	}

	private Label createLabel(Composite container, String text) {
		Label label = new Label(container, SWT.NONE);
		label.setLayoutData(new GridData());
		label.setText(text);
		return label;
	}
}
