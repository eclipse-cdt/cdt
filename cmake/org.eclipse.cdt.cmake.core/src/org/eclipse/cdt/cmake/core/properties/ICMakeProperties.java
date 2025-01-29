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

import java.util.List;

/**
 * Holds project Properties for cmake.
 *
 * @author Martin Weber
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 * @since 1.4
 */
public interface ICMakeProperties {
	/**
	 * Gets the cmake command. Has no effect if {@link #getUseDefaultCommand()} returns <code>false</code>.
	 * @since 2.0
	 */
	String getCommand();

	/**
	 * Sets the cmake command.
	 * @since 2.0
	 */
	void setCommand(String command);

	/**
	 * Gets the cmake buildscript generator.
	 * @since 2.0
	 */
	ICMakeGenerator getGenerator();

	/**
	 * Sets the cmake build-script generator.
	 * @since 2.0
	 */
	void setGenerator(ICMakeGenerator generator);

	/**
	 * {@code -Wno-dev}
	 */
	boolean isWarnNoDev();

	/**
	 * {@code -Wno-dev}
	 */
	void setWarnNoDev(boolean warnNoDev);

	/**
	 * {@code --debug-trycompile}
	 */
	boolean isDebugTryCompile();

	/**
	 * {@code --debug-trycompile}
	 */
	void setDebugTryCompile(boolean debugTryCompile);

	/**
	 * {@code --debug-output}
	 */
	boolean isDebugOutput();

	/**
	 * {@code --debug-output}
	 */
	void setDebugOutput(boolean debugOutput);

	/**
	 * {@code --trace}
	 */
	boolean isTrace();

	/**
	 * {@code --trace}
	 */
	void setTrace(boolean trace);

	/**
	 * {@code --warn-uninitialized}
	 * @since 2.0
	 */
	boolean isWarnUninitialized();

	/**
	 * {@code --warn-uninitialized}
	 * @since 2.0
	 */
	void setWarnUninitialized(boolean warnUninitialized);

	/**
	 * {@code --warn-unused-vars}
	 * @since 2.0
	 */
	boolean isWarnUnusedVars();

	/**
	 * {@code --warn-unused-vars}
	 * @since 2.0
	 */
	void setWarnUnusedVars(boolean warnUnused);

	/** Gets the build type ({@code Debug}, {@code Release}, ...). The returned value is passed to cmake
	 * as the {@code CMAKE_BUILD_TYPE} symbol on the command-line.
	 *
	 * @return the build type String. If <code>null</code> or {@link String#isBlank() blank}, no
	 * {@code CMAKE_BUILD_TYPE} symbol argument will be given to cmake, causing it to use its
	 * default build-type.
	 */
	String getBuildType();

	/** Sets the build type.
	 * @param buildType the build type to set. May be <code>null</code> or {@link String#isBlank() blank}.
	 * @see #getBuildType()
	 */
	void setBuildType(String buildType);

	/**
	 * Gets the list of extra arguments to pass on the cmake command-line.
	 *
	 * @return a unmodifiable list, never {@code null}
	 *
	 */
	List<String> getExtraArguments();

	/**
	 * Sets the list of extra arguments to pass on the cmake command-line.
	 */
	void setExtraArguments(List<String> extraArguments);

	/**
	 * Gets the name of the file that is used to pre-populate the cmake cache.
	 * {@code -C}
	 *
	 * @return the file name to set. If <code>null</code> or {@link String#isBlank() blank}, the cmake cache shall not be
	 *         pre-populated.
	 */
	String getCacheFile();

	/**
	 * Sets the name of the file that is used to pre-populate the cmake cache.
	 * {@code -C}
	 *
	 * @param cacheFile
	 *          the file name.  May be <code>null</code> or {@link String#isBlank() blank}.
	 * @see #getCacheFile()
	 */
	void setCacheFile(String cacheFile);

	/** Gets whether to clear the cmake-cache before build. If set to <code>true</code>, this will force to run cmake
	 * prior to each build.
	 */
	boolean isClearCache();

	/** Sets whether to clear the cmake-cache before build.
	 */
	void setClearCache(boolean clearCache);

	/**
	 * The target to pass to {@code --target} CMake command line option, used when user asks to clean a project.
	 * @since 2.0
	 */
	String getCleanTarget();

	/**
	 * @param cleanTarget The target to pass to {@code --target} CMake command line option, used when user asks to clean a project.
	 * @since 2.0
	 */
	void setCleanTarget(String cleanTarget);

	/**
	 * The target to pass to {@code --target} CMake command line option, used when user asks to build a project.
	 * @since 2.0
	 */
	String getAllTarget();

	/**
	 * @param allTarget The target to pass to {@code --target} CMake command line option, used when user asks to build a project.
	 * @since 2.0
	 */
	void setAllTarget(String allTarget);

}