/*******************************************************************************
 * Copyright (c) 2021 Mat Booth and others.
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
 * This is a custom tab that uses a non-GridLayout to test the assumptions made
 * by the Launchbar's launch configuration editing dialog. The dialog should not
 * generate CCEs if bespoke tabs do not use GridLayout.
 *
 * See bug 560287
 */
public class CustomLaunchConfigTab extends AbstractLaunchConfigurationTab {

	@Override
	public void createControl(Composite parent) {
		parent.setLayout(new RowLayout());
		Label label = new Label(parent, SWT.NONE);
		label.setText("This is my custom tab!");
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
		return "My Custom Tab";
	}

	@Override
	public String getId() {
		return "my.custom.tab";
	}
}
