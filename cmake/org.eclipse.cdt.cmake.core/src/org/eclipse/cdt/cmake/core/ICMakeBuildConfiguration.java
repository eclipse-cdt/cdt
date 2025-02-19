/*******************************************************************************
 * Copyright (c) 2025 Renesas Electronics Europe and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.cmake.core;

import org.eclipse.cdt.cmake.core.properties.CMakeGenerator;
import org.eclipse.cdt.cmake.core.properties.ICMakeGenerator;
import org.eclipse.cdt.cmake.core.properties.ICMakeProperties;
import org.eclipse.cdt.core.build.ICBuildConfiguration;

/**
 * Encapsulates the CMake specific {@link ICBuildConfiguration} for building CMake projects.
 * <p>
 * This interface defines the set of properties that can be exposed to a user via the UI. This
 * set of properties is converted to a {@link ICMakeBuildConfiguration} when CDT runs CMake's
 * build commands.
 *
 * @since 2.0
 */
public interface ICMakeBuildConfiguration {

	/**
	 * When true, {@link #getCMakeProperties()} should use defaults from {@link #getDefaultProperties()},
	 * otherwise uses values from {@link #getProperties()}
	 */
	public static final String CMAKE_USE_DEFAULT_CMAKE_SETTINGS = "cmake.use.default.cmake.settings"; //$NON-NLS-1$
	public static final String CMAKE_USE_DEFAULT_CMAKE_SETTINGS_DEFAULT = "true"; //$NON-NLS-1$

	/**
	 * The generator to use, typically one of {@link CMakeGenerator}, or if not present in {@link CMakeGenerator} an
	 * an custom one will be instantiated. Extenders can customize the generator by overriding {@link #getCMakeProperties()}
	 * and create an {@link ICMakeGenerator} to assign to {@link ICMakeProperties#setGenerator(ICMakeGenerator)}
	 */
	public static final String CMAKE_GENERATOR = "cmake.generator"; //$NON-NLS-1$
	public static final String CMAKE_GENERATOR_DEFAULT = "Ninja"; //$NON-NLS-1$

	/**
	 * Additional arguments to pass to CMake when running the generator stage.
	 */
	public static final String CMAKE_ARGUMENTS = "cmake.arguments"; //$NON-NLS-1$
	public static final String CMAKE_ARGUMENTS_DEFAULT = ""; //$NON-NLS-1$

	/**
	 * Custom environment to use when launching CMake
	 */
	public static final String CMAKE_ENV = "cmake.environment"; //$NON-NLS-1$

	/**
	 * Name of the CMake executable.
	 */
	public static final String CMAKE_BUILD_COMMAND = "cmake.command.build"; //$NON-NLS-1$
	public static final String CMAKE_BUILD_COMMAND_DEFAULT = "cmake"; //$NON-NLS-1$

	/**
	 * Name of the default target to pass to CMake's build {@code --target} when building.
	 */
	public static final String CMAKE_ALL_TARGET = "cmake.target.all"; //$NON-NLS-1$
	public static final String CMAKE_ALL_TARGET_DEFAULT = "all"; //$NON-NLS-1$

	/**
	 * Name of the default target to pass to CMake's build {@code --target} when cleaning.
	 */
	public static final String CMAKE_CLEAN_TARGET = "cmake.target.clean"; //$NON-NLS-1$
	public static final String CMAKE_CLEAN_TARGET_DEFAULT = "clean"; //$NON-NLS-1$

	/**
	 * Name of the build type set by user. Default of this value will be set based on Launch Mode
	 * (i.e. Debug for Debug launch mode, Release for Run and other launch modes)
	 */
	public static final String CMAKE_BUILD_TYPE = "cmake.build.type"; //$NON-NLS-1$

	/**
	 * Converts the {@link ICBuildConfiguration}'s properties, using the keys defined above
	 * into an {@link ICMakeProperties} that configures the CMake build processes.
	 * @return A ICMakeProperties object. Must not be null.
	 */
	ICMakeProperties getCMakeProperties();
}
