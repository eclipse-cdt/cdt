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

package org.eclipse.cdt.cmake.is.core.participant;

/**
 * Gathers information about C preprocessor symbols and include paths collected
 * from a compiler command-line or by performing compiler built-ins detection.
 *
 * @author weber
 */
public interface IRawIndexerInfoCollector {

	/**
	 * Adds a preprocessor symbol (macro definition).
	 *
	 * @param name  the name of the preprocessor symbol
	 * @param value the symbol value or {@code null} or the empty string if the
	 *              symbol has no value
	 */
	void addDefine(String name, String value);

	/**
	 * Adds a preprocessor symbol cancellation (macro undefine) and cancels any
	 * previous definition of {@code name} that was added through
	 * {@link #addDefine(String, String)}.
	 *
	 * @param name the name of the preprocessor symbol to cancel
	 */
	void addUndefine(String name);

	/**
	 * Adds a preprocessor include path.
	 *
	 * @param path the name of the include directory
	 */
	void addIncludePath(String path);

	/**
	 * Adds the name of a file that will be pre-processed by the compiler before parsing the source-file in
	 * order to populate the preprocessor macro-dictionary.
	 *
	 * @param path the name of the file
	 */
	void addSystemIncludePath(String path);

	/**
	 * Adds the name of a file that will be pre-processed by the compiler as if
	 * an {@code #include "file"} directive appeared as the first line of the source file.
	 *
	 * @param path the name of the file
	 */
	void addMacroFile(String path);

	/**
	 * Adds a preprocessor system include path.
	 *
	 * @param path the name of the include directory
	 */
	void addIncludeFile(String path);
}
