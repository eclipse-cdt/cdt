/*******************************************************************************
 * Copyright (c) 2024 Renesas Electronics Europe and others.
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
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * Creates a tab to test that the Launch Configuration, when opened via the Launch Bar,
 * displays a warning in the message area.
 * @see {@link CreateLaunchConfigTests}
 */
public class WarningLaunchConfigTab extends AbstractLaunchConfigurationTab {
	public static final String WARNING_MESSAGE = "This is a warning";
	public static final String TAB_NAME = "Warning Tab";

	@Override
	public void createControl(Composite parent) {
		parent.setLayout(new RowLayout());
		Label label = new Label(parent, SWT.NONE);
		label.setText("The Launch Configuration message area should show a warning message!");
		setControl(label);
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
		return "org.eclipse.launchbar.ui.tests.internal.WarningLaunchConfigTab";
	}

	@Override
	public boolean isValid(ILaunchConfiguration launchConfig) {
		setWarningMessage(WARNING_MESSAGE);
		return true;
	}
}
