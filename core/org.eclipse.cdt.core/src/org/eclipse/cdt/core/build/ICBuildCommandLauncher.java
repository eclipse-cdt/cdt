/*******************************************************************************
 *  Copyright (c) 2018 Red Hat Inc. and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *      Red Hat Inc. - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.build;

/**
 * @since 6.5
 */
public interface ICBuildCommandLauncher {

	/**
	 * Get registered CBuildConfiguration
	 *
	 * @return ICBuildConfiguration or null
	 */
	public ICBuildConfiguration getBuildConfiguration();

	/**
	 * Register a CBuildConfiguration for this command launcher
	 *
	 * @param config - CBuildConfiguration to register
	 */
	public void setBuildConfiguration(ICBuildConfiguration config);

	/**
	 * Get any special console header (e.g. Container Image used)
	 */
	public String getConsoleHeader();

}
