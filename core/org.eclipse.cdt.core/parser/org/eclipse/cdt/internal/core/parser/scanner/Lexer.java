/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.parser.scanner;

import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.parser.IGCCToken;
import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.OffsetLimitReachedException;

/**
 * In short this class converts line endings (to '\n') and trigraphs 
 * (to their corresponding character), 
 * removes line-splices, comments and whitespace other than newline.
 * Returns preprocessor tokens.
 * <p>
 * In addition to the preprocessor tokens the following tokens may also be returned:
 * {@link #tEND_OF_INPUT}, {@link IToken#tCOMPLETION}.
 * <p>
 * Number literals are split up into {@link IToken#tINTEGER} and {@link IToken#tFLOATINGPT}. 
 * No checks are done on the number literals.
 * <p>
 * UNCs are accepted, however characters from outside of the basic source character set are
 * not converted to UNCs. Rather than that they are tested with 
 * {@link Character#isUnicodeIdentifierPart(char)} and may be accepted as part of an 
 * identifier.
 * <p>
 * The characters in string literals and char-literals are left as they are found, no conversion to
 * an execution character-set is performed.
 */
final public class Lexer {
	public static final int tBEFORE_INPUT   = IToken.FIRST_RESERVED_SCANNER;
	public static final int tNEWLINE		= IToken.FIRST_RESERVED_SCANNER + 1;
	public static final int tEND_OF_INPUT	= IToken.FIRST_RESERVED_SCANNER + 2;
	public static final int tQUOTE_HEADER_NAME    = IToken.FIRST_RESERVED_SCANNER + 3;
	public static final int tSYSTEM_HEADER_NAME   = IToken.FIRST_RESERVED_SCANNER + 4;
	
	private static final int END_OF_INPUT = -1;
	private static final int LINE_SPLICE_SEQUENCE = -2;
	private static final int ORIGIN_LEXER = OffsetLimitReachedException.ORIGIN_LEXER;
	
	public final static class LexerOptions implements Cloneable {
		public boolean fSupportDollarInitializers= true;
		public boolean fSupportMinAndMax= true;
		public boolean fSupportContentAssist= false;
		
		public Object clone() {
			try {
				return super.clone();
			} catch (CloneNotSupportedException e) {
				return null;
			}
		}
	}

	// configuration
	private final LexerOptions fOptions;
	private final ILexerLog fLog;
	
	// the input to the lexer
	private final char[] fInput;
	private int fLimit;

	// after phase 3 (newline, trigraph, line-splice)
	private int fOffset;
	private int fEndOffset;
	private int fCharPhase3;
	
	private boolean fInsideIncludeDirective= false;
	private Token fToken= new SimpleToken(tBEFORE_INPUT, 0, 0);
	
	// for the few cases where we have to lookahead more than one character
	private int fMarkOffset;
	private int fMarkEndOffset;
	private int fMarkPrefetchedChar;
	private boolean fFirstTokenAfterNewline= true;
	
	
	public Lexer(char[] input, LexerOptions options, ILexerLog log) {
		fInput= input;
		fLimit= input.length;
		fOptions= options;
		fLog= log;
		nextCharPhase3();
	}

	public Lexer(char[] input, int limit, LexerOptions options, ILexerLog log) {
		fInput= input;
		fLimit= limit;
		fOptions= options;
		fLog= log;
		nextCharPhase3();
	}
	
	/**
	 * Resets the lexer to the first char and prepares for content-assist mode. 
	 */
	public void setContentAssistMode(int offset) {
		fOptions.fSupportContentAssist= true;
		fLimit= Math.min(fLimit, fInput.length);
		// re-initialize 
		fOffset= fEndOffset= 0;
		nextCharPhase3();
	}

	/**
	 * Call this before consuming the name-token in the include directive. It causes the header-file 
	 * tokens to be created. 
	 */
	public void setInsideIncludeDirective() {
		fInsideIncludeDirective= true;
	}
	
	/** 
	 * Returns the current preprocessor token, does not advance.
	 */
	public Token currentToken() {
		return fToken;
	}
	
	/**
	 * Advances to the next token, skipping whitespace other than newline.
	 * @throws OffsetLimitReachedException when completion is requested in a literal or a header-name.
	 */
	public Token nextToken() throws OffsetLimitReachedException {
		fFirstTokenAfterNewline= fToken.getType() == tNEWLINE;
		return fToken= fetchToken();
	}

	public boolean currentTokenIsFirstOnLine() {
		return fFirstTokenAfterNewline;
	}
	
	/**
	 * Advances to the next newline.
	 * @return the end offset of the last token before the newline or the start of the newline
	 * if there were no other tokens.
	 * @param origin parameter for the {@link OffsetLimitReachedException} when it has to be thrown.
	 * @since 5.0
	 */
	public final int consumeLine(int origin) throws OffsetLimitReachedException {
		Token t= fToken;
		Token lt= null;
		while(true) {
			switch(t.getType()) {
			case IToken.tCOMPLETION:
				fToken= t;
				throw new OffsetLimitReachedException(origin, t);
			case Lexer.tEND_OF_INPUT:
				fToken= t;
				if (fOptions.fSupportContentAssist) {
					throw new OffsetLimitReachedException(origin, lt);
				}
				return lt != null ? lt.getEndOffset() : t.getOffset();
			case Lexer.tNEWLINE:
				fToken= t;
				return lt != null ? lt.getEndOffset() : t.getOffset();
			}
			lt= t;
			t= fetchToken();
		}
	}

	/** 
	 * Advances to the next pound token that starts a preprocessor directive. 
	 * @return pound token of the directive or end-of-input.
	 * @throws OffsetLimitReachedException when completion is requested in a literal or an header-name.
	 */
	public Token nextDirective() throws OffsetLimitReachedException {
		Token t= fToken;
		boolean haveNL= t==null || t.getType() == tNEWLINE;
		loop: while(true) {
			t= fetchToken();
			if (haveNL) {
				switch(t.getType()) {
				case tEND_OF_INPUT:
				case IToken.tPOUND:
					break loop;
				}
				haveNL= false;
			}
			else {
				switch(t.getType()) {
				case tEND_OF_INPUT:
					break loop;
				case tNEWLINE:
					haveNL= true;
					break;
				}
			}
			t= fetchToken();
		} 
		fToken= t;
		return t;
	}
	
	/**
	 * Computes the next token.
	 */
	private Token fetchToken() throws OffsetLimitReachedException {
		while(true) {
			final int start= fOffset;
			final int c= fCharPhase3;
			final int d= nextCharPhase3();
			switch(c) {
			case END_OF_INPUT:
				return newToken(Lexer.tEND_OF_INPUT, start);
			case '\n':
				fInsideIncludeDirective= false;
				return newToken(Lexer.tNEWLINE, start);
			case ' ':
			case '\t':
			case 0xb:  // vertical tab
			case '\f': 
			case '\r':
				continue;

			case 'L':
				switch(d) {
				case '"':
					nextCharPhase3();
					return stringLiteral(start, true);
				case '\'':
					nextCharPhase3();
					return charLiteral(start, true);
				}
				return identifier(start, 1);

			case '"':
				if (fInsideIncludeDirective) {
					return headerName(start, true);
				}
				return stringLiteral(start, false);

			case '\'':
				return charLiteral(start, false);

			case 'a': case 'b': case 'c': case 'd': case 'e': case 'f': case 'g': case 'h': case 'i': 
			case 'j': case 'k': case 'l': case 'm': case 'n': case 'o': case 'p': case 'q': case 'r': 
			case 's': case 't': case 'u': case 'v': case 'w': case 'x': case 'y': case 'z':
			case 'A': case 'B': case 'C': case 'D': case 'E': case 'F': case 'G': case 'H': case 'I':
			case 'J': case 'K':           case 'M': case 'N': case 'O': case 'P': case 'Q': case 'R': 
			case 'S': case 'T': case 'U': case 'V': case 'W': case 'X': case 'Y': case 'Z':
			case '_':
				return identifier(start, 1);

			case '$':
				if (fOptions.fSupportDollarInitializers) {
					return identifier(start, 1);
				}
				break;

			case '\\':
				switch(d) {
				case 'u': case 'U':
					nextCharPhase3();
					return identifier(start, 2);
				}
				return newToken(IToken.tBACKSLASH, start);

			case '0': case '1': case '2': case '3': case '4':
			case '5': case '6': case '7': case '8': case '9':
				return number(start, 1, false);

			case '.':
				switch(d) {
				case '0': case '1': case '2': case '3': case '4':
				case '5': case '6': case '7': case '8': case '9':
					nextCharPhase3();
					return number(start, 2, true);

				case '.':
					markPhase3();
					if (nextCharPhase3() == '.') {
						nextCharPhase3();
						return newToken(IToken.tELLIPSIS, start);
					}
					restorePhase3();
					break;

				case '*':
					nextCharPhase3();
					return newToken(IToken.tDOTSTAR, start);
				}
				return newToken(IToken.tDOT, start);

			case '#':
				if (d == '#') {
					nextCharPhase3();
					return newToken(IToken.tPOUNDPOUND, start);
				}
				return newToken(IToken.tPOUND, start);

			case '{':
				return newToken(IToken.tLBRACE, start);
			case '}':
				return newToken(IToken.tRBRACE, start);
			case '[':
				return newToken(IToken.tLBRACKET, start);
			case ']':
				return newToken(IToken.tRBRACKET, start);
			case '(':
				return newToken(IToken.tLPAREN, start);
			case ')':
				return newToken(IToken.tRPAREN, start);
			case ';':
				return newToken(IToken.tSEMI, start);

			case ':':
				switch(d) {
				case ':':
					nextCharPhase3();
					return newToken(IToken.tCOLONCOLON, start);
				case '>': 
					nextCharPhase3();
					return newDigraphToken(IToken.tRBRACKET, start);
				}
				return newToken(IToken.tCOLON, start);

			case '?':
				return newToken(IToken.tQUESTION, start);

			case '+':
				switch (d) {
				case '+':
					nextCharPhase3();
					return newToken(IToken.tINCR, start);
				case '=':
					nextCharPhase3();
					return newToken(IToken.tPLUSASSIGN, start);
				}
				return newToken(IToken.tPLUS, start);

			case '-':
				switch (d) {
				case '>': 
					int e= nextCharPhase3();
					if (e == '*') {
						nextCharPhase3();
						return newToken(IToken.tARROWSTAR, start);
					}
					return newToken(IToken.tARROW, start);

				case '-':
					nextCharPhase3();
					return newToken(IToken.tDECR, start);
				case '=':
					nextCharPhase3();
					return newToken(IToken.tMINUSASSIGN, start);
				}
				return newToken(IToken.tMINUS, start);

			case '*':
				if (d == '=') {
					nextCharPhase3();
					return newToken(IToken.tSTARASSIGN, start);
				}
				return newToken(IToken.tSTAR, start);

			case '/':
				switch (d) {
				case '=':
					nextCharPhase3();
					return newToken(IToken.tDIVASSIGN, start);
				case '/':
					nextCharPhase3();
					lineComment(start);
					continue; 
				case '*':
					nextCharPhase3();
					blockComment(start);
					continue;
				}
				return newToken(IToken.tDIV, start);

			case '%':
				switch (d) {
				case '=':
					nextCharPhase3();
					return newToken(IToken.tMODASSIGN, start);
				case '>':
					nextCharPhase3();
					return newDigraphToken(IToken.tRBRACE, start);
				case ':':
					final int e= nextCharPhase3();
					if (e == '%') {
						markPhase3();
						if (nextCharPhase3() == ':') {
							nextCharPhase3();
							return newDigraphToken(IToken.tPOUNDPOUND, start);
						}
						restorePhase3();
					}
					return newDigraphToken(IToken.tPOUND, start);
				}
				return newToken(IToken.tMOD, start);

			case '^':
				if (d == '=') {
					nextCharPhase3();
					return newToken(IToken.tXORASSIGN, start);
				}
				return newToken(IToken.tXOR, start);

			case '&':
				switch (d) {
				case '&':
					nextCharPhase3();
					return newToken(IToken.tAND, start);
				case '=':
					nextCharPhase3();
					return newToken(IToken.tAMPERASSIGN, start);
				}
				return newToken(IToken.tAMPER, start);

			case '|':
				switch (d) {
				case '|':
					nextCharPhase3();
					return newToken(IToken.tOR, start);
				case '=':
					nextCharPhase3();
					return newToken(IToken.tBITORASSIGN, start);
				}
				return newToken(IToken.tBITOR, start);

			case '~':
				return newToken(IToken.tBITCOMPLEMENT, start);

			case '!':
				if (d == '=') {
					nextCharPhase3();
					return newToken(IToken.tNOTEQUAL, start);
				}
				return newToken(IToken.tNOT, start);

			case '=':
				if (d == '=') {
					nextCharPhase3();
					return newToken(IToken.tEQUAL, start);
				}
				return newToken(IToken.tASSIGN, start);

			case '<':
				if (fInsideIncludeDirective) {
					return headerName(start, false);
				}

				switch(d) {
				case '=':
					nextCharPhase3();
					return newToken(IToken.tLTEQUAL, start);
				case '<':
					final int e= nextCharPhase3();
					if (e == '=') {
						nextCharPhase3();
						return newToken(IToken.tSHIFTLASSIGN, start);
					} 
					return newToken(IToken.tSHIFTL, start);
				case '?':
					if (fOptions.fSupportMinAndMax) {
						nextCharPhase3();
						return newToken(IGCCToken.tMIN, start);
					} 
					break;
				case ':':
					nextCharPhase3();
					return newDigraphToken(IToken.tLBRACKET, start);
				case '%':
					nextCharPhase3();
					return newDigraphToken(IToken.tLBRACE, start);
				}
				return newToken(IToken.tLT, start);

			case '>':
				switch(d) {
				case '=':
					nextCharPhase3();
					return newToken(IToken.tGTEQUAL, start);
				case '>':
					final int e= nextCharPhase3();
					if (e == '=') {
						nextCharPhase3();
						return newToken(IToken.tSHIFTRASSIGN, start);
					} 
					return newToken(IToken.tSHIFTR, start);
				case '?':
					if (fOptions.fSupportMinAndMax) {
						nextCharPhase3();
						return newToken(IGCCToken.tMAX, start);
					} 
					break;
				}
				return newToken(IToken.tGT, start);

			case ',':
				return newToken(IToken.tCOMMA, start);

			default:
				// in case we have some other letter to start an identifier
				if (Character.isUnicodeIdentifierStart((char) c)) {
					return identifier(start, 1);
				}
				break;
			}
			
			handleProblem(IASTProblem.SCANNER_BAD_CHARACTER, new char[] {(char) c}, start);
			// loop is continued, character is treated as white-space.
		}
    }

	private Token newToken(int kind, int offset) {
    	return new SimpleToken(kind, offset, fOffset);
    }

	private Token newDigraphToken(int kind, int offset) {
    	return new DigraphToken(kind, offset, fOffset);
    }

    private Token newToken(int kind, int offset, int imageLength) {
    	final int endOffset= fOffset;
    	int sourceLen= endOffset-offset;
    	if (sourceLen != imageLength) {
    		return new ImageToken(kind, offset, endOffset, getCharImage(offset, endOffset, imageLength));
    	}
    	return new SourceImageToken(kind, offset, endOffset, fInput);
    }

    private void handleProblem(int problemID, char[] arg, int offset) {
    	fLog.handleProblem(problemID, arg, offset, fOffset);
    }

    private Token headerName(final int start, final boolean expectQuotes) throws OffsetLimitReachedException {
    	int length= 1;
		boolean done = false;
		int c= fCharPhase3;
		loop: while (!done) {
			switch (c) {
			case END_OF_INPUT:
				if (fOptions.fSupportContentAssist) {
					throw new OffsetLimitReachedException(ORIGIN_LEXER, 
							newToken((expectQuotes ? tQUOTE_HEADER_NAME : tSYSTEM_HEADER_NAME), start, length));
				}
				// no break;
			case '\n':
				handleProblem(IProblem.SCANNER_UNBOUNDED_STRING, getInputChars(start, fOffset), start);
				break loop;
				
			case '"':
				done= expectQuotes;
				break;
			case '>':
				done= !expectQuotes;
				break;
			}
			length++;
			c= nextCharPhase3();
		}
		return newToken((expectQuotes ? tQUOTE_HEADER_NAME : tSYSTEM_HEADER_NAME), start, length);
	}

	private void blockComment(final int start) {
		int c= nextCharPhase3();
		while(true) {
			switch (c) {
			case END_OF_INPUT:
				fLog.handleComment(true, start, fOffset);
				return;
			case '*':
				c= nextCharPhase3();
				if (c == '/') {
					nextCharPhase3();
					fLog.handleComment(true, start, fOffset);
					return;
				}
				break;
			default:
				c= nextCharPhase3();
				break;
			}
		}
	}

	private void lineComment(final int start) {
		int c= fCharPhase3;
		while(true) {
			switch (c) {
			case END_OF_INPUT:
			case '\n':
				fLog.handleComment(false, start, fOffset);
				return;
			}
			c= nextCharPhase3();
		}
	}

	private Token stringLiteral(final int start, final boolean wide) throws OffsetLimitReachedException {
		boolean escaped = false;
		boolean done = false;
		int length= wide ? 2 : 1;
		int c= fCharPhase3;
		
		loop: while (!done) {
			switch(c) {
			case END_OF_INPUT:
				if (fOptions.fSupportContentAssist) {
					throw new OffsetLimitReachedException(ORIGIN_LEXER, newToken(wide ? IToken.tLSTRING : IToken.tSTRING, start, length));
				}
				// no break;
			case '\n':
				handleProblem(IProblem.SCANNER_UNBOUNDED_STRING, getInputChars(start, fOffset), start);
				break loop;
				
			case '\\': 
				escaped= !escaped;
				break;
			case '"':
				if (!escaped) {
					done= true;
				}
				escaped= false;
				break;
			default:
				escaped= false;
				break;
			}
			length++;
			c= nextCharPhase3();
		}
		return newToken(wide ? IToken.tLSTRING : IToken.tSTRING, start, length);
	}
	
	private Token charLiteral(final int start, boolean wide) throws OffsetLimitReachedException {
		boolean escaped = false;
		boolean done = false;
		int length= wide ? 2 : 1;
		int c= fCharPhase3;
		
		loop: while (!done) {
			switch(c) {
			case END_OF_INPUT:
				if (fOptions.fSupportContentAssist) {
					throw new OffsetLimitReachedException(ORIGIN_LEXER, newToken(wide ? IToken.tLCHAR : IToken.tCHAR, start, length));
				}
				// no break;
			case '\n':
				handleProblem(IProblem.SCANNER_BAD_CHARACTER, getInputChars(start, fOffset), start);
				break loop;
			case '\\': 
				escaped= !escaped;
				break;
			case '\'':
				if (!escaped) {
					done= true;
				}
				escaped= false;
				break;
			default:
				escaped= false;
				break;
			}
			length++;
			c= nextCharPhase3();
		}
		return newToken(wide ? IToken.tLCHAR : IToken.tCHAR, start, length);
	}
	
	private Token identifier(int start, int length) {
		int tokenKind= IToken.tIDENTIFIER;
    	boolean isPartOfIdentifier= true;
    	int c= fCharPhase3;
        while (true) {
        	switch(c) {
            case 'a': case 'b': case 'c': case 'd': case 'e': case 'f': case 'g': case 'h': case 'i': 
            case 'j': case 'k': case 'l': case 'm': case 'n': case 'o': case 'p': case 'q': case 'r': 
            case 's': case 't': case 'u': case 'v': case 'w': case 'x': case 'y': case 'z':
            case 'A': case 'B': case 'C': case 'D': case 'E': case 'F': case 'G': case 'H': case 'I':
            case 'J': case 'K': case 'L': case 'M': case 'N': case 'O': case 'P': case 'Q': case 'R': 
            case 'S': case 'T': case 'U': case 'V': case 'W': case 'X': case 'Y': case 'Z':
            case '_': 
            case '0': case '1': case '2': case '3': case '4':
            case '5': case '6': case '7': case '8': case '9':
            	break;
            	
            case '\\': // universal character name
            	markPhase3();
            	switch(nextCharPhase3()) {
            	case 'u': case 'U':
            		length++;
            		break;
            	default:
            		restorePhase3();
            		isPartOfIdentifier= false;
            		break;
            	}
            	break;

            case END_OF_INPUT:
				if (fOptions.fSupportContentAssist) {
					tokenKind= IToken.tCOMPLETION;
				}
				isPartOfIdentifier= false;
				break;
            case ' ': case '\t': case 0xb: case '\f': case '\r': case '\n':
                isPartOfIdentifier= false;
            	break;

            case '$':
            	isPartOfIdentifier= fOptions.fSupportDollarInitializers;
            	break;
            	
            case '{': case '}': case '[': case ']': case '#': case '(': case ')': case '<': case '>':
            case '%': case ':': case ';': case '.': case '?': case '*': case '+': case '-': case '/':
            case '^': case '&': case '|': case '~': case '!': case '=': case ',': case '"': case '\'':
            	isPartOfIdentifier= false;
            	break;
            	
            default:
            	isPartOfIdentifier= Character.isUnicodeIdentifierPart((char) c);
            	break;
        	}
        	
        	if (!isPartOfIdentifier) {
        		break;
        	}
        	
        	length++;
        	c= nextCharPhase3();
        }

        return newToken(tokenKind, start, length);
	}
	
	private Token number(final int start, int length, boolean isFloat) throws OffsetLimitReachedException {
		boolean isPartOfNumber= true;
		int c= fCharPhase3;
		while (true) {
			switch(c) {
			// non-digit
            case 'a': case 'b': case 'c': case 'd':           case 'f': case 'g': case 'h': case 'i': 
            case 'j': case 'k': case 'l': case 'm': case 'n': case 'o':           case 'q': case 'r': 
            case 's': case 't': case 'u': case 'v': case 'w': case 'x': case 'y': case 'z':
            case 'A': case 'B': case 'C': case 'D':           case 'F': case 'G': case 'H': case 'I':
            case 'J': case 'K': case 'L': case 'M': case 'N': case 'O':           case 'Q': case 'R': 
            case 'S': case 'T': case 'U': case 'V': case 'W': case 'X': case 'Y': case 'Z':
            case '_': 
            	
            // digit
            case '0': case '1': case '2': case '3': case '4':
            case '5': case '6': case '7': case '8': case '9':
            	break;
            	
            // period
            case '.':
            	isFloat= true;
            	break;
            	
            // sign
            case 'p':
            case 'P':
            case 'e':
            case 'E':
            	length++;
            	c= nextCharPhase3();
            	switch (c) {
            	case '+': case '-':
            	case '0': case '1': case '2': case '3': case '4': case '5': case '6': case '7': case '8': case '9':
            		isFloat= true;
            		length++;
                	c= nextCharPhase3();
            		break;
            	}
            	continue;
            	
            // universal character name (non-digit)
            case '\\':
            	markPhase3();
            	switch(nextCharPhase3()) {
            	case 'u': case 'U':
            		length++;
            		break;
            	default:
            		restorePhase3();
            		isPartOfNumber= false;
            		break;
            	}
            	break;
            
            case tEND_OF_INPUT:
				if (fOptions.fSupportContentAssist) {
					throw new OffsetLimitReachedException(ORIGIN_LEXER, 
							newToken((isFloat ? IToken.tFLOATINGPT : IToken.tINTEGER), start, length));
				}
				isPartOfNumber= false;
				break;
				
            default:
            	isPartOfNumber= false;
            	break;
			}
        	if (!isPartOfNumber) {
        		break;
        	}
        	
        	c= nextCharPhase3();
        	length++;
		}
		
        return newToken((isFloat ? IToken.tFLOATINGPT : IToken.tINTEGER), start, length);
	}
	
	
	/**
	 * Saves the current state of phase3, necessary for '...', '%:%:' and UNCs.
	 */
	private void markPhase3() {
		fMarkOffset= fOffset;
		fMarkEndOffset= fEndOffset;
		fMarkPrefetchedChar= fCharPhase3;
	}
	
	/**
	 * Restores a previously saved state of phase3.
	 */
	private void restorePhase3() {
		fOffset= fMarkOffset;
		fEndOffset= fMarkEndOffset;
		fCharPhase3= fMarkPrefetchedChar;
	}
	
	/**
	 * Perform phase 1-3: Replace \r\n with \n, handle trigraphs, detect line-splicing.
	 * Changes fOffset, fEndOffset and fCharPhase3.
	 */
	private int nextCharPhase3() {
		int offset;
		int c; 
		do {
			offset= fEndOffset;
			c= fetchCharPhase3(offset); // changes fEndOffset
		}
		while(c == LINE_SPLICE_SEQUENCE);

		fOffset= offset;
		fCharPhase3= c;
		return c;
	}
	
	/**
	 * Perform phase 1-3: Replace \r\n with \n, handle trigraphs, detect line-splicing.
	 * Changes <code>fEndOffset</code>, but is stateless otherwise.
	 */
	private int fetchCharPhase3(int pos) {
		if (pos >= fLimit) {
			fEndOffset= fLimit;
			return END_OF_INPUT;
		}
		final char c= fInput[pos++];
		switch(c) {
			// windows line-ending
			case '\r':
			if (pos < fLimit && fInput[pos] == '\n') {	
				fEndOffset= pos+1;
				return '\n';
			}
			fEndOffset= pos;
			return c;

		// trigraph sequences
		case '?':
			if (pos+1 >= fLimit || fInput[pos] != '?') {
				fEndOffset= pos;
				return c;
			}
			final char trigraph= checkTrigraph(fInput[pos+1]);
			if (trigraph == 0) {
				fEndOffset= pos;
				return c;
			}
			if (trigraph != '\\') {
				fEndOffset= pos+2;
				return trigraph;
			}
			pos+= 2;
			// no break, handle backslash
		
		case '\\':
			final int lsPos= findEndOfLineSpliceSequence(pos);
			if (lsPos > pos) {
				fEndOffset= lsPos;
				return LINE_SPLICE_SEQUENCE;
			}
			fEndOffset= pos;
			return '\\';	// don't return c, it may be a '?'
			
		default:
			fEndOffset= pos;
			return c;
		}
	}

	/**
	 * Maps a trigraph to the character it encodes.
	 * @param c trigraph without leading question marks.
	 * @return the character encoded or 0.
	 */
	private char checkTrigraph(char c) {
		switch(c) {
		case '=': return '#';
		case '\'':return '^';
		case '(': return '[';
		case ')': return ']';
		case '!': return '|';
		case '<': return '{';
		case '>': return '}';
		case '-': return '~';
		case '/': return '\\';
		}
		return 0;
	}

	/**
	 * Returns the endoffset for a line-splice sequence, or -1 if there is none.
	 */
	private int findEndOfLineSpliceSequence(int pos) {
		boolean haveBackslash= true;
		int result= -1;
		loop: while(pos < fLimit) {
			switch(fInput[pos++]) {
			case '\n':	
				if (haveBackslash) {
					result= pos;
					haveBackslash= false;
					continue loop;
				}
				return result; 					
		
			case '\r': case ' ': case '\f': case '\t': case 0xb: // vertical tab  
				if (haveBackslash) {
					continue loop;
				}
				return result;
			
			case '?':
				if (pos+1 >= fLimit || fInput[pos] != '?' || fInput[++pos] != '/') {
					return result;
				}
				// fall through to backslash handling
					
			case '\\':
				if (!haveBackslash) {
					haveBackslash= true;
					continue loop;
				}
				return result;

			default:
				return result;
			}
		}
		return result;
	}

	/**
	 * Returns the image from the input without any modification.
	 */
	public char[] getInputChars(int offset, int endOffset) {
		final int length= endOffset-offset;
		final char[] result= new char[length];
		System.arraycopy(fInput, offset, result, 0, length);
		return result;
	}

	char[] getInput() {
		return fInput;
	}
	
	/**
	 * Returns the image with trigraphs replaced and line-splices removed.
	 */
	private char[] getCharImage(int offset, int endOffset, int imageLength) {
		final char[] result= new char[imageLength];
		markPhase3();
		fEndOffset= offset;
		int idx= 0;
		while (idx<imageLength) {
			int c= fetchCharPhase3(fEndOffset);
			if (c != LINE_SPLICE_SEQUENCE) {
				result[idx++]= (char) c;
			}
		}
		restorePhase3();
		return result;
	}
}
