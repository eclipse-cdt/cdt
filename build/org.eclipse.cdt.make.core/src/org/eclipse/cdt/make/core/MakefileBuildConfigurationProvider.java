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
package org.eclipse.cdt.make.core;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.build.CBuildConfigUtils;
import org.eclipse.cdt.core.build.ICBuildConfiguration;
import org.eclipse.cdt.core.build.ICBuildConfigurationManager;
import org.eclipse.cdt.core.build.ICBuildConfigurationProvider;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.build.IToolChainManager;
import org.eclipse.cdt.core.build.StandardBuildConfiguration;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.ILaunchTargetManager;

/**
 * @since 7.4
 */
public class MakefileBuildConfigurationProvider implements ICBuildConfigurationProvider {

	public static final String ID = "org.eclipse.cdt.make.core.provider"; //$NON-NLS-1$
	private final ILaunchTargetManager launchTargetManager = MakeCorePlugin.getService(ILaunchTargetManager.class);

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public ICBuildConfiguration getCBuildConfiguration(IBuildConfiguration config, String name) throws CoreException {
		if (config.getName().equals(IBuildConfiguration.DEFAULT_CONFIG_NAME)) {
			IToolChain toolChain = null;

			// try the toolchain for the local target
			Map<String, String> properties = new HashMap<>();
			properties.put(IToolChain.ATTR_OS, Platform.getOS());
			properties.put(IToolChain.ATTR_ARCH, Platform.getOSArch());
			IToolChainManager toolChainManager = MakeCorePlugin.getService(IToolChainManager.class);
			for (IToolChain tc : toolChainManager.getToolChainsMatching(properties)) {
				toolChain = tc;
				break;
			}

			// local didn't work, try and find one that does
			if (toolChain == null) {
				for (IToolChain tc : toolChainManager.getToolChainsMatching(new HashMap<>())) {
					toolChain = tc;
					break;
				}
			}

			if (toolChain != null) {
				return new StandardBuildConfiguration(config, name, toolChain, "run", //$NON-NLS-1$
						launchTargetManager.getLocalLaunchTarget());
			} else {
				// No valid combinations
				return null;
			}
		}
		return new StandardBuildConfiguration(config, name);
	}

	@Override
	public ICBuildConfiguration createCBuildConfiguration(IProject project, IToolChain toolChain, String launchMode,
			ILaunchTarget launchTarget, IProgressMonitor monitor) throws CoreException {
		// Compute name to use for ICBuildConfiguration
		String cBuildConfigName = getCBuildConfigName(project, "make", toolChain, launchMode, launchTarget); //$NON-NLS-1$

		// Create Platform Build configuration
		ICBuildConfigurationManager cBuildConfigManager = MakeCorePlugin.getService(ICBuildConfigurationManager.class);
		IBuildConfiguration buildConfig = CBuildConfigUtils.createBuildConfiguration(this, project, cBuildConfigName,
				cBuildConfigManager, monitor);

		// Create Core Build configuration
		ICBuildConfiguration cBuildConfig = new StandardBuildConfiguration(buildConfig, cBuildConfigName, toolChain,
				launchMode, launchTarget);

		// Add the Platform Build/Core Build configuration combination
		cBuildConfigManager.addBuildConfiguration(buildConfig, cBuildConfig);
		return cBuildConfig;
	}
}
