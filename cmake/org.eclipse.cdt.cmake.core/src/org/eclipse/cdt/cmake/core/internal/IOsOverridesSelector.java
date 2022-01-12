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

package org.eclipse.cdt.cmake.core.internal;

import org.eclipse.cdt.cmake.core.properties.ICMakeProperties;
import org.eclipse.cdt.cmake.core.properties.IOsOverrides;

/**
 * Responsible for retrieving the {@link IOsOverrides} that match the target operating system of a
 * project build.
 *
 * @author Martin Weber
 */
interface IOsOverridesSelector {
	/**
	 * Gets the overrides from the specified {@code ICMakeProperties} object that match the target
	 * operating system. <br>
	 * Intended to get the command-line arguments to generate build-scripts or perform the build if
	 * the platform the eclipse workbench is running on differs from the platform the build
	 * artifacts are to be build for. (E.g.: The workbench runs on windows but the build is to run
	 * in a container that runs linux.)
	 */
	IOsOverrides getOsOverrides(ICMakeProperties cmakeProperties);
}
