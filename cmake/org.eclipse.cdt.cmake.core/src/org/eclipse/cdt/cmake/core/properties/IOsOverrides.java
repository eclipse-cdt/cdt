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
 * Properties that override/augment the generic properties when running under/building for a
 * specific OS.
 *
 * @author Martin Weber
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 1.4
 */
public interface IOsOverrides {

	/**
	 * Gets whether to use the default cmake command.
	 */
	boolean getUseDefaultCommand();

	/**
	 * Sets whether to use the default cmake command.
	 */
	void setUseDefaultCommand(boolean useDefaultCommand);

	/**
	 * Gets the cmake command. Has no effect if {@link #getUseDefaultCommand()} returns <code>false</code>.
	 */
	String getCommand();

	/**
	 * Sets the cmake command.
	 */
	void setCommand(String command);

	/**
	 * Gets the cmake buildscript generator.
	 */
	CMakeGenerator getGenerator();

	/**
	 * Sets the cmake build-script generator.
	 */
	void setGenerator(CMakeGenerator generator);

	/**
	 * Gets the extra arguments to pass on the cmake command-line.
	 *
	 * @return the extra arguments, never {@code null}
	 */
	String getExtraArguments();

	/**
	 * Sets the extra arguments to pass on the cmake command-line.
	 */
	void setExtraArguments(String string);

	/**
	 * Sets each property to its default value. This is intended for UIs that wish to implement a restore-defaults feature.
	 */
	void reset();
}