/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *     Anton Leherbauer (Wind River Systems)
 *     Andrew Ferguson (Symbian)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text;


import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPartitionTokenScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

import org.eclipse.cdt.ui.text.ICPartitions;
import org.eclipse.cdt.ui.text.doctools.IDocCommentOwner;


/**
 * This scanner recognizes the C multi line comments, C single line comments,
 * C strings, C characters and C preprocessor directives.
 */
public final class FastCPartitionScanner implements IPartitionTokenScanner, ICPartitions {

	// states
	private static final int CCODE= 0;
	private static final int SINGLE_LINE_COMMENT= 1;
	private static final int MULTI_LINE_COMMENT= 2;
	private static final int CHARACTER= 3;
	private static final int STRING= 4;
	private static final int RAW_STRING= 5;
	private static final int PREPROCESSOR= 6;
	private static final int PREPROCESSOR_MULTI_LINE_COMMENT= 7;
	private static final int PREPROCESSOR_STRING= 8;
	private static final int SINGLE_LINE_DOC_COMMENT= 9;
	private static final int MULTI_LINE_DOC_COMMENT= 10;
	
	/**
	 * Sub state for raw strings.
	 */
	private enum RawStringState {
		OPEN_DELIMITER,
		CONTENT,
		CLOSE_DELIMITER
	}
	
	// beginning of prefixes and postfixes
	private static final int NONE= 0;
	private static final int BACKSLASH= 1; // postfix for STRING, CHARACTER and SINGLE_LINE_COMMENT
	private static final int SLASH= 2; // prefix for SINGLE_LINE or MULTI_LINE
	private static final int SLASH_STAR= 3; // prefix for MULTI_LINE_COMMENT
	private static final int STAR= 4; // postfix for MULTI_LINE_COMMENT
	private static final int CARRIAGE_RETURN=5; // postfix for STRING, CHARACTER and SINGLE_LINE_COMMENT
	private static final int BACKSLASH_CR= 6; // postfix for STRING, CHARACTER and SINGLE_LINE_COMMENT
	private static final int BACKSLASH_BACKSLASH= 7; // postfix for STRING, CHARACTER
	private static final int RAW_STRING_R= 8; // prefix for RAW_STRING
	private static final int IDENT= 9;
	
	/** The scanner. */
	private final BufferedDocumentScanner fScanner= new BufferedDocumentScanner(1000);	// faster implementation
	
	/** The offset of the last returned token. */
	private int fTokenOffset;
	/** The length of the last returned token. */
	private int fTokenLength;
	
	/** The state of the scanner. */
	private int fState;
	/** The last significant characters read. */
	private int fLast;
	/** The amount of characters already read on first call to nextToken(). */
	private int fPrefixLength;
	/** Indicate whether current char is first non-whitespace char on the line*/
	private boolean fFirstCharOnLine= true;
	/** An optional (possibly null) comment owner for detecting documentation-comments */
	private final IDocCommentOwner fOwner;
	
	private IDocument fDocument;
	
	private final IToken[] fTokens= new IToken[] {
		new Token(null),
		new Token(C_SINGLE_LINE_COMMENT),
		new Token(C_MULTI_LINE_COMMENT),
		new Token(C_CHARACTER),
		new Token(C_STRING),
		new Token(C_STRING),
		new Token(C_PREPROCESSOR),
		new Token(C_MULTI_LINE_COMMENT),
		new Token(C_PREPROCESSOR),
		new Token(C_SINGLE_LINE_DOC_COMMENT),
		new Token(C_MULTI_LINE_DOC_COMMENT)
	};
	
	private final StringBuilder fRawStringDelimiter = new StringBuilder(12);

	public FastCPartitionScanner(IDocCommentOwner owner) {
	   fOwner = owner;
	}
	
	public FastCPartitionScanner() {
	    this(null);
	}

	/*
	 * @see org.eclipse.jface.text.rules.ITokenScanner#nextToken()
	 */
	@Override
	public IToken nextToken() {

		fTokenOffset += fTokenLength;
		fTokenLength= fPrefixLength;

		RawStringState rawStringState = RawStringState.OPEN_DELIMITER;
		int rawStringDelimiterIdx = 0;
		
		while (true) {
			final int ch= fScanner.read();
			
			final boolean isFirstCharOnLine= fFirstCharOnLine;
			if (isFirstCharOnLine && ch != ' ' && ch != '\t') {
				fFirstCharOnLine= false;
			}
			// characters
	 		switch (ch) {
	 		case ICharacterScanner.EOF:
	 			fLast= NONE; // ignore last
		 		if (fTokenLength > 0) {
		 			return preFix(fState, CCODE, NONE, 0);

		 		} else {
		 			fPrefixLength= 0;
					return Token.EOF;
		 		}

	 		case '\r':
	 			fFirstCharOnLine= true;
	 			if (fLast == BACKSLASH || fLast == BACKSLASH_BACKSLASH) {
	 				fLast= BACKSLASH_CR;
					fTokenLength++;
 					continue;
	 			} else if (fLast != CARRIAGE_RETURN) {
	 				fLast= CARRIAGE_RETURN;
	 				fTokenLength++;
	 				continue;
	 			} else {
	 				// fLast == CARRIAGE_RETURN
					switch (fState) {
					case SINGLE_LINE_COMMENT:
					case CHARACTER:
					case STRING:
					case PREPROCESSOR:
						if (fTokenLength > 0) {
							IToken token= fTokens[fState];
							fLast= CARRIAGE_RETURN;
							fPrefixLength= 1;

							fState= CCODE;
							return token;

						} else {
							consume();
							continue;
						}

					default:
						consume();
						continue;
					}
	 			}

	 		case '\\':
				switch (fState) {
				case CHARACTER:
				case STRING:
				case PREPROCESSOR_STRING:
					fTokenLength++;
					fLast= fLast == BACKSLASH ? BACKSLASH_BACKSLASH : BACKSLASH;
					continue;
				default:
					fTokenLength++;
					fLast= BACKSLASH;
					continue;
	 			}

	 		case '\n':
	 			fFirstCharOnLine= true;
				switch (fState) {
				case SINGLE_LINE_COMMENT:
				case CHARACTER:
				case STRING:
				case PREPROCESSOR:
				case PREPROCESSOR_STRING:
					// assert(fTokenLength > 0);
					// if last char was a backslash then we have spliced line
					if (fLast != BACKSLASH && fLast != BACKSLASH_CR && fLast != BACKSLASH_BACKSLASH) {
						return postFix(fState);
					}
					consume();
					continue;

				default:
					consume();
					continue;
				}

			default:
				if (fLast == CARRIAGE_RETURN) {
					switch (fState) {
					case SINGLE_LINE_COMMENT:
					case CHARACTER:
					case STRING:
					case PREPROCESSOR:
					case PREPROCESSOR_STRING:

						int last;
						int newState;
						switch (ch) {
						case '/':
							last= SLASH;
							newState= CCODE;
							break;

						case '*':
							last= STAR;
							newState= CCODE;
							break;

						case '\'':
							last= NONE;
							newState= CHARACTER;
							break;

						case '"':
							last= NONE;
							newState= STRING;
							break;

						case '\r':
							last= CARRIAGE_RETURN;
							newState= CCODE;
							break;

						case '\\':
							last= BACKSLASH;
							newState= CCODE;
							break;

						case '#':
							last= NONE;
							newState= PREPROCESSOR;
							break;

						default:
							last= NONE;
							newState= CCODE;
							break;
						}

						fLast= NONE; // ignore fLast
						return preFix(fState, newState, last, 1);

					case CCODE:
						if (ch == '#' && isFirstCharOnLine) {
							fLast= NONE; // ignore fLast
							int column= fScanner.getColumn() - 1;
							fTokenLength -= column;
							if (fTokenLength > 0) {
								return preFix(fState, PREPROCESSOR, NONE, column + 1);
							} else {
								preFix(fState, PREPROCESSOR, NONE, column + 1);
								fTokenOffset += fTokenLength;
								fTokenLength= fPrefixLength;
								break;
							}
						}
						break;

					default:
						break;
					}
				}
			}

			// states
	 		switch (fState) {
	 		case CCODE:
				switch (ch) {
				case '/':
					if (fLast == SLASH) {
						if (fTokenLength - getLastLength(fLast) > 0) {
							return preFix(CCODE, SINGLE_LINE_COMMENT, NONE, 2);
						} else {
							preFix(CCODE, SINGLE_LINE_COMMENT, NONE, 2);
							fTokenOffset += fTokenLength;
							fTokenLength= fPrefixLength;
							break;
						}
	
					} else {
						fTokenLength++;
						fLast= SLASH;
						break;
					}
	
				case '*':
					if (fLast == SLASH) {
						if (fTokenLength - getLastLength(fLast) > 0) {
							return preFix(CCODE, MULTI_LINE_COMMENT, SLASH_STAR, 2);
						} else {
							preFix(CCODE, MULTI_LINE_COMMENT, SLASH_STAR, 2);
							fTokenOffset += fTokenLength;
							fTokenLength= fPrefixLength;
							break;
						}

					} else {
						consume();
						break;
					}
					
				case '\'':
					fLast= NONE; // ignore fLast
					if (fTokenLength > 0) {
						return preFix(CCODE, CHARACTER, NONE, 1);
					} else {
						preFix(CCODE, CHARACTER, NONE, 1);
						fTokenOffset += fTokenLength;
						fTokenLength= fPrefixLength;
						break;
					}

				case 'u':
				case 'U':
				case 'L':
					if (fLast != IDENT) {
						fLast = NONE;
					}
					fTokenLength++;
					continue;
					
		 		case 'R':
		 			if (fLast == RAW_STRING_R) {
		 				fLast = IDENT;
		 			} else if (fLast != IDENT) {
			 			fLast = RAW_STRING_R;
		 			}
			 		fTokenLength++;
		 			continue;

				case '"':
					int newState = STRING;
					if (fLast == RAW_STRING_R) {
						newState = RAW_STRING;
						rawStringState = RawStringState.OPEN_DELIMITER;
						fRawStringDelimiter.setLength(0);
					}
					fLast= NONE; // ignore fLast
					if (fTokenLength > 0 ) {
						return preFix(CCODE, newState, NONE, 1);
					} else {
						preFix(CCODE, newState, NONE, 1);
						fTokenOffset += fTokenLength;
						fTokenLength= fPrefixLength;
						break;
					}
					
				case '#':
					if (isFirstCharOnLine) {
						int column= fScanner.getColumn() - 1;
						fTokenLength -= column;
						if (fTokenLength > 0) {
							return preFix(fState, PREPROCESSOR, NONE, column + 1);
						} else {
							preFix(fState, PREPROCESSOR, NONE, column + 1);
							fTokenOffset += fTokenLength;
							fTokenLength= fPrefixLength;
							break;
						}
					}
					consume();
					break;
				default:
					if ('a' <= ch && ch <= 'z' || 'A' <= ch && ch <= 'Z' || ch =='_') {
						fLast = IDENT;
						fTokenOffset++;
					} else if ('0' <= ch && ch <= '9' && fLast == IDENT) {
						fTokenOffset++;
					} else {
						consume();
					}
					break;
				}
				break;

	 		case SINGLE_LINE_COMMENT:
				consume();
 				break;

	 		case PREPROCESSOR:
	 			switch (ch) {
				case '/':
					if (fLast == SLASH) {
						if (fTokenLength - getLastLength(fLast) > 0) {
							return preFix(fState, SINGLE_LINE_COMMENT, SLASH, 2);
						} else {
							preFix(fState, SINGLE_LINE_COMMENT, SLASH, 2);
							fTokenOffset += fTokenLength;
							fTokenLength= fPrefixLength;
							break;
						}
					} else {
						fTokenLength++;
						fLast= SLASH;
					}
					break;
	
				case '*':
					if (fLast == SLASH) {
						if (fTokenLength - getLastLength(fLast) > 0) {
							return preFix(fState, PREPROCESSOR_MULTI_LINE_COMMENT, SLASH_STAR, 2);
						} else {
							preFix(fState, PREPROCESSOR_MULTI_LINE_COMMENT, SLASH_STAR, 2);
							fTokenOffset += fTokenLength;
							fTokenLength= fPrefixLength;
							break;
						}
					}
					consume();
					break;

				case '"':
					if (fLast != BACKSLASH) {
						fState= PREPROCESSOR_STRING;
					}
					consume();
					break;
					
		 		default:
					consume();
	 				break;
	 			}
	 			break;

	 		case PREPROCESSOR_STRING:
				switch (ch) {
				case '"':
					if (fLast != BACKSLASH) {
						fState= PREPROCESSOR;
					}
					consume();
					break;
					
		 		default:
					consume();
	 				break;
				}
				break;
				
	 		case PREPROCESSOR_MULTI_LINE_COMMENT:
				switch (ch) {
				case '*':
					fTokenLength++;
					fLast= STAR;
					break;
	
				case '/':
					if (fLast == STAR) {
						IToken token= postFix(fState);
						fState= PREPROCESSOR;
						return token;
					}
					consume();
					break;
	
				default:
					consume();
					break;
				}
				break;
				
	 		case MULTI_LINE_COMMENT:
				switch (ch) {
				case '*':
					fTokenLength++;
					fLast= STAR;
					break;
	
				case '/':
					if (fLast == STAR) {
						return postFix(MULTI_LINE_COMMENT);
					} else {
						consume();
						break;
					}
	
				default:
					consume();
					break;
				}
				break;
				
	 		case STRING:
	 			switch (ch) {
				case '\"':
	 				if (fLast != BACKSLASH) {
	 					return postFix(STRING);
		 			} else {
		 				consume();
		 				break;
		 			}
		 		
		 		default:
					consume();
	 				break;
	 			}
	 			break;

	 		case RAW_STRING:
	 			switch (rawStringState) {
	 			case OPEN_DELIMITER:
	 				if (ch == '(') {
	 					rawStringState = RawStringState.CONTENT;
	 				} else if (ch == '"') {
	 					return postFix(RAW_STRING);
	 				} else if (ch != ' ' && ch != '\\' && ch != ')' && fRawStringDelimiter.length() < 12) {
	 					fRawStringDelimiter.append((char) ch);
	 				} else {
	 					fState = STRING;
	 				}
		 			consume();
	 				break;
	 			case CONTENT:
	 				if (ch == ')') {
	 					rawStringState = RawStringState.CLOSE_DELIMITER;
	 					rawStringDelimiterIdx = 0;
	 				}
		 			consume();
	 				break;
	 			case CLOSE_DELIMITER:
	 				if (ch == ')') {
	 					rawStringDelimiterIdx = 0;
	 				} else if (rawStringDelimiterIdx < fRawStringDelimiter.length()) {
	 					if (fRawStringDelimiter.charAt(rawStringDelimiterIdx) != ch) {
	 						rawStringState = RawStringState.CONTENT;
	 					} else {
	 						++rawStringDelimiterIdx;
	 					}
	 				} else if (ch == '"') {
	 					return postFix(RAW_STRING);
	 				} else {
	 					rawStringState = RawStringState.CONTENT;
	 				}
 					consume();
	 				break;
	 			}
	 			break;

	 		case CHARACTER:
	 			switch (ch) {
	 			case '\'':
	 				if (fLast != BACKSLASH) {
	 					return postFix(CHARACTER);
	 				} else {
		 				consume();
		 				break;
	 				}
	
	 			default:
					consume();
	 				break;
	 			}
	 			break;
	 		}
		}
 	}

	private static final int getLastLength(int last) {
		switch (last) {
		default:
			return -1;

		case NONE:
		case IDENT:
			return 0;
			
		case CARRIAGE_RETURN:
		case BACKSLASH:
		case SLASH:
		case STAR:
		case RAW_STRING_R:
			return 1;

		case SLASH_STAR:
		case BACKSLASH_CR:
		case BACKSLASH_BACKSLASH:
			return 2;

		}
	}

	private final void consume() {
		fTokenLength++;
		fLast= NONE;
	}
	
	private final IToken postFix(int state) {
		return postFix(state, CCODE);
	}
	
	private final IToken postFix(int state, int newState) {
		fTokenLength++;
		fLast= NONE;
		fState= newState;
		fPrefixLength= 0;
		return fTokens[interceptTokenState(state)];
	}


	private final IToken preFix(int state, int newState, int last, int prefixLength) {
		fTokenLength -= getLastLength(fLast);
		fLast= last;
		fPrefixLength= prefixLength;
		fState= newState;
		return fTokens[interceptTokenState(state)];
	}

	private static int getState(String contentType) {

		if (contentType == null)
			return CCODE;

		else if (contentType.equals(C_SINGLE_LINE_COMMENT))
			return SINGLE_LINE_COMMENT;

		else if (contentType.equals(C_MULTI_LINE_COMMENT))
			return MULTI_LINE_COMMENT;

		else if (contentType.equals(C_STRING))
			return STRING;

		else if (contentType.equals(C_CHARACTER))
			return CHARACTER;

		else if (contentType.equals(C_PREPROCESSOR))
			return PREPROCESSOR;
		
		else if (contentType.equals(C_SINGLE_LINE_DOC_COMMENT))
			return SINGLE_LINE_COMMENT; // intentionally non-doc state: the state machine is doc-comment unaware

		else if (contentType.equals(C_MULTI_LINE_DOC_COMMENT))
			return MULTI_LINE_COMMENT; // intentionally non-doc state: the state machine is doc-comment unaware
		
		else
			return CCODE;
	}

	/*
	 * @see IPartitionTokenScanner#setPartialRange(IDocument, int, int, String, int)
	 */
	@Override
	public void setPartialRange(IDocument document, int offset, int length, String contentType, int partitionOffset) {
		fDocument= document;
		fScanner.setRange(document, offset, length);
		fTokenOffset= partitionOffset;
		fTokenLength= 0;
		fPrefixLength= offset - partitionOffset;
		fLast= NONE;
		
		fState= getState(contentType);
		if (fState == STRING) {
			// raw string is a special case: need to restart from partition offset
			try {
				if (partitionOffset > 0 && fDocument.getChar(partitionOffset - 1) == 'R') {
					fState = RAW_STRING;
					int endOffset = offset + length;
					offset = partitionOffset + 1;
					length = endOffset - offset;
					fScanner.setRange(document, offset, length);
					fPrefixLength = offset - partitionOffset;
					fRawStringDelimiter.setLength(0);
				}
			} catch (BadLocationException exc) {
				// cannot happen
			}
		}
		if (offset == partitionOffset) {
			// restart at beginning of partition
			fState= CCODE;
		}

		try {
			int column= fScanner.getColumn();
			fFirstCharOnLine= column == 0 || document.get(offset-column, column).trim().length() == 0;
		} catch (BadLocationException exc) {
			fFirstCharOnLine= true;
		}
	}

	/*
	 * @see ITokenScanner#setRange(IDocument, int, int)
	 */
	@Override
	public void setRange(IDocument document, int offset, int length) {
		fDocument= document;
		fScanner.setRange(document, offset, length);
		fTokenOffset= offset;
		fTokenLength= 0;
		fPrefixLength= 0;
		fLast= NONE;
		fState= CCODE;

		try {
			int column= fScanner.getColumn();
			fFirstCharOnLine= column == 0 || document.get(offset-column, column).trim().length() == 0;
		} catch (BadLocationException exc) {
			fFirstCharOnLine= true;
		}
	}

	/*
	 * @see ITokenScanner#getTokenLength()
	 */
	@Override
	public int getTokenLength() {
		return fTokenLength;
	}

	/*
	 * @see ITokenScanner#getTokenOffset()
	 */
	@Override
	public int getTokenOffset() {
		return fTokenOffset;
	}

	private int interceptTokenState(int proposedTokenState) {
		if(fOwner!=null) {
			switch(proposedTokenState) {
			case MULTI_LINE_COMMENT:
				if(fOwner.getMultilineConfiguration().isDocumentationComment(fDocument, fTokenOffset, fTokenLength))
					return MULTI_LINE_DOC_COMMENT;
				break;

			case SINGLE_LINE_COMMENT:
				if(fOwner.getSinglelineConfiguration().isDocumentationComment(fDocument, fTokenOffset, fTokenLength))
					return SINGLE_LINE_DOC_COMMENT;
				break;

			}
		}
		return proposedTokenState;
	}
	
	/**
	 * @return the DocCommentOwner associated with this partition scanner, or null
	 * if there is no owner.
	 * @since 5.0
	 */
	public IDocCommentOwner getDocCommentOwner() {
		return fOwner;
	}
}
