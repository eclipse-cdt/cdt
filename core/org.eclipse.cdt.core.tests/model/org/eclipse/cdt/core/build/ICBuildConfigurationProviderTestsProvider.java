/*******************************************************************************
 * Copyright (c) 2025 Renesas Electronics Europe.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.core.build;

import org.eclipse.cdt.cmake.core.CMakeBuildConfigurationProvider;
import org.eclipse.core.resources.IProject;
import org.eclipse.launchbar.core.target.ILaunchTarget;

/**
 * Supporting class for test {@link ICBuildConfigurationProviderTests}
 */
public class ICBuildConfigurationProviderTestsProvider extends CMakeBuildConfigurationProvider {

	@Override
	public String getId() {
		return "org.eclipse.cdt.core.build.ICBuildConfigurationProviderTests.providerId";
	}

	@Override
	public String getCBuildConfigName(IProject project, String toolName, IToolChain toolchain, String launchMode,
			ILaunchTarget launchTarget) {
		return super.getCBuildConfigName(project, toolName, toolchain, launchMode, launchTarget) + ".customizedTest";
	}
}