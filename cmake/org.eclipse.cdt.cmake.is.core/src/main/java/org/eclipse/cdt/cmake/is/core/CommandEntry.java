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

package org.eclipse.cdt.cmake.is.core;

/**
 * Represents a parsed command entry of a compile_commands.json file.
 * @author weber
 */
class CommandEntry {
	private String directory;
	private String command;
	private String file;

	/**
	 *  Gets the build directory as a String.
	 */
	public String getDirectory() {
		return directory;
	}

	/**
	 * Gets the command-line to compile the source file.
	 */
	public String getCommand() {
		return command;
	}

	/**
	 * Gets the source file path as a String.
	 */
	public String getFile() {
		return file;
	}
}
