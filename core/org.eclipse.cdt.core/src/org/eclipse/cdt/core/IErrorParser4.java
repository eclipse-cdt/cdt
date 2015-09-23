/*******************************************************************************
 * Copyright (c) 2015 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.core;

/**
 * Interface to provide flexibility for error parsers to specify whether to
 * parse stdout, stderr or both.
 *
 * @since 5.12
 */
public interface IErrorParser4 extends IErrorParser {
	/** Parse standard out stream */
	public static final int PARSE_STDOUT = 0x01;
	/** Parse standard error stream */
	public static final int PARSE_STDERR = 0x02;

	/**
	 * Specifies whether to parse stdout, stderr or both.
	 *
	 * @return combination of flags to specify whether to parse stdout, stderr
	 *         or both.
	 * @see #PARSE_STDOUT
	 * @see #PARSE_STDERR
	 */
	int getStreamType();
}
