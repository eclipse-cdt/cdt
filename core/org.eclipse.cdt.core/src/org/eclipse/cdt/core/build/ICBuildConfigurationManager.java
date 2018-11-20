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

import java.util.Map;

import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

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
	 * Create a new build configuration to be owned by a provider.
	 *
	 * @param provider
	 * @param project
	 * @param configName
	 * @param monitor
	 * @return new build configuration
	 * @throws CoreException
	 */
	IBuildConfiguration createBuildConfiguration(ICBuildConfigurationProvider provider, IProject project,
			String configName, IProgressMonitor monitor) throws CoreException;

	/**
	 * Create a new build configuration for a given project using a given
	 * toolchain and builds for a given launch mode.
	 *
	 * @param project
	 *            project for the config
	 * @param toolChain
	 *            toolchain the build config will use
	 * @param launchMode
	 *            launch mode the buld config will build for
	 * @return new build configuration
	 * @throws CoreException
	 * @since 6.1
	 */
	ICBuildConfiguration getBuildConfiguration(IProject project, IToolChain toolChain, String launchMode,
			IProgressMonitor monitor) throws CoreException;

	/**
	 * Create a new build configuration for a given project using a toolchain with
	 * the given properties and that builds for a given launch mode.
	 *
	 * @deprecated clients really need to pick which toolchain they want a build
	 *             config for. This method pretty much picks one at random.
	 * @param project
	 *            project for the config
	 * @param properties
	 *            properties for the toolchain to be selected
	 * @param launchMode
	 *            launch mode the buld config will build for
	 * @return new build configuration
	 * @throws CoreException
	 * @since 6.2
	 */
	@Deprecated
	ICBuildConfiguration getBuildConfiguration(IProject project, Map<String, String> properties, String launchMode,
			IProgressMonitor monitor) throws CoreException;

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
