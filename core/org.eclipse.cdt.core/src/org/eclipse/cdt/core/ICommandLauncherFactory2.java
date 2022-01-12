/*******************************************************************************
 * Copyright (c) 2018 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core;

import java.util.List;

import org.eclipse.cdt.core.build.ICBuildConfiguration;

/**
 * @since 6.5
 */
public interface ICommandLauncherFactory2 {

	public static final String CONTAINER_BUILD_ENABLED = "container.build.enabled"; //$NON-NLS-1$

	/**
	 * Get a Command Launcher for a build configuration descriptor
	 * @param cfg - ICBuildConfiguration to get command launcher for
	 * @return ICommandLauncher or null
	 */
	public default ICommandLauncher getCommandLauncher(ICBuildConfiguration cfg) {
		return null;
	}

	/**
	 * Process include paths and if necessary copy header files as needed.
	 * @param cfg - ICBuildConfiguration to process includes for
	 * @param includes List of include paths to process
	 * @return processed List of include paths
	 */
	public default List<String> verifyIncludePaths(ICBuildConfiguration cfg, List<String> includes) {
		return includes;
	}

}
