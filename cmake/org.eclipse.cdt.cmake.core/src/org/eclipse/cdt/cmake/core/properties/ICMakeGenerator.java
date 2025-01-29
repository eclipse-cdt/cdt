/*******************************************************************************
 * Copyright (c) 2020, 2025 Martin Weber and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.cmake.core.properties;

import org.eclipse.cdt.cmake.core.CMakeBuildConfiguration;

/**
 * This class can be implemented by extenders who want to contribute a fully custom
 * CMakeGenerator configuration when generating {@link ICMakeProperties} in
 * {@link CMakeBuildConfiguration#getCMakeProperties()};
 *
 * @since 2.0
 */
public interface ICMakeGenerator {

	/**
	 * Gets the cmake argument that specifies the build-script generator.
	 *
	 * @return a non-empty string, which must be a valid argument for cmake's -G
	 *         option.
	 */
	String getCMakeName();

	/**
	 * Gets the name of the top-level makefile (build-script) which is interpreted
	 * by the build-script processor.
	 *
	 * @return name of the makefile, or {@code null} for CDT to always run CMake.
	 */
	String getMakefileName();

	/**
	 * Gets the build-script processor´s command argument(s) to ignore build errors.
	 *
	 * @return the command option string or {@code null} if no argument is needed.
	 */
	String getIgnoreErrOption();

}
