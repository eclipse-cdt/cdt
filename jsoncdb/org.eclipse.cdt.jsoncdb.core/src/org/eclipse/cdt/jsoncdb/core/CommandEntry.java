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

package org.eclipse.cdt.jsoncdb.core;

/**
 * Represents a parsed command entry of a compile_commands.json file.<br>
 * See the <a href="https://clang.llvm.org/docs/JSONCompilationDatabase.html">JSON Compilation Database Format Specification</a>.
 *
 * @author Martin Weber
 */
class CommandEntry {
	private String directory;
	private String command;
	private String[] arguments;
	private String file;

	/**
	 * Gets the working directory of the compilation the build directory as a String.<br>
	 * The specification states: All paths specified in the command or file fields must be either absolute or relative to this directory.
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
	 * Gets the command-line to compile the source file split up into arguments.<br>
	 * Either {@link #getCommand()} or {@link #getArguments()} will return a non-null value.
	 */
	public String[] getArguments() {
		return arguments;
	}

	/**
	 * Gets the source file path as a String.
	 */
	public String getFile() {
		return file;
	}
}
