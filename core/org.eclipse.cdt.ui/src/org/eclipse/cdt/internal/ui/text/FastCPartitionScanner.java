package org.eclipse.cdt.internal.ui.text;


/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPartitionTokenScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;


/**
 * This scanner recognizes the C multi line comments, C single line comments,
 * C strings and C characters.
 */
public class FastCPartitionScanner implements IPartitionTokenScanner, ICPartitions {

	// states
	private static final int CCODE= 0;	
	private static final int SINGLE_LINE_COMMENT= 1;
	private static final int MULTI_LINE_COMMENT= 2;
	private static final int CHARACTER= 3;
	private static final int STRING= 4;
	
	// beginning of prefixes and postfixes
	private static final int NONE= 0;
	private static final int BACKSLASH= 1; // postfix for STRING and CHARACTER
	private static final int SLASH= 2; // prefix for SINGLE_LINE or MULTI_LINE or JAVADOC
	private static final int SLASH_STAR= 3; // prefix for MULTI_LINE_COMMENT or JAVADOC
	private static final int STAR= 4; // postfix for MULTI_LINE_COMMENT or JAVADOC
	private static final int CARRIAGE_RETURN=5; // postfix for STRING, CHARACTER and SINGLE_LINE_COMMENT
	
	/** The scanner. */
//	private final BufferedRuleBasedScanner fScanner= new BufferedRuleBasedScanner(1000);
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
	
	// emulate CPartitionScanner
	private static final boolean fgEmulate= false;
	private int fCOffset;
	private int fCLength;
	
	private final IToken[] fTokens= new IToken[] {
		new Token(null),
		new Token(C_SINGLE_LINE_COMMENT),
		new Token(C_MULTILINE_COMMENT),
		new Token(SKIP),
		new Token(C_STRING)
	};

	/*
	 * @see org.eclipse.jface.text.rules.ITokenScanner#nextToken()
	 */
	public IToken nextToken() {
		
		// emulate CPartitionScanner
		if (fgEmulate) {
			if (fCOffset != -1 && fTokenOffset + fTokenLength != fCOffset + fCLength) {
				fTokenOffset += fTokenLength;		
				return fTokens[CCODE];	
			} else {
				fCOffset= -1;
				fCLength= 0;	
			}
		}		

		fTokenOffset += fTokenLength;
		fTokenLength= fPrefixLength;

		while (true) {
			final int ch= fScanner.read();
			
			// characters
	 		switch (ch) {
	 		case ICharacterScanner.EOF:
		 		if (fTokenLength > 0) {
		 			fLast= NONE; // ignore last
		 			return preFix(fState, CCODE, NONE, 0);

		 		} else {
		 			fLast= NONE;
		 			fPrefixLength= 0;
					return Token.EOF;
		 		}

	 		case '\r':
	 			// emulate CPartitionScanner
	 			if (!fgEmulate && fLast != CARRIAGE_RETURN) {
						fLast= CARRIAGE_RETURN;
						fTokenLength++;
	 					continue;

	 			} else {
	 				
					switch (fState) {
					case SINGLE_LINE_COMMENT:
					case CHARACTER:
					case STRING:
						if (fTokenLength > 0) {
							IToken token= fTokens[fState];
							
				 			// emulate CPartitionScanner
							if (fgEmulate) {
								fTokenLength++;
								fLast= NONE;
								fPrefixLength= 0;
							} else {								
								fLast= CARRIAGE_RETURN;	
								fPrefixLength= 1;
							}
							
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
	
	 		case '\n': 		 		
				switch (fState) {
				case SINGLE_LINE_COMMENT:
				case CHARACTER:
				case STRING:				
					// assert(fTokenLength > 0);
					return postFix(fState);

				default:
					consume();
					continue;
				}

			default:
				if (!fgEmulate && fLast == CARRIAGE_RETURN) {			
					switch (fState) {
					case SINGLE_LINE_COMMENT:
					case CHARACTER:
					case STRING:

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

						default:
							last= NONE;
							newState= CCODE;
							break;
						}
						
						fLast= NONE; // ignore fLast
						return preFix(fState, newState, last, 1);
	
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
						if (fTokenLength - getLastLength(fLast) > 0)
							return preFix(CCODE, MULTI_LINE_COMMENT, SLASH_STAR, 2);
						else {
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
					if (fTokenLength > 0)
						return preFix(CCODE, CHARACTER, NONE, 1);
					else {						
						preFix(CCODE, CHARACTER, NONE, 1);
						fTokenOffset += fTokenLength;
						fTokenLength= fPrefixLength;
						break;
					}

				case '"':
					fLast= NONE; // ignore fLast				
					if (fTokenLength > 0)
						return preFix(CCODE, STRING, NONE, 1);
					else {
						preFix(CCODE, STRING, NONE, 1);
						fTokenOffset += fTokenLength;
						fTokenLength= fPrefixLength;
						break;
					}
	
				default:
					consume();
					break;
				}
				break;
	
	 		case SINGLE_LINE_COMMENT:
				consume();
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
	 			case '\\':
					fLast= (fLast == BACKSLASH) ? NONE : BACKSLASH;
					fTokenLength++;
					break;
					
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
	
	 		case CHARACTER:
	 			switch (ch) {
				case '\\':
					fLast= (fLast == BACKSLASH) ? NONE : BACKSLASH;
					fTokenLength++;
					break;
	
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
			return 0;
			
		case CARRIAGE_RETURN:
		case BACKSLASH:
		case SLASH:
		case STAR:
			return 1;

		case SLASH_STAR:
			return 2;

		}	
	}

	private final void consume() {
		fTokenLength++;
		fLast= NONE;	
	}
	
	private final IToken postFix(int state) {
		fTokenLength++;
		fLast= NONE;
		fState= CCODE;
		fPrefixLength= 0;		
		return fTokens[state];
	}

	private final IToken preFix(int state, int newState, int last, int prefixLength) {
		// emulate CPartitionScanner
		if (fgEmulate && state == CCODE && (fTokenLength - getLastLength(fLast) > 0)) {
			fTokenLength -= getLastLength(fLast);
			fCOffset= fTokenOffset;
			fCLength= fTokenLength;
			fTokenLength= 1;
			fState= newState;
			fPrefixLength= prefixLength;
			fLast= last;
			return fTokens[state];

		} else {
			fTokenLength -= getLastLength(fLast);
			fLast= last;
			fPrefixLength= prefixLength;
			IToken token= fTokens[state];		
			fState= newState;
			return token;
		}
	}

	private static int getState(String contentType) {

		if (contentType == null)
			return CCODE;

		else if (contentType.equals(C_SINGLE_LINE_COMMENT))
			return SINGLE_LINE_COMMENT;

		else if (contentType.equals(C_MULTILINE_COMMENT))
			return MULTI_LINE_COMMENT;

		else if (contentType.equals(C_STRING))
			return STRING;

		else if (contentType.equals(SKIP))
			return CHARACTER;
			
		else
			return CCODE;
	}

	/*
	 * @see IPartitionTokenScanner#setPartialRange(IDocument, int, int, String, int)
	 */
	public void setPartialRange(IDocument document, int offset, int length, String contentType, int partitionOffset) {

		fScanner.setRange(document, offset, length);
		fTokenOffset= partitionOffset;
		fTokenLength= 0;
		fPrefixLength= offset - partitionOffset;
		fLast= NONE;
		
		if (offset == partitionOffset) {
			// restart at beginning of partition
			fState= CCODE;
		} else {
			fState= getState(contentType);			
		}

		// emulate CPartitionScanner
		if (fgEmulate) {
			fCOffset= -1;
			fCLength= 0;
		}
	}

	/*
	 * @see ITokenScanner#setRange(IDocument, int, int)
	 */
	public void setRange(IDocument document, int offset, int length) {

		fScanner.setRange(document, offset, length);
		fTokenOffset= offset;
		fTokenLength= 0;		
		fPrefixLength= 0;
		fLast= NONE;
		fState= CCODE;

		// emulate CPartitionScanner
		if (fgEmulate) {
			fCOffset= -1;
			fCLength= 0;
		}
	}

	/*
	 * @see ITokenScanner#getTokenLength()
	 */
	public int getTokenLength() {
		return fTokenLength;
	}

	/*
	 * @see ITokenScanner#getTokenOffset()
	 */
	public int getTokenOffset() {
		return fTokenOffset;
	}

}
