/*******************************************************************************
 * Copyright (c) 2017 Martin Weber.
 *
 * Content is provided to you under the terms and conditions of the Eclipse Public License Version 2.0 "EPL".
 * A copy of the EPL is available at http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.cmake.is.core.participant;

import org.eclipse.core.runtime.IPath;

/**
 * Handles parsing of command-line arguments.
 *
 * @author Martin Weber
 */
public interface IParserHandler {

	/**
	 * Parses the given String with the first parser that can handle the first
	 * argument on the command-line.
	 *
	 * @param args the command line arguments to process
	 */
	void parseArguments(String args);

	/**
	 * Gets the current working directory of the compiler at the time of its
	 * invocation.
	 */
	IPath getCompilerWorkingDirectory();
}
