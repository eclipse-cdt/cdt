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
	 */
	boolean isWarnUnitialized();

	/**
	 * {@code --warn-uninitialized}
	 */
	void setWarnUnitialized(boolean warnUnitialized);

	/**
	 * {@code --warn-unused-vars}
	 */
	boolean isWarnUnused();

	/**
	 * {@code --warn-unused-vars}
	 */
	void setWarnUnused(boolean warnUnused);

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
	 * Gets the extra arguments to pass on the cmake command-line.
	 *
	 * @return the extra arguments, never {@code null}
	 *
	 */
	String getExtraArguments();

	/**
	 * Sets the extra arguments to pass on the cmake command-line.
	 */
	void setExtraArguments(String string);

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
	 * Gets the override/augmenting properties to apply when the build runs on linux.
	 */
	IOsOverrides getLinuxOverrides();

	/**
	 * Gets the override/augmenting properties to apply when the build runs on windows.
	 */
	IOsOverrides getWindowsOverrides();

	/**
	 * Sets each property to its default value. This is intended for UIs that wish to implement a restore-defaults feature.<br>
	 *
	 * @param resetOsOverrides
	 * 		  whether to also reset the OS-specific overrides ({@link #getLinuxOverrides()},
	 * 		  {@link #getWindowsOverrides()}). If the overrides are displayed in separate tabs in the UI, <code>false</code>
	 * 		  should be specified.
	 */
	void reset(boolean resetOsOverrides);
}