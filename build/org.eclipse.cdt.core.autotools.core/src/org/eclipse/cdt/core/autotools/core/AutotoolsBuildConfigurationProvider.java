/*******************************************************************************
 * Copyright (c) 2017, 2019 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Intel Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.autotools.core;

import org.eclipse.cdt.core.autotools.core.internal.Activator;
import org.eclipse.cdt.core.build.CBuildConfigUtils;
import org.eclipse.cdt.core.build.ICBuildConfiguration;
import org.eclipse.cdt.core.build.ICBuildConfigurationManager;
import org.eclipse.cdt.core.build.ICBuildConfigurationProvider;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.launchbar.core.target.ILaunchTarget;

public class AutotoolsBuildConfigurationProvider implements ICBuildConfigurationProvider {
	public static final String ID = Activator.PLUGIN_ID + ".provider"; //$NON-NLS-1$

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public ICBuildConfiguration getCBuildConfiguration(IBuildConfiguration config, String name) throws CoreException {
		if (config.getName().equals(IBuildConfiguration.DEFAULT_CONFIG_NAME)) {
			/*
			 * IBuildConfiguration configs with name IBuildConfiguration.DEFAULT_CONFIG_NAME
			 * are not supported to avoid build output directory being named "default".
			 */
			return null;
		}
		AutotoolsBuildConfiguration autotoolsConfig = new AutotoolsBuildConfiguration(config, name);
		IToolChain toolChain = autotoolsConfig.getToolChain();
		if (toolChain == null) {
			// config not complete
			return null;
		}
		return autotoolsConfig;
	}

	@Override
	public ICBuildConfiguration createCBuildConfiguration(IProject project, IToolChain toolChain, String launchMode,
			ILaunchTarget launchTarget, IProgressMonitor monitor) throws CoreException {
		// Compute name to use for ICBuildConfiguration
		String cBuildConfigName = getCBuildConfigName(project, "autotools", toolChain, launchMode, launchTarget); //$NON-NLS-1$

		// Create Platform Build configuration
		ICBuildConfigurationManager cBuildConfigManager = Activator.getService(ICBuildConfigurationManager.class);
		IBuildConfiguration buildConfig = CBuildConfigUtils.createBuildConfiguration(this, project, cBuildConfigName,
				cBuildConfigManager, monitor);

		// Create Core Build configuration
		ICBuildConfiguration cBuildConfig = new AutotoolsBuildConfiguration(buildConfig, cBuildConfigName, toolChain,
				launchMode, launchTarget);

		// Add the Platform Build/Core Build configuration combination
		cBuildConfigManager.addBuildConfiguration(buildConfig, cBuildConfig);
		return cBuildConfig;
	}

}
