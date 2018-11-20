/*******************************************************************************
 * Copyright (c) 2017 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.build.gcc.ui.internal;

import org.eclipse.cdt.core.envvar.EnvironmentVariable;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class NewEnvVarDialog extends Dialog {

	private Text nameText;
	private Label valueLabel;
	private Text valueText;
	private Label delimiterLabel;
	private Text delimiterText;
	private Button replaceButton;
	private Button prependButton;
	private Button appendButton;
	private Button removeButton;

	private IEnvironmentVariable envvar;

	public NewEnvVarDialog(Shell parentShell) {
		super(parentShell);
	}

	public NewEnvVarDialog(Shell parentShell, IEnvironmentVariable var) {
		super(parentShell);
		this.envvar = var;
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		if (envvar == null) {
			getShell().setText(Messages.NewEnvVarDialog_New);
		} else {
			getShell().setText(Messages.NewEnvVarDialog_Edit);
		}

		Composite comp = new Composite(parent, SWT.NONE);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		layoutData.widthHint = 400;
		comp.setLayoutData(layoutData);
		comp.setLayout(new GridLayout(3, false));

		Group opGroup = new Group(comp, SWT.NONE);
		opGroup.setText(Messages.NewEnvVarDialog_Operation);
		opGroup.setLayout(new GridLayout(4, false));
		layoutData = new GridData(SWT.FILL, SWT.FILL, true, false);
		layoutData.horizontalSpan = 3;
		opGroup.setLayoutData(layoutData);

		replaceButton = new Button(opGroup, SWT.RADIO);
		replaceButton.setText(Messages.NewEnvVarDialog_Replace);

		prependButton = new Button(opGroup, SWT.RADIO);
		prependButton.setText(Messages.NewEnvVarDialog_Prepend);
		prependButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateEnablement();
			}
		});

		appendButton = new Button(opGroup, SWT.RADIO);
		appendButton.setText(Messages.NewEnvVarDialog_Append);
		appendButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateEnablement();
			}
		});

		removeButton = new Button(opGroup, SWT.RADIO);
		removeButton.setText(Messages.NewEnvVarDialog_Unset);
		removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateEnablement();
			}
		});

		if (envvar == null) {
			replaceButton.setSelection(true);
		} else {
			switch (envvar.getOperation()) {
			case IEnvironmentVariable.ENVVAR_REPLACE:
				replaceButton.setSelection(true);
				break;
			case IEnvironmentVariable.ENVVAR_PREPEND:
				prependButton.setSelection(true);
				break;
			case IEnvironmentVariable.ENVVAR_APPEND:
				appendButton.setSelection(true);
				break;
			default:
				removeButton.setSelection(true);
			}
		}

		Label label = new Label(comp, SWT.NONE);
		label.setText(Messages.NewEnvVarDialog_Name);

		nameText = new Text(comp, SWT.BORDER);
		layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		layoutData.horizontalSpan = 2;
		nameText.setLayoutData(layoutData);
		if (envvar != null) {
			nameText.setText(envvar.getName());
		}

		// TODO
		// Button selectButton = new Button(comp, SWT.PUSH);
		// selectButton.setText(Messages.NewEnvVarDialog_Select);

		valueLabel = new Label(comp, SWT.NONE);
		valueLabel.setText(Messages.NewEnvVarDialog_Value);

		valueText = new Text(comp, SWT.BORDER);
		layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		layoutData.horizontalSpan = 2;
		valueText.setLayoutData(layoutData);
		if (envvar != null && envvar.getValue() != null) {
			valueText.setText(envvar.getValue());
		}

		delimiterLabel = new Label(comp, SWT.NONE);
		delimiterLabel.setText(Messages.NewEnvVarDialog_Delimiter);

		delimiterText = new Text(comp, SWT.BORDER);
		layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		layoutData.horizontalSpan = 2;
		delimiterText.setLayoutData(layoutData);
		if (envvar != null && envvar.getDelimiter() != null) {
			delimiterText.setText(envvar.getDelimiter());
		}

		updateEnablement();
		return comp;
	}

	private void updateEnablement() {
		valueLabel.setEnabled(!removeButton.getSelection());
		valueText.setEnabled(!removeButton.getSelection());

		delimiterLabel.setEnabled(prependButton.getSelection() || appendButton.getSelection());
		delimiterText.setEnabled(prependButton.getSelection() || appendButton.getSelection());
	}

	@Override
	protected void okPressed() {
		String name = nameText.getText().trim();

		int operation;
		if (replaceButton.getSelection()) {
			operation = IEnvironmentVariable.ENVVAR_REPLACE;
		} else if (prependButton.getSelection()) {
			operation = IEnvironmentVariable.ENVVAR_PREPEND;
		} else if (appendButton.getSelection()) {
			operation = IEnvironmentVariable.ENVVAR_APPEND;
		} else {
			operation = IEnvironmentVariable.ENVVAR_REMOVE;
		}

		String value;
		if (valueText.isEnabled()) {
			value = valueText.getText().trim();
			if (value.isEmpty()) {
				value = null;
			}
		} else {
			value = null;
		}

		String delimiter;
		if (delimiterText.isEnabled()) {
			delimiter = delimiterText.getText().trim();
			if (delimiter.isEmpty()) {
				delimiter = null;
			}
		} else {
			delimiter = null;
		}

		envvar = new EnvironmentVariable(name, value, operation, delimiter);

		super.okPressed();
	}

	public IEnvironmentVariable getEnvVar() {
		return envvar;
	}
}
