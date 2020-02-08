/*******************************************************************************
 * Copyright (c) 2019 Martin Weber.
 *
 * Content is provided to you under the terms and conditions of the Eclipse Public License Version 2.0 "EPL".
 * A copy of the EPL is available at http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.cmake.is.core.builtins;

import java.util.List;

/**
 * Specifies how built-in compiler macros and include path detection is handled
 * for a specific compiler.
 *
 * @author Martin Weber
 */
public interface IBuiltinsDetectionBehavior {
	/**
	 * Gets the compiler arguments that tell the compiler to output its built-in
	 * values for include search paths and predefined macros. For the GNU compilers,
	 * these are {@code -E -P -dM -Wp,-v}.
	 */
	List<String> getBuiltinsOutputEnablingArgs();

	/**
	 * Creates an object that parses the output from built-in detection.
	 */
	IBuiltinsOutputProcessor createCompilerOutputProcessor();

	/**
	 * Gets whether to suppress the error-message that is printed if the compiler
	 * process exited with a non-zero exit status code. Except for some special
	 * cases, most implementations, should return {@code false} here.
	 */
	boolean suppressErrormessage();

	/**
	 * Gets the filename extension for the input file. An empty input file will be
	 * created and its name will be given on the command-line when the compiler is
	 * invoked for built-ins detection.
	 *
	 * @param languageID the language ID
	 * @return the filename extension or {@code null} if no filename argument needs
	 *         to be given for built-ins detection
	 */
	String getInputFileExtension(String languageID);
}
