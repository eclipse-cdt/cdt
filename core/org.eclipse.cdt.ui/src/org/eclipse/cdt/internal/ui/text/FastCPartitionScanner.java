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
	private static final int SLASH= 2; // prefix for SINGLE_LINE or MULTI_LINE
	private static final int SLASH_STAR= 3; // prefix for MULTI_LINE_COMMENT
	private static final int STAR= 4; // postfix for MULTI_LINE_COMMENT
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
	
	private final IToken[] fTokens= new IToken[] {
		new Token(null),
		new Token(C_SINGLE_LINE_COMMENT),
		new Token(C_MULTILINE_COMMENT),
		new Token(C_CHARACTER),
		new Token(C_STRING)
	};

	/*
	 * @see org.eclipse.jface.text.rules.ITokenScanner#nextToken()
	 */
	public IToken nextToken() {
		fTokenOffset += fTokenLength;
		fTokenLength= fPrefixLength;

		final char[][] delimiters = fScanner.getLegalLineDelimiters();

		while (true) {
			final int ch= fScanner.read();

			if (ch == ICharacterScanner.EOF) {
		 		if (fTokenLength > 0) {
		 			fLast= NONE; // ignore last
		 			return preFix(fState, CCODE, NONE, 0);

		 		}
		 		fLast= NONE;
		 		fPrefixLength= 0;
		 		return Token.EOF;
			}
			
			// detect if we're at the end of the line
			final int delim = detectLineDelimiter(ch, fScanner, delimiters);
			if (delim != -1) {
				final int len = delimiters[delim].length;
				if (len > 1) {
					// adjust the token length if the delimiter was 2 or more chars
					fTokenLength += (len - 1);
				}
				switch (fState) {
				case SINGLE_LINE_COMMENT:
				case CHARACTER:
				//case STRING:				
					// assert(fTokenLength > 0);
					// if last char was a backslash then we have an escaped line
					if (fLast != BACKSLASH) {
						return postFix(fState);
					}
					// FALLTHROUGH

				default:
					consume();
					continue;
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
						}							
						preFix(CCODE, SINGLE_LINE_COMMENT, NONE, 2);
						fTokenOffset += fTokenLength;
						fTokenLength= fPrefixLength;
						break;
	
					}
					fTokenLength++;
					fLast= SLASH;
					break;
	
				case '*':
					if (fLast == SLASH) {
						if (fTokenLength - getLastLength(fLast) > 0)
							return preFix(CCODE, MULTI_LINE_COMMENT, SLASH_STAR, 2);
			
						preFix(CCODE, MULTI_LINE_COMMENT, SLASH_STAR, 2);
						fTokenOffset += fTokenLength;
						fTokenLength= fPrefixLength;
						break;

					}
					consume();
					break;
					
				case '\'':
					fLast= NONE; // ignore fLast
					if (fTokenLength > 0) {
						return preFix(CCODE, CHARACTER, NONE, 1);
					}
					preFix(CCODE, CHARACTER, NONE, 1);
					fTokenOffset += fTokenLength;
					fTokenLength= fPrefixLength;
					break;

				case '"':
					fLast= NONE; // ignore fLast				
					if (fTokenLength > 0 ) {
						return preFix(CCODE, STRING, NONE, 1);
					}
					preFix(CCODE, STRING, NONE, 1);
					fTokenOffset += fTokenLength;
					fTokenLength= fPrefixLength;
					consume();
					break;

				default:
					consume();
					break;
				}
				break;
	
	 		case SINGLE_LINE_COMMENT:
				switch (ch) {
				case '\\':
					fLast= (fLast == BACKSLASH) ? NONE : BACKSLASH;
					fTokenLength++;
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
					}
					consume();
					break;
	
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

		 			}
	 				consume();
	 				break; 					
		 		
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
	
	 				}
	 				consume();
	 				break;
	
	 			default:
					consume();
	 				break;
	 			}
	 			break;
	 		}
		} 
 	}		

	/**
	 * returns index of longest matching element in delimiters
	 */
	private static final int detectLineDelimiter(final int ch, final ICharacterScanner scanner, final char[][] delimiters) {
		int longestDelimiter = -1;
		int maxLen = 0;
		for (int i = 0; i < delimiters.length; i++) {
			final char[] delim = delimiters[i];
			if (ch == delim[0]) {
				final int len = delim.length;
				if (len > maxLen && (len == 1 || sequenceDetected(scanner, delim, 1, len))) {
					maxLen = len;
					longestDelimiter = i;
				}
			}
		}
		return longestDelimiter;
	}
	
	/**
	 * <code>true</code> if sequence matches between beginIndex (inclusive) and endIndex (exclusive)
	 */
	private static final boolean sequenceDetected(final ICharacterScanner scanner, final char[] sequence, final int beginIndex, final int endIndex) {
		int charsRead = 0;
		for (int i = beginIndex; i < endIndex; ++i) {
			final int c = scanner.read();
			if (c != ICharacterScanner.EOF) {
				++charsRead;
			}
			if (c != sequence[i]) {
				// Non-matching character detected, rewind the scanner back to the start.
				for (; charsRead > 0; --charsRead) {
					scanner.unread();
				}
				return false;
			}
		}
		return true;
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
		fTokenLength -= getLastLength(fLast);
		fLast= last;
		fPrefixLength= prefixLength;
		IToken token= fTokens[state];		
		fState= newState;
		return token;
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

		else if (contentType.equals(C_CHARACTER))
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
