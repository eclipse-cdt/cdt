/*******************************************************************************
 * Copyright (c) 2016-2020 Martin Weber.
 *
 * Content is provided to you under the terms and conditions of the Eclipse Public License Version 2.0 "EPL".
 * A copy of the EPL is available at http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.cmake.is.core.participant;

import java.util.List;
import java.util.Optional;

import org.eclipse.cdt.cmake.is.core.participant.builtins.IBuiltinsDetectionBehavior;
import org.eclipse.core.runtime.IPath;

/**
 * Parses the command-line produced by a specific tool invocation and detects
 * LanguageSettings.
 *
 * @author Martin Weber
 */
public interface IToolCommandlineParser {
	/**
	 * Parses all arguments given to the tool.
	 *
	 * @param cwd  the current working directory of the compiler at the time of its
	 *             invocation
	 * @param args the command line arguments to process
	 *
	 * @throws NullPointerException if any of the arguments is {@code null}
	 */
	public IResult processArgs(IPath cwd, String args);

	/**
	 * Gets the {@code IBuiltinsDetectionBehavior} which specifies how built-in
	 * compiler macros and include path detection is handled for a specific
	 * compiler.
	 *
	 * @return the {@code IBuiltinsDetectionBehavior} or an empty {@code Optional}
	 *         if the compiler does not support built-in detection
	 */
	public Optional<IBuiltinsDetectionBehavior> getIBuiltinsDetectionBehavior();

	/**
	 * The result of processing a compiler command-line.
	 *
	 * @author Martin Weber
	 *
	 * @see IToolCommandlineParser#processArgs(IPath, String)
	 */
	interface IResult extends IRawIndexerInfo {
		/**
		 * Gets the compiler arguments from the command-line that affect built-in
		 * detection. For the GNU compilers, these are options like {@code --sysroot}
		 * and options that specify the language's standard ({@code -std=c++17}.
		 */
		List<String> getBuiltinDetectionArgs();
	} // IResult
}