/*******************************************************************************
 * Copyright (c) 2017-2019 Martin Weber.
 *
 * Content is provided to you under the terms and conditions of the Eclipse Public License Version 2.0 "EPL".
 * A copy of the EPL is available at http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.cmake.is.core.participant;

/**
 * Parses a 'response file' tool argument and its content.
 *
 * @author Martin Weber
 */
public interface IResponseFileArglet {

	/**
	 * Detects whether the first argument on the given {@code argsLine} argument
	 * denotes a response file and parses that file.
	 *
	 * @param parserHandler the handler to parse the arguments in the
	 *                      response-file`s content
	 * @param argsLine      the arguments passed to the tool, as they appear in the
	 *                      build output. Implementers may safely assume that the
	 *                      specified value does not contain leading whitespace
	 *                      characters, but trailing WS may occur.
	 * @return the number of characters from {@code argsLine} that has been
	 *         processed. Return a value of {@code zero} or less, if this tool
	 *         argument parser cannot process the first argument from the input.
	 */
	int process(IParserHandler parserHandler, String argsLine);

}
