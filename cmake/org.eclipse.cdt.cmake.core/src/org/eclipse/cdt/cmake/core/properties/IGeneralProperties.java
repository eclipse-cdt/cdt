/*******************************************************************************
 * Copyright (c) 2020 Martin Weber.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.cmake.core.properties;

/**
 * General, non-cmake related project settings.
 *
 * @author Martin Weber
 *
 */
public interface IGeneralProperties {

	/**
	 * Gets the name of the top-level directory relative to the project root.
	 */
	String getSourceDirectory();

	/**
	 * Sets the name of the top-level directory relative to the project root.
	 */
	void setSourceDirectory(String sourceDirectory);

	/**
	 * Gets the name of the build directory.
	 */
	String getBuildDirectory();

	/**
	 * Sets the name of the build directory.
	 */
	void setBuildDirectory(String buildDirectory);
}