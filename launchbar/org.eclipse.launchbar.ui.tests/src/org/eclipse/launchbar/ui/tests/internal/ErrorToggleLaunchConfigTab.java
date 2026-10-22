/*******************************************************************************
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.launchbar.ui.tests.internal;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

/**
 * Creates a tab that always has a warning, and also has an error while its check box is selected. Used to
 * test that the Launch Configuration dialog, when opened via the Launch Bar, enables the OK button again
 * once an error is fixed and only the warning of the visible tab remains.
 * <p>
 * The tab must not contain any {@link org.eclipse.swt.widgets.Text} widgets, as the tests locate the
 * message area as the last Text widget of the dialog.
 * @see {@link CreateLaunchConfigTests}
 */
public class ErrorToggleLaunchConfigTab extends AbstractLaunchConfigurationTab {
	public static final String TAB_NAME = "Error Toggle Tab";
	public static final String CHECKBOX_LABEL = "Simulate an error";
	public static final String ERROR_MESSAGE = "This is an error";
	public static final String WARNING_MESSAGE = "This is a warning from the error toggle tab";

	private Button errorCheckbox;

	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		errorCheckbox = new Button(composite, SWT.CHECK);
		errorCheckbox.setText(CHECKBOX_LABEL);
		errorCheckbox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateLaunchConfigurationDialog();
			}
		});
		setControl(composite);
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
	}

	@Override
	public String getName() {
		return TAB_NAME;
	}

	@Override
	public String getId() {
		return "org.eclipse.launchbar.ui.tests.internal.ErrorToggleLaunchConfigTab";
	}

	@Override
	public boolean isValid(ILaunchConfiguration launchConfig) {
		setWarningMessage(WARNING_MESSAGE);
		if (errorCheckbox != null && !errorCheckbox.isDisposed() && errorCheckbox.getSelection()) {
			setErrorMessage(ERROR_MESSAGE);
			return false;
		}
		setErrorMessage(null);
		return true;
	}
}
