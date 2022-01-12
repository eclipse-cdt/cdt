/*******************************************************************************
 * Copyright (c) 2008 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core;

/**
 * Interface to provide flexibility for error parsers to parse unprocessed build output
 *
 * @since 5.1
 */
public interface IErrorParser2 extends IErrorParser {
	/** Default behavior, lines are trimmed and cut by EOL and less or equal 1000 chars */
	public static final int NONE = 0x0;
	/** Do not trim output line */
	public static final int KEEP_UNTRIMMED = 0x01;
	/** Parser can process lines with unlimited length (default length is 1000) */
	public static final int KEEP_LONGLINES = 0x04;

	/**
	 * Defines how much output would be processed before calling {@link #processLine(String, ErrorParserManager)}
	 *
	 * @return combination of flags that describe parser expectations of input line
	 * @see #KEEP_UNTRIMMED
	 * @see #KEEP_LONGLINES
	 * */
	int getProcessLineBehaviour();
}
