/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.qt.ui.pro.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

/**
 * Contains all information about a variable's representation in a Qt Project (.pro) File. This includes information about offsets,
 * lengths, and textual representation of various components of a variable declaration such as its:
 * <ul>
 * <li>Name, such as "SOURCES"</li>
 * <li>Assignment operator (= or +=)</li>
 * <li>Values for a particular line</li>
 * <li>Comments for a particular line</li>
 * <li>Line feeds</li>
 * <li>Line escapes (\)</li>
 * </ul>
 * Also contains the static method <code>findNextVariable(Scanner)</code> to perform the regular expressions lookup of the next
 * variable in a document.
 */
public class QtProjectVariable {
	private static final Pattern REGEX = Pattern.compile(
			"(?m)^\\h*((?:[_a-zA-Z][_a-zA-Z0-9]*\\.)*[_a-zA-Z][_a-zA-Z0-9]*)\\h*(=|\\+=|-=|\\*=)\\h*([^#\\v]*?)\\h*((?:(\\\\)\\h*)?(#[^\\v]*)?$)"); //$NON-NLS-1$
	private static final Pattern LINE_ESCAPE_REGEX = Pattern
			.compile("(?m)^(\\h*)([^#\\v]*?)\\h*((?:(\\\\)\\h*)?(#[^\\v]*)?$)"); //$NON-NLS-1$

	private static final int GROUP_VAR_NAME = 1;
	private static final int GROUP_VAR_ASSIGNMENT = 2;
	private static final int GROUP_VAR_CONTENTS = 3;
	private static final int GROUP_VAR_TERMINATOR = 4;
	private static final int GROUP_VAR_LINE_ESCAPE = 5;
	private static final int GROUP_VAR_COMMENT = 6;

	private static final int GROUP_LINE_INDENT = 1;
	private static final int GROUP_LINE_CONTENTS = 2;
	private static final int GROUP_LINE_TERMINATOR = 3;
	private static final int GROUP_LINE_LINE_ESCAPE = 4;
	private static final int GROUP_LINE_COMMENT = 5;

	/**
	 * Finds the next Qt Project Variable within a String using the given Scanner. If there are no variables to be found, this
	 * method will return <code>null</code>.
	 *
	 * @param scanner
	 *            the scanner to use for regular expressions matching
	 * @return the next variable or <code>null</code> if none
	 */
	public static QtProjectVariable findNextVariable(Scanner scanner) {
		List<MatchResult> matchResults = new ArrayList<>();

		// Find the start of a variable declaration
		String match = scanner.findWithinHorizon(REGEX, 0);
		if (match == null) {
			return null;
		}

		// Get subsequent lines if the previous one ends with '\'
		MatchResult matchResult = scanner.match();
		matchResults.add(matchResult);
		if (matchResult.group(QtProjectVariable.GROUP_VAR_TERMINATOR).startsWith("\\")) { //$NON-NLS-1$
			do {
				match = scanner.findWithinHorizon(LINE_ESCAPE_REGEX, 0);
				if (match == null) {
					// This means that we have a newline escape where another line doesn't exist
					break;
				}

				matchResult = scanner.match();
				matchResults.add(matchResult);
			} while (matchResult.group(QtProjectVariable.GROUP_LINE_TERMINATOR).startsWith("\\")); //$NON-NLS-1$
		}
		return new QtProjectVariable(matchResults);
	}

	private final int startOffset;
	private final int endOffset;
	private final String text;

	private final List<MatchResult> matchResults;

	/**
	 * Constructs a project file variable from a list of match results obtained from a <code>Scanner</code>. This constructor is
	 * only intended to be called from within the static method <code>findNextVariable(Scanner)</code>.
	 *
	 * @param matches
	 *            list of <code>MatchResult</code>
	 */
	private QtProjectVariable(List<MatchResult> matches) {
		this.startOffset = matches.get(0).start();
		this.endOffset = matches.get(matches.size() - 1).end();
		this.matchResults = matches;

		StringBuilder sb = new StringBuilder();
		for (MatchResult m : matches) {
			sb.append(m.group());
		}
		this.text = sb.toString();
	}

	/**
	 * Gets the offset of this variable relative to the start of its containing document.
	 *
	 * @return the offset of this variable
	 */
	public int getOffset() {
		return startOffset;
	}

	/**
	 * Gets the length of this variable as it appears in its containing document.
	 *
	 * @return the total length of this variable
	 */
	public int getLength() {
		return endOffset - startOffset;
	}

	/**
	 * Gets the name of this variable as it appears in the document. For example, the <code>"SOURCES"</code> variable.
	 *
	 * @return the name of this variable
	 */
	public String getName() {
		return matchResults.get(0).group(GROUP_VAR_NAME);
	}

	/**
	 * the assignment operator of this variable (<code>+=</code> or <code>"="</code>)
	 *
	 * @return the assignment operator
	 */
	public String getAssignmentOperator() {
		return matchResults.get(0).group(GROUP_VAR_ASSIGNMENT);
	}

	/**
	 * Returns a list of value(s) assigned to this variable. Each entry in the list represents a new line.
	 *
	 * @return a List containing all of the value(s) assigned to this variable
	 */
	public List<String> getValues() {
		List<String> values = new ArrayList<>();
		values.add(matchResults.get(0).group(GROUP_VAR_CONTENTS));
		for (int i = 1; i < matchResults.size(); i++) {
			values.add(matchResults.get(i).group(GROUP_LINE_CONTENTS));
		}
		return values;
	}

	/**
	 * Returns the indentation of the given line as a String. Mainly used by the QtProjectFileWriter to write back to the Document.
	 *
	 * @param line
	 *            the line number to check
	 * @return a <code>String</code> representing the indentation of the given line
	 */
	public String getIndentString(int line) {
		MatchResult match = matchResults.get(line);
		if (line == 0) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < match.start(GROUP_VAR_CONTENTS) - match.start(); i++) {
				sb.append(' ');
			}
			return sb.toString();
		}
		return match.group(GROUP_LINE_INDENT);
	}

	/**
	 * Retrieves the offset of the value portion of a given line relative to the start of its containing document.
	 *
	 * @param line
	 *            the line to check
	 * @return the offset of the value
	 */
	public int getValueOffsetForLine(int line) {
		if (line == 0) {
			return matchResults.get(line).start(GROUP_VAR_CONTENTS);
		}
		return matchResults.get(line).start(GROUP_LINE_CONTENTS);
	}

	/**
	 * Retrieves a String representing the value at a specific line of this variable.
	 *
	 * @param line
	 *            the line to check
	 * @return the value
	 */
	public String getValueForLine(int line) {
		if (line == 0) {
			return matchResults.get(line).group(GROUP_VAR_CONTENTS);
		}
		return matchResults.get(line).group(GROUP_LINE_CONTENTS);
	}

	/**
	 * Returns the ideal offset in the containing document at which a line escape can be inserted.
	 *
	 * @param line
	 *            the line to check
	 * @return the ideal location for a line escape
	 */
	public int getLineEscapeReplacementOffset(int line) {
		if (line == 0) {
			return matchResults.get(line).end(GROUP_VAR_CONTENTS);
		}
		return matchResults.get(line).end(GROUP_LINE_CONTENTS);
	}

	/**
	 * Returns the ideal String for the line escape character. This is mostly for spacing requirements and should be used in tandem
	 * with the method <code>getLineEscapeReplacementOffset</code>.
	 *
	 * @param line
	 *            the line to check
	 * @return the ideal String for the line escape character
	 */
	public String getLineEscapeReplacementString(int line) {
		int commentOffset = -1;
		int contentsOffset = -1;
		if (line == 0) {
			commentOffset = matchResults.get(line).start(GROUP_VAR_COMMENT);
			contentsOffset = matchResults.get(line).end(GROUP_VAR_CONTENTS);
		} else {
			commentOffset = matchResults.get(line).start(GROUP_LINE_COMMENT);
			contentsOffset = matchResults.get(line).end(GROUP_LINE_CONTENTS);
		}

		if (commentOffset > 0) {
			if (commentOffset - contentsOffset == 0) {
				return " \\ "; //$NON-NLS-1$
			}
		}
		return " \\"; //$NON-NLS-1$
	}

	/**
	 * Retrieves the offset of the line escape for a given line relative to its containing document. This method takes into account
	 * spacing and should be used to determine how to best remove a line escape character from a given line.
	 *
	 * @param line
	 *            the line to check
	 * @return the offset of the line escape character
	 */
	public int getLineEscapeOffset(int line) {
		if (line == 0) {
			return matchResults.get(line).end(GROUP_VAR_CONTENTS);
		}
		return matchResults.get(line).end(GROUP_LINE_CONTENTS);
	}

	/**
	 * Get the end position relative to the start of the containing document that contains the line escape character of the given
	 * line. This is used for removal of the line escape character and takes into account the spacing of the line.
	 *
	 * @param line
	 *            the line to check
	 * @return the end position of the line escape character
	 */
	public int getLineEscapeEnd(int line) {
		int end = -1;
		if (line == 0) {
			end = matchResults.get(line).end(GROUP_VAR_LINE_ESCAPE);
		} else {
			end = matchResults.get(line).end(GROUP_LINE_LINE_ESCAPE);
		}

		if (end > 0) {
			return end;
		}

		if (line == 0) {
			return matchResults.get(line).end(GROUP_VAR_TERMINATOR);
		}
		return matchResults.get(line).end(GROUP_LINE_TERMINATOR);
	}

	/**
	 * Gets the end position of this variable relative to the containing document.
	 *
	 * @return the end position of this variable
	 */
	public int getEndOffset() {
		return matchResults.get(matchResults.size() - 1).end();
	}

	/**
	 * Retrieves the full text of this variable as it appears in the document.
	 *
	 * @return the full String of this variable as it appears in the document
	 */
	public String getText() {
		return text;
	}

	/**
	 * Gets the total number of lines in this variable declaration.
	 *
	 * @return the total number of lines
	 */
	public int getNumberOfLines() {
		return matchResults.size();
	}

	/**
	 * Retrieves a String representing the given line as it appears in the document.
	 *
	 * @param line
	 *            the line to retrieve
	 * @return a String representing the line
	 */
	public String getLine(int line) {
		return matchResults.get(line).group();
	}

	/**
	 * Retrieves the offset of the given line relative to its containing document.
	 *
	 * @param line
	 *            the line to retrieve
	 * @return the line's offset in the document
	 */
	public int getLineOffset(int line) {
		return matchResults.get(line).start();
	}

	/**
	 * Returns the line at which the specified value appears. This method checks the whole line for the value and will not match a
	 * subset of that String. This is equivalent to calling <code>getValueIndex(value,false)</code>.
	 *
	 * @param value
	 *            the value to search for
	 * @return the line that the value appears on or -1 if it doesn't exist
	 */
	public int getValueIndex(String value) {
		return getValueIndex(value, false);
	}

	/**
	 * Returns the line at which the specified value appears. This method checks the whole line for the value and will not match a
	 * subset of that String. If <code>ignoreCase</code> is <code>false</code>, this method searches for the value using
	 * <code>equalsIgnoreCase</code> instead of <code>equals</code>.
	 *
	 * @param value
	 *            the value to search for
	 * @param ignoreCase
	 *            whether or not the value is case-sensitive
	 * @return the line that the value appears on or -1 if it doesn't exist
	 */
	public int getValueIndex(String value, boolean ignoreCase) {
		int line = 0;
		for (String val : getValues()) {
			if (ignoreCase) {
				if (val.equalsIgnoreCase(value)) {
					return line;
				}
			} else {
				if (val.equals(value)) {
					return line;
				}
			}
			line++;
		}
		return -1;
	}

	/**
	 * Gets the offset of the end of a given line relative to its containing document.
	 *
	 * @param line
	 *            the line to check
	 * @return the offset of the end of the line
	 */
	public int getLineEnd(int line) {
		return matchResults.get(line).end();
	}
}