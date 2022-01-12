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

/**
 * @since 7.4
 */
public class MakefileBuildConfigurationProvider implements ICBuildConfigurationProvider {

	public static final String ID = "org.eclipse.cdt.make.core.provider"; //$NON-NLS-1$

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
				return new StandardBuildConfiguration(config, name, toolChain, "run"); //$NON-NLS-1$
			} else {
				// No valid combinations
				return null;
			}
		}
		return new StandardBuildConfiguration(config, name);
	}

	@Override
	public ICBuildConfiguration createBuildConfiguration(IProject project, IToolChain toolChain, String launchMode,
			IProgressMonitor monitor) throws CoreException {
		ICBuildConfigurationManager configManager = MakeCorePlugin.getService(ICBuildConfigurationManager.class);

		StringBuilder configName = new StringBuilder("make."); //$NON-NLS-1$
		configName.append(launchMode);
		String os = toolChain.getProperty(IToolChain.ATTR_OS);
		if ("linux-container".equals(os)) { //$NON-NLS-1$
			String osConfigName = toolChain.getProperty("linux-container-id"); //$NON-NLS-1$
			osConfigName = osConfigName.replaceAll("/", "_"); //$NON-NLS-1$ //$NON-NLS-2$
			configName.append('.');
			configName.append(osConfigName);
		} else {
			if (os != null) {
				configName.append('.');
				configName.append(os);
			}
			String arch = toolChain.getProperty(IToolChain.ATTR_ARCH);
			if (arch != null && !arch.isEmpty()) {
				configName.append('.');
				configName.append(arch);
			}
		}
		String name = configName.toString();
		IBuildConfiguration config = null;
		// reuse any IBuildConfiguration with the same name for the project
		// so adding the CBuildConfiguration will override the old one stored
		// by the CBuildConfigurationManager
		if (configManager.hasConfiguration(this, project, name)) {
			config = project.getBuildConfig(this.getId() + '/' + name);
		}
		if (config == null) {
			config = configManager.createBuildConfiguration(this, project, name, monitor);
		}
		StandardBuildConfiguration makeConfig = new StandardBuildConfiguration(config, name, toolChain, launchMode);
		configManager.addBuildConfiguration(config, makeConfig);
		return makeConfig;
	}

}
