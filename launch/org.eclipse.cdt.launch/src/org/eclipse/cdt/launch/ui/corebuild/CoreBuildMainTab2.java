/*******************************************************************************
 * Copyright (c) 2024 Intel corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.launch.ui.corebuild;

import org.eclipse.cdt.launch.internal.ui.LaunchMessages;
import org.eclipse.cdt.launch.ui.CMainTab2;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.swt.widgets.Composite;

/**
 * @since 11.0
 */
public class CoreBuildMainTab2 extends CMainTab2 {

	/*
	 * A Core Build launch configuration is created immediately upon the Core Build project creation.
	 * It cannot be created by hand and it is not duplicatable and can't be renamed.
	 * The launch configuration is tied to the project. The project name may not be changed.
	 */
	@Override
	protected void createProjectGroup(Composite parent, int colSpan) {
		super.createProjectGroup(parent, colSpan);
		fProjText.setEnabled(false);
		fProjButton.setVisible(false);
	}

	@Override
	protected void createExeFileGroup(Composite parent, int colSpan) {
		super.createExeFileGroup(parent, colSpan);
		fProgText.setMessage(LaunchMessages.CoreBuildMainTab_Keep_empty_for_auto_selection);
	}

	/*
	 * For Core Build projects the build configuration is hidden and it is selected
	 * via the LaunchBar Launch Mode. We remove the BuildConfigCombo.
	 */
	@Override
	protected void createBuildConfigCombo(Composite parent, int colspan) {
		fBuildConfigCombo = null;
	}

	/*
	 * Don't check the program name if it is empty. When the program name is empty the default
	 * CoreBuild binary is used.
	 */
	@Override
	public boolean isValid(ILaunchConfiguration config) {
		String programName = fProgText.getText().trim();
		setDontCheckProgram(programName.isEmpty());
		return super.isValid(config);
	}
}