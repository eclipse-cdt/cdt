/*******************************************************************************
 * Copyright (c) 2018 Red Hat Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Inc. - Initial Contribution
 *******************************************************************************/
package org.eclipse.cdt.internal.meson.ui.wizards;

import org.eclipse.cdt.core.build.ICBuildConfiguration;
import org.eclipse.cdt.meson.core.IMesonConstants;
import org.eclipse.jface.wizard.Wizard;

public class RunNinja extends Wizard {

	private RunNinjaPage mainPage;
	private ICBuildConfiguration config;
	private String envStr;
	private String ninjaArgs;

	public RunNinja(ICBuildConfiguration config) {
		this.config = config;
	}

	@Override
	public void addPages() {
		mainPage = new RunNinjaPage(config);
		addPage(mainPage);
	}

	@Override
	public boolean canFinish() {
		return mainPage.isPageComplete();
	}

	@Override
	public boolean performFinish() {
		envStr = mainPage.getEnvStr();
		config.setProperty(IMesonConstants.NINJA_ENV, envStr);
		ninjaArgs = mainPage.getNinjaArgs();
		config.setProperty(IMesonConstants.NINJA_ARGUMENTS, ninjaArgs);
		return true;
	}

	public String getEnvStr() {
		return envStr;
	}

	public String getNinjaArgs() {
		return ninjaArgs;
	}

}
