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
package org.eclipse.cdt.qt.core.qmldir;

import java.io.InputStream;
import java.util.Scanner;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import org.eclipse.cdt.internal.qt.core.location.Position;
import org.eclipse.cdt.internal.qt.core.location.SourceLocation;
import org.eclipse.cdt.qt.core.location.ISourceLocation;

/**
 * Converts an <code>InputStream</code> representing a qmldir file into a stream of tokens through successive calls to
 * <code>nextToken</code>. This lexer uses regular expressions to match its 16 valid token types:
 * <ul>
 * <li><b>COMMENT</b>: A single line comment that begins with '#'
 * <li><b>MODULE</b>: Keyword 'module'
 * <li><b>TYPEINFO</b>: The keyword 'typeinfo'
 * <li><b>SINGLETON</b>: The keyword 'singleton'
 * <li><b>INTERNAL</b>: The keyword 'internal'
 * <li><b>PLUGIN</b>: The keyword 'plugin'
 * <li><b>CLASSNAME</b>: The keyword 'classname'
 * <li><b>DEPENDS</b>: The keyword 'depends'
 * <li><b>DESIGNERSUPPORTED</b>: The keyword 'designersupported'
 * <li><b>WORD</b>: A group of characters that form an identifier, filename, or path
 * <li><b>DECIMAL</b>: A number of the form [0-9]+ '.' [0-9]+
 * <li><b>INTEGER</b>: An integer of the form [0-9]+
 * <li><b>WHITESPACE</b>: A group of whitespace characters (not including newlines)
 * <li><b>COMMAND_END</b>: A newline character
 * <li><b>UNKNOWN</b>: A group of characters that does not match any of the preceding tokens
 * <li><b>EOF</b>: End of File
 * </ul>
 */
public class QMLDirectoryLexer {
	/**
	 * A single matched token returned by a <code>QMLDirectoryLexer</code>. A <code>Token</code> stores information on how it was
	 * matched including the type of token, the exact text that was matched, and its position in the <code>InputStream</code> .
	 */
	public static class Token {
		private final TokenType tokType;
		private final String raw;
		private final ISourceLocation location;
		private final int start;
		private final int end;

		private Token(TokenType type, MatchResult match, int line, int lineStart) {
			this(type, match.group(), match.start(), match.end(), line, lineStart);
		}

		private Token(TokenType type, String raw, int start, int end, int line, int lineStart) {
			this.tokType = type;
			raw = raw.replaceAll("\n", "\\\\n"); //$NON-NLS-1$ //$NON-NLS-2$
			raw = raw.replaceAll("\r", "\\\\r"); //$NON-NLS-1$ //$NON-NLS-2$
			this.raw = raw;
			this.start = start;
			this.end = end;
			this.location = new SourceLocation(null, new Position(line, start - lineStart),
					new Position(line, end - lineStart));
		}

		/**
		 * Get the type of token that was matched.
		 *
		 * @return the type of token
		 */
		public TokenType getType() {
			return tokType;
		}

		/**
		 * Gets the raw text that this token was matched with.
		 *
		 * @return a String representing the matched text
		 */
		public String getText() {
			return raw;
		}

		/**
		 * Gets a more detailed description of this token's location in the <code>InputStream</code> than {@link Token#getStart()}
		 * and {@link Token#getEnd()}. This method allows the retrieval of line and column information in order to make output for
		 * syntax errors and the like more human-readable.
		 *
		 * @return the {@link ISourceLocation} representing this token's location in the <code>InputStream</code>
		 */
		public ISourceLocation getLocation() {
			return location;
		}

		/**
		 * Gets the zero-indexed offset indicating the start of this token in the <code>InputStream</code>.
		 *
		 * @return the token's start offset
		 */
		public int getStart() {
			return start;
		}

		/**
		 * Gets the zero-indexed offset indicating the end of this token in the <code>InputStream</code>.
		 *
		 * @return the token's end offset
		 */
		public int getEnd() {
			return end;
		}
	}

	/**
	 * An Enumeration encompassing the 16 possible types of tokens returned by a <code>QMLDirectoryLexer</code>.
	 *
	 * @see org.eclipse.cdt.qt.core.qmldir.QMLDirectoryLexer
	 */
	public static enum TokenType {
		COMMENT("#.*$"), //$NON-NLS-1$
		MODULE("module(?=\\s|$)"), //$NON-NLS-1$
		TYPEINFO("typeinfo(?=\\s|$)"), //$NON-NLS-1$
		SINGLETON("singleton(?=\\s|$)"), //$NON-NLS-1$
		INTERNAL("internal(?=\\s|$)"), //$NON-NLS-1$
		PLUGIN("plugin(?=\\s|$)"), //$NON-NLS-1$
		CLASSNAME("classname(?=\\s|$)"), //$NON-NLS-1$
		DEPENDS("depends(?=\\s|$)"), //$NON-NLS-1$
		DESIGNERSUPPORTED("designersupported(?=\\s|$)"), //$NON-NLS-1$
		WORD("[^0-9\\s][^\\s]*"), //$NON-NLS-1$
		DECIMAL("[0-9]+\\.[0-9]+"), //$NON-NLS-1$
		INTEGER("[0-9]+"), //$NON-NLS-1$
		WHITESPACE("\\h+"), //$NON-NLS-1$
		COMMAND_END("(?:\r\n)|\n"), //$NON-NLS-1$
		UNKNOWN(".+"), //$NON-NLS-1$
		EOF(null);

		private static Pattern pattern;

		private static Pattern patternForAllTerminals() {
			if (pattern == null) {
				String regex = ""; //$NON-NLS-1$
				TokenType[] tokens = TokenType.values();
				for (int i = 0; i < TokenType.values().length; i++) {
					TokenType tok = tokens[i];
					if (tok.regex != null) {
						if (i != 0) {
							regex += "|"; //$NON-NLS-1$
						}
						regex += "(" + tok.regex + ")"; //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
				pattern = Pattern.compile(regex, Pattern.MULTILINE);
			}
			return pattern;
		}

		private final String regex;

		private TokenType(String regex) {
			this.regex = regex;
		}
	}

	private Scanner input;
	private MatchResult lastMatch;
	private int currentLine;
	private int currentLineStart;

	/**
	 * Creates a new <code>QMLDirectoryLexer</code> without initializing any of the its internal state. A call to
	 * <code>setInput</code> is necessary to fully initialize the lexer before any calls to <code>nextToken</code>.
	 */
	public QMLDirectoryLexer() {
	}

	/**
	 * Prepares for lexical analysis by giving the lexer an <code>InputStream</code> to retrieve text from.
	 *
	 * @param input
	 *            the input to perform lexical analysis on
	 */
	public void setInput(InputStream input) {
		this.input = new Scanner(input);
		this.lastMatch = null;
		this.currentLine = 1;
		this.currentLineStart = 0;
	}

	/**
	 * Retrieves the next valid token from the <code>InputStream</code> given by <code>setInput</code>. This is a helper method to
	 * skip whitespace that is equivalent to <code>QMLDirectoryLexer.nextToken(true)</code>.
	 *
	 * @return the next token in the <code>InputStream</code>
	 * @throws IllegalArgumentException
	 *             if <code>setInput</code> has not been called
	 */
	public Token nextToken() throws IllegalArgumentException {
		return nextToken(true);
	}

	/**
	 * Retrieves the next valid token from the <code>InputStream</code> given by <code>setInput</code>. This method has the ability
	 * to skip over whitespace tokens by setting <code>skipWhitespace</code> to <code>true</code>.
	 *
	 * @param skipWhitespace
	 *            whether or not the lexer should skip whitespace tokens
	 * @return the next token in the <code>InputStream</code>
	 * @throws IllegalArgumentException
	 *             if <code>setInput</code> has not been called
	 */
	public Token nextToken(boolean skipWhitespace) throws IllegalArgumentException {
		if (input == null) {
			throw new IllegalArgumentException("Input cannot be null"); //$NON-NLS-1$
		}
		if (input.findWithinHorizon(TokenType.patternForAllTerminals(), 0) == null) {
			if (lastMatch != null) {
				return new Token(TokenType.EOF, "", lastMatch.end(), lastMatch.end(), currentLine, currentLineStart); //$NON-NLS-1$
			} else {
				return new Token(TokenType.EOF, "", 0, 0, 1, 0); //$NON-NLS-1$
			}
		} else {
			int groupNo = 1;
			for (TokenType t : TokenType.values()) {
				if (t.regex != null) {
					if (input.match().start(groupNo) != -1) {
						lastMatch = input.match();
						Token next = null;
						if (!(t.equals(TokenType.WHITESPACE) && skipWhitespace)) {
							next = new Token(t, input.match(), currentLine, currentLineStart);
						} else {
							next = nextToken(skipWhitespace);
						}
						if (t.equals(TokenType.COMMAND_END)) {
							// Advance the line number information
							currentLine++;
							currentLineStart = input.match().end();
						}
						return next;
					}
					groupNo++;
				}
			}
			return new Token(TokenType.UNKNOWN, input.match(), currentLine, currentLineStart);
		}
	}
}
