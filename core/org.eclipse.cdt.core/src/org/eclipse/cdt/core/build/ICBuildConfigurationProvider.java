/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.core.build;

import java.util.Collection;
import java.util.StringJoiner;

import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.LaunchTargetUtils;

/**
 * A CBuildConfigurationProvider provides Core Build configurations.
 *
 * @since 6.0
 */
public interface ICBuildConfigurationProvider {

	/**
	 * Return the id of this provider
	 *
	 * @return provider id
	 */
	String getId();

	/**
	 * Returns the Core Build configuration that owns this Platform Build configuration.
	 *
	 * @param buildConfig Platform Build Configuration. Must not be null. Configs with the name
	 *   {@link IBuildConfiguration#DEFAULT_CONFIG_NAME} are ignored.
	 * @param cBuildConfigName Name to give the ICBuildConfiguration. Must not be null.
	 * @return a Core Build configuration or null if buildConfig has name
	 *   {@link IBuildConfiguration#DEFAULT_CONFIG_NAME}.
	 * @throws CoreException if this method fails. Reasons include:
	 * <ul>
	 * <li> Toolchain is missing,</li>
	 * <li> Launch mode missing,</li>
	 * <li> Launch Target is missing.</li>
	 * </ul>
	 */
	ICBuildConfiguration getCBuildConfiguration(IBuildConfiguration buildConfig, String cBuildConfigName)
			throws CoreException;

	/**
	 * Create a new Platform Build/Core Build configuration combination.
	 *
	 * @param project Project to associate this Core Build configuration to. Must not be null.
	 * @param toolChain Toolchain to associate with this ICBuildConfiguration. Must not be null.
	 * @param launchMode Launch mode (eg "debug") to associate with this ICBuildConfiguration. Must not be null.
	 * @param launchTarget Launch target to associate with this ICBuildConfiguration. Must not be null.
	 * @param monitor
	 * @return a Core Build configuration.
	 * @throws CoreException if this method fails. Reasons include:
	 * <ul>
	 * <li> This project does not exist.</li>
	 * <li> This project is not open.</li>
	 * <li> The reasons given in {@link IProject#setDescription(org.eclipse.core.resources.IProjectDescription, IProgressMonitor)}.</li>
	 * </ul>
	 *
	 * @since 9.0
	 */
	ICBuildConfiguration createCBuildConfiguration(IProject project, IToolChain toolChain, String launchMode,
			ILaunchTarget launchTarget, IProgressMonitor monitor) throws CoreException;

	/**
	 * Name used when creating a Core Build configuration, used by {@link #createCBuildConfiguration}.
	 * This is the name used when the build output directory is created.
	 * @param project Project associated with this ICBuildConfiguration. Must not be null.
	 * @param toolName Name of the build tool (eg: "cmake", "make"). Must not be null.
	 * @param toolchain Toolchain associated with this ICBuildConfiguration. Must not be null.
	 * @param launchMode Launch mode (eg "debug") associated with this ICBuildConfiguration. Must not be null.
	 * @param launchTarget Launch target associated with this ICBuildConfiguration. Must not be null.
	 * @see {@link ICBuildConfiguration2#getBuildDirectoryURI()}
	 *
	 * @return Name used when a Core Build configuration is created. Default implementation uses the following pattern <br>
	 *   toolName.launchMode.toolchain OS.toolchain Arch.launchTarget Id
	 * <p>For example, a cmake build, in debug mode, using a GCC windows toolchain with the Local launch target:<br>
	 *   "cmake.debug.win32.x86_64.Local"
	 * <p>A different pattern is used when running in a Docker container.
	 * @since 9.0
	 */
	default String getCBuildConfigName(IProject project, String toolName, IToolChain toolchain, String launchMode,
			ILaunchTarget launchTarget) {
		StringJoiner configName = new StringJoiner("."); //$NON-NLS-1$
		configName.add(toolName);
		configName.add(launchMode);

		String os = toolchain.getProperty(IToolChain.ATTR_OS);
		if ("linux-container".equals(os)) { //$NON-NLS-1$
			String osConfigName = toolchain.getProperty("linux-container-id").replaceAll("/", "_"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			configName.add(osConfigName);
		} else {
			String fragment = toolchain.getBuildConfigNameFragment();
			if (fragment != null && !fragment.isEmpty()) {
				configName.add(fragment);
			}
			configName.add(LaunchTargetUtils.sanitizeName(launchTarget.getId()));
		}
		return configName.toString();
	}

	/**
	 * Return a collection of supported toolchains for build configurations of this
	 * type.
	 *
	 * @since 6.4
	 */
	default Collection<IToolChain> getSupportedToolchains(Collection<IToolChain> toolchains) throws CoreException {
		return toolchains;
	}

}
