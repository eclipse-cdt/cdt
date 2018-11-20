/*******************************************************************************
 * Copyright (c) 2004, 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial implementation
 *     Anton Leherbauer - adding tokens for preprocessing directives
 *     Markus Schorn - classification of preprocessing directives.
 *     John Dallaway - handle CRLF after single line comment (bug 442186)
 *******************************************************************************/
package org.eclipse.cdt.internal.formatter.scanner;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;

/**
 * A C/C++ lexical scanner, which does no preprocessing,
 * but tokenizes preprocessor directives, whitespace and comments.
 *
 * @since 4.0
 */
public class SimpleScanner {
	private static final int EOFCHAR = -1;
	protected static HashMap<String, Integer> fgKeywords = new HashMap<>();

	protected Token fCurrentToken;
	protected ScannerContext fContext;
	protected StringBuilder fTokenBuffer = new StringBuilder();
	private int fPreprocessorToken = 0;
	private boolean fReuseToken;
	private boolean fSplitPreprocessor;
	private final StringBuilder fUniversalCharBuffer = new StringBuilder();

	public SimpleScanner() {
		super();
	}

	public void setReuseToken(boolean val) {
		fReuseToken = val;
		if (val) {
			fCurrentToken = new Token(0, null);
		}
	}

	public void setSplitPreprocessor(boolean val) {
		fSplitPreprocessor = val;
	}

	protected void init(Reader reader, String filename) {
		fReuseToken = false;
		fSplitPreprocessor = true;
		fPreprocessorToken = 0;
		fContext = new ScannerContext().initialize(reader);
	}

	public SimpleScanner initialize(Reader reader, String filename) {
		init(reader, filename);
		return this;
	}

	public void cleanup() {
		fContext = null;
		fTokenBuffer = new StringBuilder();
		fCurrentToken = null;
	}

	private final void setCurrentToken(Token t) {
		fCurrentToken = t;
	}

	private final Token newToken(int t) {
		if (!fReuseToken) {
			setCurrentToken(new Token(t, fTokenBuffer.toString(), fContext));
		} else {
			fCurrentToken.set(t, fTokenBuffer.toString(), fContext);
		}
		return fCurrentToken;
	}

	private Token newPreprocessorToken() {
		if (fPreprocessorToken == 0) {
			fPreprocessorToken = categorizePreprocessor(fTokenBuffer);
		}
		return newToken(fPreprocessorToken);
	}

	private int categorizePreprocessor(StringBuilder text) {
		boolean skipHash = true;
		int i = 0;
		for (; i < text.length(); i++) {
			char c = text.charAt(i);
			if (!Character.isWhitespace(c)) {
				if (!skipHash) {
					break;
				}
				skipHash = false;
				if (c != '#') {
					break;
				}
			}
		}
		String innerText = text.substring(i);
		if (innerText.startsWith("include")) { //$NON-NLS-1$
			return Token.tPREPROCESSOR_INCLUDE;
		}
		if (innerText.startsWith("define")) { //$NON-NLS-1$
			return Token.tPREPROCESSOR_DEFINE;
		}
		if (innerText.startsWith("undef")) { //$NON-NLS-1$
			return Token.tPREPROCESSOR_DEFINE;
		}
		return Token.tPREPROCESSOR;
	}

	protected final int getChar() {
		return getChar(false);
	}

	private int getChar(boolean insideString) {
		int c = EOFCHAR;

		if (fContext.undoStackSize() != 0) {
			c = fContext.popUndo();
		} else {
			try {
				c = fContext.read();
			} catch (IOException e) {
				c = EOFCHAR;
			}
		}

		fTokenBuffer.append((char) c);

		if (!insideString && c == '\\') {
			c = getChar(false);
			if (c == '\r') {
				c = getChar(false);
				if (c == '\n') {
					c = getChar(false);
				}
			} else if (c == '\n') {
				c = getChar(false);
			} else if (c == 'U' || c == 'u') {
				fUniversalCharBuffer.setLength(0);
				fUniversalCharBuffer.append('\\').append((char) c);
				c = getUniversalCharacter();
			} else {
				ungetChar(c);
				c = '\\';
			}
		}

		return c;
	}

	private int getUniversalCharacter() {
		int unicode = 0;
		do {
			int c = getChar(true);
			int digit;
			switch (c) {
			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9':
				digit = c - '0';
				break;
			case 'a':
			case 'b':
			case 'c':
			case 'd':
			case 'e':
			case 'f':
				digit = c - 'a' + 10;
				break;
			case 'A':
			case 'B':
			case 'C':
			case 'D':
			case 'E':
			case 'F':
				digit = c - 'A' + 10;
				break;
			default:
				internalUngetChar(c);
				return unicode;
			}
			fUniversalCharBuffer.append((char) c);
			unicode <<= 4;
			unicode += digit;
		} while (true);
	}

	private void internalUngetChar(int c) {
		fTokenBuffer.deleteCharAt(fTokenBuffer.length() - 1);
		fContext.pushUndo(c);
	}

	protected void ungetChar(int c) {
		if (c < 256 || c == fTokenBuffer.charAt(fTokenBuffer.length() - 1)) {
			internalUngetChar(c);
		} else if (fUniversalCharBuffer.length() > 0) {
			char[] chs = fUniversalCharBuffer.toString().toCharArray();
			for (int i = chs.length - 1; i >= 0; --i) {
				internalUngetChar(chs[i]);
			}
		} else {
			internalUngetChar(c);
		}
	}

	public Token nextToken() {
		fTokenBuffer.setLength(0);

		boolean madeMistake = false;
		int c = getChar();

		while (c != EOFCHAR) {
			if (fPreprocessorToken != 0) {
				Token token = continuePPDirective(c);
				if (token != null) {
					return token;
				}
			}

			if ((c == ' ') || (c == '\r') || (c == '\t') || (c == '\n')) {
				do {
					c = getChar();
				} while ((c == ' ') || (c == '\r') || (c == '\t') || (c == '\n'));
				ungetChar(c);
				return newToken(Token.tWHITESPACE);
			} else if (c == '"') {
				matchStringLiteral();
				return newToken(Token.tSTRING);
			} else if (c == 'L' && !madeMistake) {
				int oldChar = c;
				c = getChar();
				if (c != '"') {
					// we have made a mistake
					ungetChar(c);
					c = oldChar;
					madeMistake = true;
					continue;
				}

				matchStringLiteral();
				return newToken(Token.tLSTRING);
			} else if (c == 'R' && !madeMistake) {
				int oldChar = c;
				c = getChar();
				if (c != '"') {
					// we have made a mistake
					ungetChar(c);
					c = oldChar;
					madeMistake = true;
					continue;
				}

				matchRawStringLiteral();
				return newToken(Token.tRSTRING);
			} else if (((c >= 'a') && (c <= 'z')) || ((c >= 'A') && (c <= 'Z')) || (c == '_')
					|| (c > 255 && Character.isUnicodeIdentifierStart(c))) {
				madeMistake = false;

				c = getChar();

				while (((c >= 'a') && (c <= 'z')) || ((c >= 'A') && (c <= 'Z')) || ((c >= '0') && (c <= '9'))
						|| (c == '_') || (c > 255 && Character.isUnicodeIdentifierPart(c))) {
					c = getChar();
				}

				ungetChar(c);

				String ident = fTokenBuffer.toString();

				Object tokenTypeObject;

				tokenTypeObject = fgKeywords.get(ident);

				int tokenType = Token.tIDENTIFIER;
				if (tokenTypeObject != null)
					tokenType = ((Integer) tokenTypeObject).intValue();

				return newToken(tokenType);
			} else if ((c >= '0') && (c <= '9') || c == '.') {
				boolean hex = false;
				boolean floatingPoint = c == '.';
				boolean firstCharZero = c == '0';

				c = getChar();

				if (firstCharZero && c == 'x') {
					hex = true;
					c = getChar();
				}

				int digits = 0;

				while ((c >= '0' && c <= '9') || (hex && ((c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F')))) {
					++digits;
					c = getChar();
				}

				if (!hex) {
					if (c == '*') {
						if (floatingPoint && digits == 0) {
							// encountered .*
							return newToken(Token.tDOTSTAR);
						}
					} else if (c == '.') {
						if (floatingPoint && digits == 0) {
							// encountered ..
							if ((c = getChar()) == '.') {
								return newToken(Token.tELIPSE);
							} else {
								ungetChar(c);
								ungetChar('.');
								return newToken(Token.tDOT);
							}
						}

						floatingPoint = true;
						c = getChar();
						while ((c >= '0' && c <= '9')) {
							++digits;
							c = getChar();
						}
					} else if (digits > 0 && (c == 'e' || c == 'E')) {
						floatingPoint = true;

						// exponent type for floating point
						c = getChar();

						// optional + or -
						if (c == '+' || c == '-') {
							c = getChar();
						}

						// digit sequence of exponent part
						while ((c >= '0' && c <= '9')) {
							c = getChar();
						}
					}
				}
				if (floatingPoint) {
					if (digits > 0) {
						//floating-suffix
						if (c == 'l' || c == 'L' || c == 'f' || c == 'F') {
							c = getChar();
						}
					} else {
						ungetChar(c);
						return newToken(Token.tDOT);
					}
				} else {
					//integer suffix
					if (c == 'u' || c == 'U') {
						c = getChar();
						if (c == 'l' || c == 'L')
							c = getChar();
					} else if (c == 'l' || c == 'L') {
						c = getChar();
						if (c == 'u' || c == 'U')
							c = getChar();
					}
				}

				ungetChar(c);

				int tokenType;
				String result = fTokenBuffer.toString();

				if (floatingPoint && result.equals(".")) //$NON-NLS-1$
					tokenType = Token.tDOT;
				else
					tokenType = floatingPoint ? Token.tFLOATINGPT : Token.tINTEGER;

				return newToken(tokenType);
			} else if (c == '#') {
				return matchPPDirective();
			} else {
				switch (c) {
				case '\'':
					matchCharLiteral();
					return newToken(Token.tCHAR);

				case ':':
					c = getChar();
					if (c == ':') {
						return newToken(Token.tCOLONCOLON);
					} else {
						ungetChar(c);
						return newToken(Token.tCOLON);
					}
				case ';':
					return newToken(Token.tSEMI);
				case ',':
					return newToken(Token.tCOMMA);
				case '?':
					return newToken(Token.tQUESTION);
				case '(':
					return newToken(Token.tLPAREN);
				case ')':
					return newToken(Token.tRPAREN);
				case '[':
					return newToken(Token.tLBRACKET);
				case ']':
					return newToken(Token.tRBRACKET);
				case '{':
					return newToken(Token.tLBRACE);
				case '}':
					return newToken(Token.tRBRACE);
				case '+':
					c = getChar();
					switch (c) {
					case '=':
						return newToken(Token.tPLUSASSIGN);
					case '+':
						return newToken(Token.tINCR);
					default:
						ungetChar(c);
						return newToken(Token.tPLUS);
					}
				case '-':
					c = getChar();
					switch (c) {
					case '=':
						return newToken(Token.tMINUSASSIGN);
					case '-':
						return newToken(Token.tDECR);
					case '>':
						c = getChar();
						switch (c) {
						case '*':
							return newToken(Token.tARROWSTAR);
						default:
							ungetChar(c);
							return newToken(Token.tARROW);
						}
					default:
						ungetChar(c);
						return newToken(Token.tMINUS);
					}
				case '*':
					c = getChar();
					switch (c) {
					case '=':
						return newToken(Token.tSTARASSIGN);
					default:
						ungetChar(c);
						return newToken(Token.tSTAR);
					}
				case '%':
					c = getChar();
					switch (c) {
					case '=':
						return newToken(Token.tMODASSIGN);
					default:
						ungetChar(c);
						return newToken(Token.tMOD);
					}
				case '^':
					c = getChar();
					switch (c) {
					case '=':
						return newToken(Token.tXORASSIGN);
					default:
						ungetChar(c);
						return newToken(Token.tXOR);
					}
				case '&':
					c = getChar();
					switch (c) {
					case '=':
						return newToken(Token.tAMPERASSIGN);
					case '&':
						return newToken(Token.tAND);
					default:
						ungetChar(c);
						return newToken(Token.tAMPER);
					}
				case '|':
					c = getChar();
					switch (c) {
					case '=':
						return newToken(Token.tBITORASSIGN);
					case '|':
						return newToken(Token.tOR);
					default:
						ungetChar(c);
						return newToken(Token.tBITOR);
					}
				case '~':
					return newToken(Token.tCOMPL);
				case '!':
					c = getChar();
					switch (c) {
					case '=':
						return newToken(Token.tNOTEQUAL);
					default:
						ungetChar(c);
						return newToken(Token.tNOT);
					}
				case '=':
					c = getChar();
					switch (c) {
					case '=':
						return newToken(Token.tEQUAL);
					default:
						ungetChar(c);
						return newToken(Token.tASSIGN);
					}
				case '<':
					c = getChar();
					switch (c) {
					case '<':
						c = getChar();
						switch (c) {
						case '=':
							return newToken(Token.tSHIFTLASSIGN);
						default:
							ungetChar(c);
							return newToken(Token.tSHIFTL);
						}
					case '=':
						return newToken(Token.tLTEQUAL);
					default:
						ungetChar(c);
						return newToken(Token.tLT);
					}
				case '>':
					c = getChar();
					switch (c) {
					case '>':
						c = getChar();
						switch (c) {
						case '=':
							return newToken(Token.tSHIFTRASSIGN);
						default:
							ungetChar(c);
							return newToken(Token.tSHIFTR);
						}
					case '=':
						return newToken(Token.tGTEQUAL);
					default:
						ungetChar(c);
						return newToken(Token.tGT);
					}
				case '.':
					c = getChar();
					switch (c) {
					case '.':
						c = getChar();
						switch (c) {
						case '.':
							return newToken(Token.tELIPSE);
						default:
							break;
						}
						break;
					case '*':
						return newToken(Token.tDOTSTAR);
					default:
						ungetChar(c);
						return newToken(Token.tDOT);
					}
					break;
				case '/':
					c = getChar();
					switch (c) {
					case '/': {
						matchSinglelineComment(true);
						return newToken(Token.tLINECOMMENT);
					}
					case '*': {
						matchMultilineComment();
						return newToken(Token.tBLOCKCOMMENT);
					}
					case '=':
						return newToken(Token.tDIVASSIGN);
					default:
						ungetChar(c);
						return newToken(Token.tDIV);
					}
				default:
					// Bad character
					return newToken(Token.tBADCHAR);
				}
				// throw EOF;
			}
		}

		// we're done
		// throw EOF;
		return null;
	}

	private void matchCharLiteral() {
		int c;
		c = getChar(true);
		int next = getChar(true);
		if (c == '\\') {
			if (next >= '0' && next <= '7') {
				do {
					next = getChar(true);
				} while (next >= '0' && next <= '7');
			} else if (next == 'x' || next == 'X' || next == 'u' || next == 'U') {
				do {
					next = getChar(true);
				} while ((next >= '0' && next <= '9') || (next >= 'a' && next <= 'f') || (next >= 'A' && next <= 'F'));
			} else {
				next = getChar(true);
			}
		}
		if (next != '\'') {
			ungetChar(next);
		}
	}

	private void matchStringLiteral() {
		// string
		boolean escaped = false;
		int c = getChar(true);

		LOOP: for (;;) {
			if (c == EOFCHAR)
				break;
			if (escaped) {
				escaped = false;
				int nc = getChar(true);
				if (c == '\r' && nc == '\n') {
					nc = getChar(true);
				}
				c = nc;
			} else {
				switch (c) {
				case '\\':
					escaped = true;
					break;
				case '"':
					break LOOP;
				case '\r':
				case '\n':
					// unterminated string constant
					ungetChar(c);
					break LOOP;
				}
				c = getChar(true);
			}
		}
	}

	private void matchRawStringLiteral() {
		// raw-string R"<delim-opt>(string)<delim-opt>";
		int c = getChar(false);
		StringBuilder delim = new StringBuilder(12);
		while (c != '(') {
			if (c == EOFCHAR) {
				return;
			}
			delim.append((char) c);
			c = getChar(false);
		}
		int delimLen = delim.length();
		c = getChar(false);
		LOOP: for (;;) {
			if (c == EOFCHAR)
				break;
			if (c == ')') {
				c = getChar(false);
				int idx = 0;
				while (idx < delimLen) {
					if (c != delim.charAt(idx)) {
						continue LOOP;
					}
					++idx;
					c = getChar(false);
				}
				if (c == '"')
					break;
			}
			c = getChar(false);
		}
	}

	/**
	 * Matches a preprocessor directive.
	 *
	 * @return a preprocessor token
	 */
	private Token matchPPDirective() {
		if (!fSplitPreprocessor) {
			getRestOfPreprocessorLine();
			return newToken(Token.tPREPROCESSOR);
		}
		return continuePPDirective(getChar());
	}

	private Token continuePPDirective(int c) {
		boolean done = false;
		while (!done) {
			switch (c) {
			case '\'':
				if (fTokenBuffer.length() > 1) {
					if (fPreprocessorToken == 0) {
						fPreprocessorToken = categorizePreprocessor(fTokenBuffer);
					}
					ungetChar(c);
					return newPreprocessorToken();
				}
				matchCharLiteral();
				return newToken(Token.tCHAR);

			case '"':
				if (fTokenBuffer.length() > 1) {
					if (fPreprocessorToken == 0) {
						fPreprocessorToken = categorizePreprocessor(fTokenBuffer);
					}
					if (fPreprocessorToken == Token.tPREPROCESSOR_INCLUDE) {
						matchStringLiteral();
						c = getChar();
						break;
					} else {
						ungetChar(c);
						return newPreprocessorToken();
					}
				}
				matchStringLiteral();
				return newToken(Token.tSTRING);
			case '/': {
				int next = getChar();
				if (next == '/') {
					Token result = null;
					if (fTokenBuffer.length() > 2) {
						ungetChar(next);
						ungetChar(c);
						result = newPreprocessorToken();
					} else {
						matchSinglelineComment(false);
						result = newToken(Token.tLINECOMMENT);
					}
					fPreprocessorToken = 0;
					return result;
				}
				if (next == '*') {
					if (fTokenBuffer.length() > 2) {
						ungetChar(next);
						ungetChar(c);
						return newPreprocessorToken();
					}
					// multiline comment
					if (matchMultilineComment()) {
						fPreprocessorToken = 0;
					}
					return newToken(Token.tBLOCKCOMMENT);
				}
				c = next;
				break;
			}
			case '\n':
			case '\r':
			case EOFCHAR:
				done = true;
				break;
			default:
				c = getChar();
				break;
			}
		}
		ungetChar(c);
		Token result = null;
		if (fTokenBuffer.length() > 0) {
			result = newPreprocessorToken();
		}
		fPreprocessorToken = 0;
		return result;
	}

	/**
	 * Read until end of preprocessor directive.
	 */
	private void getRestOfPreprocessorLine() {
		int c = getChar();
		while (true) {
			while ((c != '\n') && (c != '\r') && (c != '/') && (c != '"') && (c != EOFCHAR)) {
				c = getChar();
			}
			if (c == '/') {
				// we need to peek ahead at the next character to see if
				// this is a comment or not
				int next = getChar();
				if (next == '/') {
					// single line comment
					matchSinglelineComment(false);
					break;
				} else if (next == '*') {
					// multiline comment
					if (matchMultilineComment())
						break;
					else
						c = getChar();
					continue;
				} else {
					// we are not in a comment
					c = next;
					continue;
				}
			} else if (c == '"') {
				matchStringLiteral();
				c = getChar();
			} else {
				ungetChar(c);
				break;
			}
		}
	}

	private void matchSinglelineComment(boolean includeNewline) {
		int c = getChar();
		while (c != '\n' && c != EOFCHAR) {
			int next = getChar();
			if (c == '\r' && next == '\n' && !includeNewline) {
				// exclude CRLF line ending
				ungetChar(next);
				break;
			}
			c = next;
		}
		if (c == EOFCHAR || !includeNewline) {
			ungetChar(c);
		}
	}

	private boolean matchMultilineComment() {
		boolean encounteredNewline = false;
		int state = 0;
		int c = getChar();
		while (state != 2 && c != EOFCHAR) {
			if (c == '\n')
				encounteredNewline = true;

			switch (state) {
			case 0:
				if (c == '*')
					state = 1;
				break;
			case 1:
				if (c == '/') {
					state = 2;
				} else if (c != '*') {
					state = 0;
				}
				break;
			}
			c = getChar();
		}
		ungetChar(c);
		return encounteredNewline;
	}

	static {
		fgKeywords.put("and", Integer.valueOf(Token.t_and)); //$NON-NLS-1$
		fgKeywords.put("and_eq", Integer.valueOf(Token.t_and_eq)); //$NON-NLS-1$
		fgKeywords.put("asm", Integer.valueOf(Token.t_asm)); //$NON-NLS-1$
		fgKeywords.put("auto", Integer.valueOf(Token.t_auto)); //$NON-NLS-1$
		fgKeywords.put("bitand", Integer.valueOf(Token.t_bitand)); //$NON-NLS-1$
		fgKeywords.put("bitor", Integer.valueOf(Token.t_bitor)); //$NON-NLS-1$
		fgKeywords.put("bool", Integer.valueOf(Token.t_bool)); //$NON-NLS-1$
		fgKeywords.put("break", Integer.valueOf(Token.t_break)); //$NON-NLS-1$
		fgKeywords.put("case", Integer.valueOf(Token.t_case)); //$NON-NLS-1$
		fgKeywords.put("catch", Integer.valueOf(Token.t_catch)); //$NON-NLS-1$
		fgKeywords.put("char", Integer.valueOf(Token.t_char)); //$NON-NLS-1$
		fgKeywords.put("class", Integer.valueOf(Token.t_class)); //$NON-NLS-1$
		fgKeywords.put("compl", Integer.valueOf(Token.t_compl)); //$NON-NLS-1$
		fgKeywords.put("const", Integer.valueOf(Token.t_const)); //$NON-NLS-1$
		fgKeywords.put("const_cast", Integer.valueOf(Token.t_const_cast)); //$NON-NLS-1$
		fgKeywords.put("constexpr", Integer.valueOf(Token.t_constexpr)); //$NON-NLS-1$
		fgKeywords.put("continue", Integer.valueOf(Token.t_continue)); //$NON-NLS-1$
		fgKeywords.put("default", Integer.valueOf(Token.t_default)); //$NON-NLS-1$
		fgKeywords.put("delete", Integer.valueOf(Token.t_delete)); //$NON-NLS-1$
		fgKeywords.put("do", Integer.valueOf(Token.t_do)); //$NON-NLS-1$
		fgKeywords.put("double", Integer.valueOf(Token.t_double)); //$NON-NLS-1$
		fgKeywords.put("dynamic_cast", Integer.valueOf(Token.t_dynamic_cast)); //$NON-NLS-1$
		fgKeywords.put("else", Integer.valueOf(Token.t_else)); //$NON-NLS-1$
		fgKeywords.put("enum", Integer.valueOf(Token.t_enum)); //$NON-NLS-1$
		fgKeywords.put("explicit", Integer.valueOf(Token.t_explicit)); //$NON-NLS-1$
		fgKeywords.put("export", Integer.valueOf(Token.t_export)); //$NON-NLS-1$
		fgKeywords.put("extern", Integer.valueOf(Token.t_extern)); //$NON-NLS-1$
		fgKeywords.put("false", Integer.valueOf(Token.t_false)); //$NON-NLS-1$
		fgKeywords.put("float", Integer.valueOf(Token.t_float)); //$NON-NLS-1$
		fgKeywords.put("for", Integer.valueOf(Token.t_for)); //$NON-NLS-1$
		fgKeywords.put("friend", Integer.valueOf(Token.t_friend)); //$NON-NLS-1$
		fgKeywords.put("goto", Integer.valueOf(Token.t_goto)); //$NON-NLS-1$
		fgKeywords.put("if", Integer.valueOf(Token.t_if)); //$NON-NLS-1$
		fgKeywords.put("inline", Integer.valueOf(Token.t_inline)); //$NON-NLS-1$
		fgKeywords.put("int", Integer.valueOf(Token.t_int)); //$NON-NLS-1$
		fgKeywords.put("long", Integer.valueOf(Token.t_long)); //$NON-NLS-1$
		fgKeywords.put("mutable", Integer.valueOf(Token.t_mutable)); //$NON-NLS-1$
		fgKeywords.put("namespace", Integer.valueOf(Token.t_namespace)); //$NON-NLS-1$
		fgKeywords.put("new", Integer.valueOf(Token.t_new)); //$NON-NLS-1$
		fgKeywords.put("not", Integer.valueOf(Token.t_not)); //$NON-NLS-1$
		fgKeywords.put("not_eq", Integer.valueOf(Token.t_not_eq)); //$NON-NLS-1$
		fgKeywords.put("operator", Integer.valueOf(Token.t_operator)); //$NON-NLS-1$
		fgKeywords.put("or", Integer.valueOf(Token.t_or)); //$NON-NLS-1$
		fgKeywords.put("or_eq", Integer.valueOf(Token.t_or_eq)); //$NON-NLS-1$
		fgKeywords.put("private", Integer.valueOf(Token.t_private)); //$NON-NLS-1$
		fgKeywords.put("protected", Integer.valueOf(Token.t_protected)); //$NON-NLS-1$
		fgKeywords.put("public", Integer.valueOf(Token.t_public)); //$NON-NLS-1$
		fgKeywords.put("register", Integer.valueOf(Token.t_register)); //$NON-NLS-1$
		fgKeywords.put("reinterpret_cast", Integer.valueOf(Token.t_reinterpret_cast)); //$NON-NLS-1$
		fgKeywords.put("return", Integer.valueOf(Token.t_return)); //$NON-NLS-1$
		fgKeywords.put("short", Integer.valueOf(Token.t_short)); //$NON-NLS-1$
		fgKeywords.put("signed", Integer.valueOf(Token.t_signed)); //$NON-NLS-1$
		fgKeywords.put("sizeof", Integer.valueOf(Token.t_sizeof)); //$NON-NLS-1$
		fgKeywords.put("static", Integer.valueOf(Token.t_static)); //$NON-NLS-1$
		fgKeywords.put("static_cast", Integer.valueOf(Token.t_static_cast)); //$NON-NLS-1$
		fgKeywords.put("struct", Integer.valueOf(Token.t_struct)); //$NON-NLS-1$
		fgKeywords.put("switch", Integer.valueOf(Token.t_switch)); //$NON-NLS-1$
		fgKeywords.put("template", Integer.valueOf(Token.t_template)); //$NON-NLS-1$
		fgKeywords.put("this", Integer.valueOf(Token.t_this)); //$NON-NLS-1$
		fgKeywords.put("throw", Integer.valueOf(Token.t_throw)); //$NON-NLS-1$
		fgKeywords.put("true", Integer.valueOf(Token.t_true)); //$NON-NLS-1$
		fgKeywords.put("try", Integer.valueOf(Token.t_try)); //$NON-NLS-1$
		fgKeywords.put("typedef", Integer.valueOf(Token.t_typedef)); //$NON-NLS-1$
		fgKeywords.put("typeid", Integer.valueOf(Token.t_typeid)); //$NON-NLS-1$
		fgKeywords.put("typename", Integer.valueOf(Token.t_typename)); //$NON-NLS-1$
		fgKeywords.put("union", Integer.valueOf(Token.t_union)); //$NON-NLS-1$
		fgKeywords.put("unsigned", Integer.valueOf(Token.t_unsigned)); //$NON-NLS-1$
		fgKeywords.put("using", Integer.valueOf(Token.t_using)); //$NON-NLS-1$
		fgKeywords.put("virtual", Integer.valueOf(Token.t_virtual)); //$NON-NLS-1$
		fgKeywords.put("void", Integer.valueOf(Token.t_void)); //$NON-NLS-1$
		fgKeywords.put("volatile", Integer.valueOf(Token.t_volatile)); //$NON-NLS-1$
		fgKeywords.put("wchar_t", Integer.valueOf(Token.t_wchar_t)); //$NON-NLS-1$
		fgKeywords.put("while", Integer.valueOf(Token.t_while)); //$NON-NLS-1$
		fgKeywords.put("xor", Integer.valueOf(Token.t_xor)); //$NON-NLS-1$
		fgKeywords.put("xor_eq", Integer.valueOf(Token.t_xor_eq)); //$NON-NLS-1$

		// additional java keywords
		fgKeywords.put("abstract", Integer.valueOf(Token.t_abstract)); //$NON-NLS-1$
		fgKeywords.put("boolean", Integer.valueOf(Token.t_boolean)); //$NON-NLS-1$
		fgKeywords.put("byte", Integer.valueOf(Token.t_byte)); //$NON-NLS-1$
		fgKeywords.put("extends", Integer.valueOf(Token.t_extends)); //$NON-NLS-1$
		fgKeywords.put("final", Integer.valueOf(Token.t_final)); //$NON-NLS-1$
		fgKeywords.put("finally", Integer.valueOf(Token.t_finally)); //$NON-NLS-1$
		fgKeywords.put("implements", Integer.valueOf(Token.t_implements)); //$NON-NLS-1$
		fgKeywords.put("import", Integer.valueOf(Token.t_import)); //$NON-NLS-1$
		fgKeywords.put("interface", Integer.valueOf(Token.t_interface)); //$NON-NLS-1$
		fgKeywords.put("instanceof", Integer.valueOf(Token.t_instanceof)); //$NON-NLS-1$
		fgKeywords.put("native", Integer.valueOf(Token.t_native)); //$NON-NLS-1$
		fgKeywords.put("null", Integer.valueOf(Token.t_null)); //$NON-NLS-1$
		fgKeywords.put("package", Integer.valueOf(Token.t_package)); //$NON-NLS-1$
		fgKeywords.put("super", Integer.valueOf(Token.t_super)); //$NON-NLS-1$
		fgKeywords.put("synchronized", Integer.valueOf(Token.t_synchronized)); //$NON-NLS-1$
		fgKeywords.put("throws", Integer.valueOf(Token.t_throws)); //$NON-NLS-1$
		fgKeywords.put("transient", Integer.valueOf(Token.t_transient)); //$NON-NLS-1$
	}
}
