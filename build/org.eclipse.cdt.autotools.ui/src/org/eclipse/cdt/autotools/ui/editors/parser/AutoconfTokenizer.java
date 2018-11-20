/*******************************************************************************
 * Copyright (c) 2008, 2016 Nokia Corporation.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Ed Swartz (Nokia) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.autotools.ui.editors.parser;

import org.eclipse.cdt.autotools.ui.editors.AutoconfEditorMessages;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

/**
 * This tokenizer traverses autotools-style text (m4 or configure.ac) to support the
 * autoconf parser.  It tracks the current context (m4 macro call or shell commands)
 * to detect appropriate tokens, and tracks the m4 current quote style as well.
 * <p>
 * In m4 mode, its primary purpose is to find word boundaries, detect comments and quoted
 * strings, and to find the macro punctuation tokens.  It will not interpret anything
 * (e.g. '$1' inside a macro) -- this is up to the parser.
 * <p>
 * In shell script mode, its primary purpose is to identify enough
 * tokens to get a general picture of the structure of source for use by the autoconf
 * parser.  This isn't intended to be used for full shell script parsing.  In fact,
 * aside from the known tokens and identifiers, only individual characters will be returned.
 * <p>
 * Both modes know about "words" or identifiers and use the same syntax to detect these.
 * It's expected that the parser will detect a word as a macro or possible macro and
 * switch the mode of the tokenizer to fit.  The parser should invoke "setM4Context(...)"
 * (and "unreadToken" if necessary) to switch modes.
 * @author eswartz
 *
 */
public class AutoconfTokenizer {

	private static final String UNTERMINATED_STRING = "UnterminatedString"; //$NON-NLS-1$
	public static final String UNMATCHED_RIGHT_QUOTE = "UnmatchedRightQuote"; //$NON-NLS-1$
	public static final String UNMATCHED_LEFT_QUOTE = "UnmatchedLeftQuote"; //$NON-NLS-1$
	public static final String UNMATCHED_CLOSE_COMMENT = "UnmatchedCloseComment"; //$NON-NLS-1$

	private IDocument document;
	private int offset;
	private String m4OpenQuote;
	private String m4CloseQuote;
	private String m4OpenComment;
	private String m4CloseComment;
	private char[] chars;
	private int startOffset;
	private boolean isM4Context;
	private Token eofToken;
	private IAutoconfErrorHandler errorHandler;

	/** Create a tokenizer for a document. */
	public AutoconfTokenizer(IDocument document, IAutoconfErrorHandler errorHandler) {
		if (document == null /* || macroDetector == null*/)
			throw new IllegalArgumentException();
		this.document = document;
		this.errorHandler = errorHandler;

		this.chars = document.get().toCharArray();
		this.offset = 0;

		this.eofToken = new Token(ITokenConstants.EOF, "", document, chars.length, 0);

		this.m4OpenQuote = "`"; //$NON-NLS-1$
		this.m4CloseQuote = "'"; //$NON-NLS-1$
		this.m4OpenComment = "#"; //$NON-NLS-1$
		this.m4CloseComment = "\n"; //$NON-NLS-1$
	}

	/**
	 * Tell whether the tokenizer considers itself to be in an m4 context.
	 * This determines what kinds of tokens it returns.
	 * @return
	 */
	public boolean isM4Context() {
		return isM4Context;
	}

	/**
	 * Switch the tokenizer into or out of m4 context.
	 * @return
	 */
	public void setM4Context(boolean flag) {
		isM4Context = flag;
	}

	/**
	 * Set the m4 quote delimiters
	 */
	public void setM4Quote(String open, String close) {
		this.m4OpenQuote = open;
		this.m4CloseQuote = close;
	}

	/**
	 * Set the m4 comment delimiters
	 */
	public void setM4Comment(String open, String close) {
		this.m4OpenComment = open;
		this.m4CloseComment = close;
	}

	/** Push back the given token.  This allows the tokenizer to restart from its start position,
	 * potentially in a different context. */
	public void unreadToken(Token token) {
		if (token.getLength() > 0 && offset == token.getOffset())
			throw new IllegalStateException();
		offset = token.getOffset();
	}

	/** Read the next token. Returns an EOF token at EOF. */
	public Token readToken() {
		if (offset >= chars.length)
			return eofToken;

		char ch = chars[offset];

		// skip whitespace (but not EOL)
		while (isWhitespace(ch)) {
			offset++;
			if (offset >= chars.length)
				return eofToken;
			ch = chars[offset];
		}

		// in shell mode, strip comments up to eol
		if (!isM4Context && ch == '#') {
			while (offset < chars.length) {
				ch = chars[offset];
				if (ch == '\n')
					break;
				offset++;
			}

			// keep inside doc if we didn't find that EOL
			if (offset >= chars.length)
				offset--;
		}

		startOffset = offset;
		StringBuilder buffer = new StringBuilder();

		// check EOL
		if (ch == '\r' || ch == '\n') {
			buffer.append(ch);
			offset++;
			if (ch == '\r' && offset < chars.length && chars[offset] == '\n') {
				buffer.append(chars[offset++]);
			}
			return makeToken(ITokenConstants.EOL, buffer.toString());
		}

		// TODO: this parser always uses fixed logic for identifier reading, ignoring m4's "changeword"
		if (isLeadIdentifierChar(ch)) {
			return parseWord(ch);
		}

		// check comments and quotes
		if (isM4Context) {
			if (lookAhead(m4OpenComment)) {
				boolean found = false;
				// keep reading until the close comment (these are NOT nested)
				while (offset < chars.length) {
					if (lookAhead(m4CloseComment)) {
						found = true;
						break;
					}
					offset++;
				}
				if (!found) {
					handleError(startOffset, offset, AutoconfEditorMessages.getFormattedString(UNMATCHED_CLOSE_COMMENT,
							m4CloseComment.equals("\n") ? "newline" : m4CloseComment)); //$NON-NLS-1$
				}
				return makeToken(ITokenConstants.M4_COMMENT);
			}

			if (lookAhead(m4OpenQuote)) {
				return parseQuote();
			}
		}

		// check shell punctuation
		if (!isM4Context) {
			if (ch == ';' && offset + 1 < chars.length && chars[offset + 1] == ';') {
				offset += 2;
				return makeToken(ITokenConstants.SH_CASE_CONDITION_END);
			}
			if (ch == '<' && offset + 1 < chars.length && chars[offset + 1] == '<') {
				offset += 2;
				if (offset < chars.length && chars[offset] == '-') {
					offset++;
					return makeToken(ITokenConstants.SH_HERE_DASH);
				} else {
					return makeToken(ITokenConstants.SH_HERE);
				}
			}
			switch (ch) {
			case '$':
				offset++;
				return makeToken(ITokenConstants.SH_DOLLAR);
			case '[':
				offset++;
				return makeToken(ITokenConstants.SH_LBRACKET);
			case ']':
				offset++;
				return makeToken(ITokenConstants.SH_RBRACKET);
			case '{':
				offset++;
				return makeToken(ITokenConstants.SH_LBRACE);
			case '}':
				offset++;
				return makeToken(ITokenConstants.SH_RBRACE);
			case '\'':
				return parseString(ITokenConstants.SH_STRING_SINGLE, ch);
			case '\"':
				return parseString(ITokenConstants.SH_STRING_DOUBLE, ch);
			case '`':
				return parseString(ITokenConstants.SH_STRING_BACKTICK, ch);
			}
		}

		// check common punctuation
		if (ch == ';') {
			offset++;
			return makeToken(ITokenConstants.SEMI);
		}
		if (ch == ',') {
			offset++;
			return makeToken(ITokenConstants.COMMA);
		}
		if (ch == '(') {
			offset++;
			return makeToken(ITokenConstants.LPAREN);
		}
		if (ch == ')') {
			offset++;
			return makeToken(ITokenConstants.RPAREN);
		}

		// unknown text
		offset++;
		return makeToken(ITokenConstants.TEXT);
	}

	private Token parseWord(char ch) {
		StringBuilder buffer = new StringBuilder();

		buffer.append(ch);
		offset++;
		do {
			if (offset >= chars.length)
				break;
			ch = chars[offset];
			if (!isIdentifierChar(ch))
				break;
			buffer.append(ch);
			offset++;
		} while (true);

		String text = buffer.toString();

		if (!isM4Context) {
			// detect sh tokens
			if ("case".equals(text))
				return makeToken(ITokenConstants.SH_CASE, text);
			if ("in".equals(text))
				return makeToken(ITokenConstants.SH_IN, text);
			if ("esac".equals(text))
				return makeToken(ITokenConstants.SH_ESAC, text);
			if ("while".equals(text))
				return makeToken(ITokenConstants.SH_WHILE, text);
			if ("select".equals(text))
				return makeToken(ITokenConstants.SH_SELECT, text);
			if ("until".equals(text))
				return makeToken(ITokenConstants.SH_UNTIL, text);
			if ("for".equals(text))
				return makeToken(ITokenConstants.SH_FOR, text);
			if ("do".equals(text))
				return makeToken(ITokenConstants.SH_DO, text);
			if ("done".equals(text))
				return makeToken(ITokenConstants.SH_DONE, text);
			if ("if".equals(text))
				return makeToken(ITokenConstants.SH_IF, text);
			if ("then".equals(text))
				return makeToken(ITokenConstants.SH_THEN, text);
			if ("else".equals(text))
				return makeToken(ITokenConstants.SH_ELSE, text);
			if ("elif".equals(text))
				return makeToken(ITokenConstants.SH_ELIF, text);
			if ("fi".equals(text))
				return makeToken(ITokenConstants.SH_FI, text);
		}

		// other identifier-looking word
		return makeToken(ITokenConstants.WORD, text);
	}

	private Token parseQuote() {
		// read text, honoring nested quotes, but don't put the outermost quotes in the token

		StringBuilder buffer = new StringBuilder();

		int quoteLevel = 1;
		// keep reading until the close quote
		while (offset < chars.length) {
			if (lookAhead(m4CloseQuote)) {
				quoteLevel--;
				if (quoteLevel == 0)
					break;
				buffer.append(m4CloseQuote);
			} else if (lookAhead(m4OpenQuote)) {
				buffer.append(m4OpenQuote);
				quoteLevel++;
			} else {
				buffer.append(chars[offset]);
				offset++;
			}
		}

		if (quoteLevel > 0) {
			handleError(startOffset, offset,
					AutoconfEditorMessages.getFormattedString(UNMATCHED_LEFT_QUOTE, m4CloseQuote));
		} else if (quoteLevel < 0) {
			handleError(startOffset, offset,
					AutoconfEditorMessages.getFormattedString(UNMATCHED_RIGHT_QUOTE, m4OpenQuote));
		}

		return makeToken(ITokenConstants.M4_STRING, buffer.toString());
	}

	private Token parseString(int type, char terminal) {
		startOffset = offset;
		offset++;

		StringBuilder buffer = new StringBuilder();

		char ch = 0;
		while (offset < chars.length) {
			ch = chars[offset++];
			if (ch == '\\') {
				if (offset < chars.length)
					buffer.append(chars[offset++]);
				else
					buffer.append(ch);
			} else if (ch == terminal) {
				break;
			} else {
				buffer.append(ch);
			}
		}

		if (ch != terminal) {
			handleError(startOffset, offset, AutoconfEditorMessages.getFormattedString(UNTERMINATED_STRING, "" + ch));
		}

		return makeToken(type, buffer.toString());
	}

	private void handleError(int start, int end, String message) {
		if (errorHandler != null) {
			int lineNumber = 0;
			int startColumn = 0;
			int endColumn = 0;
			try {
				lineNumber = document.getLineOfOffset(start);
				int lineOffs = document.getLineOffset(lineNumber);
				startColumn = start - lineOffs;
				endColumn = end - lineOffs;
			} catch (BadLocationException e) {
				// Don't care if we blow up trying to issue marker
			}
			errorHandler.handleError(new ParseException(message, start, end, lineNumber, startColumn, endColumn,
					IMarker.SEVERITY_ERROR));
		}
	}

	/**
	 * Look ahead for the given string.  If found, return true, and have
	 * offset updated.  Otherwise, return false with offset unchanged.
	 * @param keyword
	 * @param text
	 * @return
	 */
	private boolean lookAhead(String keyword) {
		int length = keyword.length();
		if (offset + length > chars.length) {
			return false;
		}
		for (int idx = 0; idx < length; idx++) {
			if (chars[offset + idx] != keyword.charAt(idx))
				return false;
		}
		offset += length;
		return true;
	}

	private boolean isWhitespace(char ch) {
		return ch == ' ' || ch == '\t' || ch == '\f';
	}

	private Token makeToken(int type) {
		return new Token(type, new String(chars, startOffset, offset - startOffset), document, startOffset,
				offset - startOffset);
	}

	private Token makeToken(int type, String text) {
		return new Token(type, text, document, startOffset, offset - startOffset);
	}

	private boolean isIdentifierChar(char ch) {
		return isLeadIdentifierChar(ch) || (ch >= '0' && ch <= '9');
	}

	private boolean isLeadIdentifierChar(char ch) {
		return (ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z') || ch == '_';
	}

	public Token peekToken() {
		Token token = readToken();
		unreadToken(token);
		return token;
	}
}
