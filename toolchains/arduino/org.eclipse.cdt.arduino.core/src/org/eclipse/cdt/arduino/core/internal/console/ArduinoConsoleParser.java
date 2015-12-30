/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.arduino.core.internal.console;

public abstract class ArduinoConsoleParser {

	private final String pattern;
	private final int flags;
	private final String lineQualifier;

	protected ArduinoConsoleParser(String pattern, int flags, String lineQualifier) {
		this.pattern = pattern;
		this.flags = flags;
		this.lineQualifier = lineQualifier;
	}

	/**
	 * Returns the pattern to be used for matching. The pattern is a string
	 * representing a regular expression.
	 * 
	 * @return the regular expression to be used for matching
	 */
	public String getPattern() {
		return pattern;
	}

	/**
	 * Returns the flags to use when compiling this pattern match listener's
	 * regular expression, as defined by by
	 * <code>Pattern.compile(String regex, int flags)</code>
	 * 
	 * @return the flags to use when compiling this pattern match listener's
	 *         regular expression
	 * @see java.util.regex.Pattern#compile(java.lang.String, int)
	 */
	public int getCompilerFlags() {
		return flags;
	}

	/**
	 * Returns a simple regular expression used to identify lines that may match
	 * this pattern matcher's complete pattern, or <code>null</code>. Use of
	 * this attribute can improve performance by disqualifying lines from the
	 * search. When a line is found containing a match for this expression, the
	 * line is searched from the beginning for this pattern matcher's complete
	 * pattern. Lines not containing this pattern are discarded.
	 * 
	 * @return a simple regular expression used to identify lines that may match
	 *         this pattern matcher's complete pattern, or <code>null</code>
	 */
	public String getLineQualifier() {
		return lineQualifier;
	}

}
