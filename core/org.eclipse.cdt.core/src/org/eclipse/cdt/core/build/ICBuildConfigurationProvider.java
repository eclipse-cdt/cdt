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

import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * A CBuildConfigurationProvider provides C build configurations.
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
	 * Returns the ICBuildConfiguration that owns this build configuration.
	 *
	 * @param config
	 * @return CDT build configuration for the Platform build configuration
	 */
	ICBuildConfiguration getCBuildConfiguration(IBuildConfiguration config, String name) throws CoreException;

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
	 * @return new build configuration or null combination not supported
	 * @throws CoreException
	 * @since 6.1
	 */
	default ICBuildConfiguration createBuildConfiguration(IProject project, IToolChain toolChain, String launchMode,
			IProgressMonitor monitor) throws CoreException {
		return null;
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
