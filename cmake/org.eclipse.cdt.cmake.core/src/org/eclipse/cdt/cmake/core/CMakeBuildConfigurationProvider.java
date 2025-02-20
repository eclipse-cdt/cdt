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
package org.eclipse.cdt.cmake.core;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.cmake.core.internal.Activator;
import org.eclipse.cdt.core.build.CBuildConfigUtils;
import org.eclipse.cdt.core.build.CBuildConfiguration;
import org.eclipse.cdt.core.build.ICBuildConfiguration;
import org.eclipse.cdt.core.build.ICBuildConfigurationManager;
import org.eclipse.cdt.core.build.ICBuildConfigurationProvider;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.ILaunchTargetManager;

/**
 * A ICBuildConfigurationProvider specialized for CMake
 *
 * Extenders can provide their own specialised CMakeConfiguration by extending this class and {@link CMakeBuildConfiguration}.
 * Extenders need to override at least {@link #getId()}, and the various createCMakeBuildConfiguration methods.
 * See the example project <a href="https://github.com/eclipse-cdt/cdt/tree/main/cmake/org.eclipse.cdt.cmake.example">
 * org.eclipse.cdt.cmake.example</a> for a full example.
 *
 * @since 2.0
 */
public class CMakeBuildConfigurationProvider implements ICBuildConfigurationProvider {

	public static final String ID = "org.eclipse.cdt.cmake.core.provider"; //$NON-NLS-1$

	@Override
	public String getId() {
		return ID;
	}

	/**
	 * Extenders should override this method to construct their specialized build configuration.
	 *
	 * @see {@link CBuildConfiguration#CBuildConfiguration(IBuildConfiguration, String)} for common documentation on
	 * parameters.
	 */
	protected CMakeBuildConfiguration createCMakeBuildConfiguration(IBuildConfiguration config, String name)
			throws CoreException {
		return new CMakeBuildConfiguration(config, name);
	}

	/**
	 * Extenders should override this method to construct their specialized build configuration.
	 *
	 * @param toolChainFile CMake toolchain file to associate with this CMakeBuildConfiguration. May be null.
	 *
	 * @see {@link CBuildConfiguration#CBuildConfiguration(IBuildConfiguration, String, IToolChain, String, ILaunchTarget)}
	 * for common documentation on other parameters.
	 */
	protected CMakeBuildConfiguration createCMakeBuildConfiguration(IBuildConfiguration config, String name,
			IToolChain toolChain, ICMakeToolChainFile toolChainFile, String launchMode, ILaunchTarget launchTarget) {
		return new CMakeBuildConfiguration(config, name, toolChain, toolChainFile, launchMode, launchTarget);
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
		CMakeBuildConfiguration cmakeConfig = createCMakeBuildConfiguration(config, name);
		ICMakeToolChainFile tcFile = cmakeConfig.getToolChainFile();
		IToolChain toolChain = cmakeConfig.getToolChain();
		if (toolChain == null) {
			// config not complete
			return null;
		}
		if (tcFile != null && !toolChain.equals(tcFile.getToolChain())) {
			// toolchain changed
			ILaunchTargetManager launchTargetManager = Activator.getService(ILaunchTargetManager.class);
			return createCMakeBuildConfiguration(config, name, tcFile.getToolChain(), tcFile,
					cmakeConfig.getLaunchMode(), launchTargetManager.getLocalLaunchTarget());
		} else {
			return cmakeConfig;
		}
	}

	@Override
	public ICBuildConfiguration createCBuildConfiguration(IProject project, IToolChain toolChain, String launchMode,
			ILaunchTarget launchTarget, IProgressMonitor monitor) throws CoreException {
		// get matching toolchain file if any
		ICMakeToolChainManager manager = Activator.getService(ICMakeToolChainManager.class);
		ICMakeToolChainFile file = manager.getToolChainFileFor(toolChain);
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
			Collection<ICMakeToolChainFile> files = manager.getToolChainFilesMatching(properties);
			if (!files.isEmpty()) {
				file = files.iterator().next();
			}
		}

		// Compute name to use for ICBuildConfiguration
		String cBuildConfigName = getCBuildConfigName(project, "cmake", toolChain, launchMode, launchTarget); //$NON-NLS-1$

		// Create Platform Build configuration
		ICBuildConfigurationManager cBuildConfigManager = Activator.getService(ICBuildConfigurationManager.class);
		IBuildConfiguration buildConfig = CBuildConfigUtils.createBuildConfiguration(this, project, cBuildConfigName,
				cBuildConfigManager, monitor);

		// Create Core Build configuration
		ICBuildConfiguration cBuildConfig = createCMakeBuildConfiguration(buildConfig, cBuildConfigName, toolChain,
				file, launchMode, launchTarget);

		// Add the Platform Build/Core Build configuration combination
		cBuildConfigManager.addBuildConfiguration(buildConfig, cBuildConfig);
		return cBuildConfig;
	}
}
