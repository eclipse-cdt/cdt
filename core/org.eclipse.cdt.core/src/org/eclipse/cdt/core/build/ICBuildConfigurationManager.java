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

import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.launchbar.core.target.ILaunchTarget;

/**
 * The OSGi service that manages the mapping from platform build configuration
 * to CDT build configuration.
 *
 * @since 6.0
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICBuildConfigurationManager {

	/**
	 * Return the build configuration provider with the given id.
	 *
	 * @param id
	 * @return build configuration provider
	 */
	ICBuildConfigurationProvider getProvider(String id);

	/**
	 * Return whether the given project has a configuration with the given
	 * configName.
	 *
	 * @param provider
	 * @param project
	 * @param configName
	 * @return true if project has the named config
	 * @throws CoreException
	 * @since 6.4
	 */
	boolean hasConfiguration(ICBuildConfigurationProvider provider, IProject project, String configName)
			throws CoreException;

	/**
	 * Create a new Platform Build configuration to be owned by a Core Build config provider.
	 *
	 * @param provider The project's Core Build config provider.  Must not be null.
	 * @param project Project in which the Platform Build configuration is created. Must not be null.
	 * @param configName The Core Build config name to be used as part of the the Platform Build configuration
	 * name. Must not be null.
	 * @param monitor
	 * @return new Platform Build configuration. Not null.
	 * @throws CoreException Reasons include the reasons given in
	 * {@link IProject#setDescription(org.eclipse.core.resources.IProjectDescription, IProgressMonitor)}
	 */
	IBuildConfiguration createBuildConfiguration(ICBuildConfigurationProvider provider, IProject project,
			String configName, IProgressMonitor monitor) throws CoreException;

	/**
	 * Finds an existing Core Build configuration or creates a new one if one does not exist.
	 *
	 * The project's ICBuildConfigurationProvider is used to attempt to get an existing configuration or create a new one.
	 *
	 * @param project Project to associate this Core Build Configuration to. Must not be null.
	 * @param toolChain Toolchain to associate with this ICBuildConfiguration. Must not be null.
	 * @param launchMode Launch mode (eg "debug") to associate with this ICBuildConfiguration. Must not be null.
	 * @param launchTarget Launch target to associate with this ICBuildConfiguration. Must not be null.
	 * @param monitor
	 * @return a Core Build configuration matching the supplied parameters. Not null.
	 * @throws CoreException Reasons include:
	 * <ul>
	 * <li> The project does not exist.</li>
	 * <li> The project is not open.</li>
	 * <li> The project's {@link ICBuildConfigurationProvider#getCBuildConfiguration(IBuildConfiguration, String)} fails.</li>
	 * <li> The project's {@link ICBuildConfigurationProvider} is not found.</li>
	 * <li> There is a problem accessing the toolchain.</li>
	 * </ul>
	 * @since 9.0
	 * @apiNote This should be renamed to getCBuildConfiguration as it returns a Core Build config.
	 */
	ICBuildConfiguration getBuildConfiguration(IProject project, IToolChain toolChain, String launchMode,
			ILaunchTarget launchTarget, IProgressMonitor monitor) throws CoreException;

	/**
	 * Called by providers to add new build configurations as they are created.
	 *
	 * @param buildConfig
	 *            platform build configuration
	 * @param cConfig
	 *            CDT build configuration
	 */
	void addBuildConfiguration(IBuildConfiguration buildConfig, ICBuildConfiguration cConfig);

	/**
	 * Return the CDT build configuration associated with the given Platform
	 * build configuration.
	 *
	 * @param buildConfig
	 * @return the matching CDT build configuration
	 */
	ICBuildConfiguration getBuildConfiguration(IBuildConfiguration buildConfig) throws CoreException;

	/**
	 * Does this build system support this project. This is determined by
	 * searching the build configuration providers looking to see if any of them
	 * support this project.
	 *
	 * @param project
	 * @return is this project supported by this build system
	 * @throws CoreException
	 * @since 6.1
	 */
	boolean supports(IProject project) throws CoreException;

}
