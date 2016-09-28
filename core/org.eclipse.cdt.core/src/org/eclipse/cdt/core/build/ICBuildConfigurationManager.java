/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.core.build;

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

	/**
	 * To allow build configuration options to be stored as launch configuration
	 * attributes this method generates the name of the map of options for a
	 * given launch mode. Pass null as the launchMode to return the name to be
	 * used to gather options for all modes.
	 * 
	 * @param launchMode
	 *            launch mode or null for all launch modes
	 * @return attribute name for attributes for a given mode or for all modes
	 * @since 6.2
	 */
	String getAttributeName(String launchMode);

}
