/*******************************************************************************
 * Copyright (c) 2015-2020 Martin Weber.
 *
 * Content is provided to you under the terms and conditions of the Eclipse Public License Version 2.0 "EPL".
 * A copy of the EPL is available at http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.cmake.is.core.participant;

import org.eclipse.core.runtime.IPath;

/**
 * Converts tool arguments into LanguageSettings objects and compiler
 * command-line arguments that affect built-in-settings detection.
 *
 * @author Martin Weber
 */
public interface IArglet {

	/**
	 * Parses the next command-line argument and extracts all detected
	 * LanguageSettings objects.
	 *
	 * @param resultCollector the buffer that receives the parsed command-line
	 *                        arguments
	 * @param cwd             the current working directory of the compiler at its
	 *                        invocation
	 * @param argsLine        the arguments passed to the tool, as they appear in
	 *                        the build output. Implementers may safely assume that
	 *                        the specified value does not contain leading
	 *                        whitespace characters, but trailing WS may occur.
	 * @return the number of characters from {@code argsLine} that has been
	 *         processed. Return a value of {@code zero} or less, if this tool
	 *         argument parser cannot process the first argument from the input.
	 */
	int processArgument(IArgumentCollector resultCollector, IPath cwd, String argsLine);

	/**
	 * Gathers the results of argument parsing.
	 *
	 * @author Martin Weber
	 */
	interface IArgumentCollector extends IRawIndexerInfoCollector {
		/**
		 * Adds a compiler argument that affects built-in detection. For the GNU
		 * compilers, these are options like {@code --sysroot} and options that specify
		 * the language's standard ({@code -std=c++17}.
		 */
		void addBuiltinDetectionArgument(String argument);
	}
}