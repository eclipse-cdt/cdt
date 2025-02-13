/*******************************************************************************
 * Copyright (c) 2016, 2019 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors
 * 		Red Hat Inc. - modified for use with Meson build
 *******************************************************************************/
package org.eclipse.cdt.internal.meson.core;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.build.CBuildConfigUtils;
import org.eclipse.cdt.core.build.ICBuildConfiguration;
import org.eclipse.cdt.core.build.ICBuildConfigurationManager;
import org.eclipse.cdt.core.build.ICBuildConfigurationProvider;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.meson.core.Activator;
import org.eclipse.cdt.meson.core.IMesonToolChainFile;
import org.eclipse.cdt.meson.core.IMesonToolChainManager;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.ILaunchTargetManager;

public class MesonBuildConfigurationProvider implements ICBuildConfigurationProvider {

	public static final String ID = "org.eclipse.cdt.meson.core.provider"; //$NON-NLS-1$

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public synchronized ICBuildConfiguration getCBuildConfiguration(IBuildConfiguration config, String name)
			throws CoreException {
		if (config.getName().equals(IBuildConfiguration.DEFAULT_CONFIG_NAME)) {
			/*
			 * IBuildConfiguration configs with name IBuildConfiguration.DEFAULT_CONFIG_NAME
			 * are not supported to avoid build output directory being named "default".
			 */
			return null;
		}
		MesonBuildConfiguration mesonConfig = new MesonBuildConfiguration(config, name);
		IMesonToolChainFile tcFile = mesonConfig.getToolChainFile();
		IToolChain toolChain = mesonConfig.getToolChain();
		if (toolChain == null) {
			// config not complete
			return null;
		}
		if (tcFile != null && !toolChain.equals(tcFile.getToolChain())) {
			// toolchain changed
			ILaunchTargetManager launchTargetManager = Activator.getService(ILaunchTargetManager.class);
			return new MesonBuildConfiguration(config, name, tcFile.getToolChain(), tcFile, mesonConfig.getLaunchMode(),
					launchTargetManager.getLocalLaunchTarget());
		}
		return mesonConfig;
	}

	@Override
	public ICBuildConfiguration createCBuildConfiguration(IProject project, IToolChain toolChain, String launchMode,
			ILaunchTarget launchTarget, IProgressMonitor monitor) throws CoreException {
		// get matching toolchain file if any
		IMesonToolChainManager manager = Activator.getService(IMesonToolChainManager.class);
		IMesonToolChainFile file = manager.getToolChainFileFor(toolChain);
		if (file == null) {
			Map<String, String> properties = new HashMap<>();
			String os = toolChain.getProperty(IToolChain.ATTR_OS);
			if (os != null && !os.isEmpty()) {
				properties.put(IToolChain.ATTR_OS, os);
			}
			String arch = toolChain.getProperty(IToolChain.ATTR_ARCH);
			if (arch != null && !arch.isEmpty()) {
				properties.put(IToolChain.ATTR_ARCH, arch);
			}
			Collection<IMesonToolChainFile> files = manager.getToolChainFilesMatching(properties);
			if (!files.isEmpty()) {
				file = files.iterator().next();
				toolChain = file.getToolChain();
			}
		}

		// Compute name to use for ICBuildConfiguration
		String cBuildConfigName = getCBuildConfigName(project, "meson", toolChain, launchMode, launchTarget); //$NON-NLS-1$

		// Create Platform Build configuration
		ICBuildConfigurationManager cBuildConfigManager = Activator.getService(ICBuildConfigurationManager.class);
		IBuildConfiguration buildConfig = CBuildConfigUtils.createBuildConfiguration(this, project, cBuildConfigName,
				cBuildConfigManager, monitor);

		// Create Core Build configuration
		ICBuildConfiguration cBuildConfig = new MesonBuildConfiguration(buildConfig, cBuildConfigName, toolChain, file,
				launchMode, launchTarget);
		cBuildConfigManager.addBuildConfiguration(buildConfig, cBuildConfig);
		return cBuildConfig;
	}

}
