/*******************************************************************************
 * Copyright (c) 2016, 2019 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.cmake.core.internal;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.cmake.core.ICMakeToolChainFile;
import org.eclipse.cdt.cmake.core.ICMakeToolChainManager;
import org.eclipse.cdt.core.build.ICBuildConfiguration;
import org.eclipse.cdt.core.build.ICBuildConfigurationManager;
import org.eclipse.cdt.core.build.ICBuildConfigurationProvider;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.build.IToolChainManager;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;

public class CMakeBuildConfigurationProvider implements ICBuildConfigurationProvider {

	public static final String ID = "org.eclipse.cdt.cmake.core.provider"; //$NON-NLS-1$

	private ICMakeToolChainManager manager = Activator.getService(ICMakeToolChainManager.class);
	private ICBuildConfigurationManager configManager = Activator.getService(ICBuildConfigurationManager.class);

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public synchronized ICBuildConfiguration getCBuildConfiguration(IBuildConfiguration config, String name)
			throws CoreException {
		if (config.getName().equals(IBuildConfiguration.DEFAULT_CONFIG_NAME)) {
			IToolChain toolChain = null;

			// try the toolchain for the local target
			Map<String, String> properties = new HashMap<>();
			properties.put(IToolChain.ATTR_OS, Platform.getOS());
			properties.put(IToolChain.ATTR_ARCH, Platform.getOSArch());
			IToolChainManager toolChainManager = Activator.getService(IToolChainManager.class);
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
				return new CMakeBuildConfiguration(config, name, toolChain);
			} else {
				// No valid combinations
				return null;
			}
		}
		CMakeBuildConfiguration cmakeConfig = new CMakeBuildConfiguration(config, name);
		ICMakeToolChainFile tcFile = cmakeConfig.getToolChainFile();
		IToolChain toolChain = cmakeConfig.getToolChain();
		if (toolChain == null) {
			// config not complete
			return null;
		}
		if (tcFile != null && !toolChain.equals(tcFile.getToolChain())) {
			// toolchain changed
			return new CMakeBuildConfiguration(config, name, tcFile.getToolChain(), tcFile,
					cmakeConfig.getLaunchMode());
		} else {
			return cmakeConfig;
		}
	}

	@Override
	public ICBuildConfiguration createBuildConfiguration(IProject project, IToolChain toolChain, String launchMode,
			IProgressMonitor monitor) throws CoreException {
		// get matching toolchain file if any
		Map<String, String> properties = new HashMap<>();
		String os = toolChain.getProperty(IToolChain.ATTR_OS);
		if (os != null && !os.isEmpty()) {
			properties.put(IToolChain.ATTR_OS, os);
		}
		String arch = toolChain.getProperty(IToolChain.ATTR_ARCH);
		if (arch != null && !arch.isEmpty()) {
			properties.put(IToolChain.ATTR_ARCH, arch);
		}
		ICMakeToolChainFile file = manager.getToolChainFileFor(toolChain);
		if (file == null) {
			Collection<ICMakeToolChainFile> files = manager.getToolChainFilesMatching(properties);
			if (!files.isEmpty()) {
				file = files.iterator().next();
			}
		}

		// create config
		StringBuilder configName = new StringBuilder("cmake."); //$NON-NLS-1$
		configName.append(launchMode);
		if ("linux-container".equals(os)) { //$NON-NLS-1$
			String osConfigName = toolChain.getProperty("linux-container-id"); //$NON-NLS-1$
			osConfigName = osConfigName.replaceAll("/", "_"); //$NON-NLS-1$ //$NON-NLS-2$
			configName.append('.');
			configName.append(osConfigName);
		} else {
			String fragment = toolChain.getBuildConfigNameFragment();
			if (fragment != null && !fragment.isEmpty()) {
				configName.append('.');
				configName.append(fragment);
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

		CMakeBuildConfiguration cmakeConfig = new CMakeBuildConfiguration(config, name, toolChain, file, launchMode);
		configManager.addBuildConfiguration(config, cmakeConfig);
		return cmakeConfig;
	}

}
