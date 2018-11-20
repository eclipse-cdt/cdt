/*******************************************************************************
 * Copyright (c) 2002, 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     John Camelon (IBM Rational Software) - Initial API and implementation
 *     Mike Kucera (IBM)- convert to Java 5 enum
 *******************************************************************************/
package org.eclipse.cdt.core.parser;

/**
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class ParseError extends Error {

	private static final long serialVersionUID = -3626877473345356953L;

	private final ParseErrorKind errorKind;

	public enum ParseErrorKind {
		// the method called is not implemented in this particular implementation
		METHOD_NOT_IMPLEMENTED,

		// offset specified is within a section of code #if'd out by the preprocessor
		// semantic context cannot be provided in this case
		OFFSETDUPLE_UNREACHABLE,

		// offset range specified is not a valid identifier or qualified name
		// semantic context cannot be provided in this case
		OFFSET_RANGE_NOT_NAME,

		TIMEOUT_OR_CANCELLED,

		/**
		 * The user preference for
		 * {@link org.eclipse.cdt.core.CCorePreferenceConstants#SCALABILITY_LIMIT_TOKENS_PER_TU} is
		 * enabled and more than
		 * {@link org.eclipse.cdt.core.CCorePreferenceConstants#SCALABILITY_MAXIMUM_TOKENS} tokens
		 * were created while parsing a single translation unit.
		 * @since 5.7
		 */
		TOO_MANY_TOKENS
	}

	public ParseErrorKind getErrorKind() {
		return errorKind;
	}

	public ParseError(ParseErrorKind kind) {
		errorKind = kind;
	}

	/**
	 * @since 5.7
	 */
	public ParseError(String message, ParseErrorKind kind) {
		super(message);
		errorKind = kind;
	}
}
