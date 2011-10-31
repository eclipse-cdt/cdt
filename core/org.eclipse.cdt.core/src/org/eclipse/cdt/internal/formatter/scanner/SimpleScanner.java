/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial implementation
 *     Anton Leherbauer - adding tokens for preprocessing directives
 *     Markus Schorn - classification of preprocessing directives.
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
	private static final int EOFCHAR= -1;
	protected static HashMap<String, Integer> fgKeywords= new HashMap<String, Integer>();

    protected Token fCurrentToken;
	protected ScannerContext fContext;
	protected StringBuilder fTokenBuffer= new StringBuilder();
	private int fPreprocessorToken= 0;
	private boolean fReuseToken;
	private boolean fSplitPreprocessor;
	private final StringBuilder fUniversalCharBuffer= new StringBuilder();

	public SimpleScanner() {
		super();
	}

	public void setReuseToken(boolean val) {
	    fReuseToken= val;
	    if (val) {
	        fCurrentToken= new Token(0, null);
	    }
	}

	public void setSplitPreprocessor(boolean val) {
	    fSplitPreprocessor= val;
	}

	protected void init(Reader reader, String filename) {
	    fReuseToken= false;
	    fSplitPreprocessor= true;
	    fPreprocessorToken= 0;
	    fContext = new ScannerContext().initialize(reader);
	}

	public SimpleScanner initialize(Reader reader, String filename) {
	    init(reader, filename);
	    return this;
	}

	public void cleanup() {
	    fContext= null;
	    fTokenBuffer= new StringBuilder();
	    fCurrentToken= null;
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
	    if (fPreprocessorToken==0) {
	        fPreprocessorToken= categorizePreprocessor(fTokenBuffer);
	    }
	    return newToken(fPreprocessorToken);
	}

	private int categorizePreprocessor(StringBuilder text) {
	    boolean skipHash= true;
	    int i= 0;
	    for (; i < text.length(); i++) {
	        char c= text.charAt(i);
	        if (!Character.isWhitespace(c)) {
	            if (!skipHash) {
	                break;
	            }
	            skipHash= false;
	            if (c != '#') {
	                break;
	            }
	        }
	    }
	    String innerText= text.substring(i);
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
	            Token token= continuePPDirective(c);
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
	        } else if (((c >= 'a') && (c <= 'z')) || ((c >= 'A') && (c <= 'Z')) || (c == '_') || (c > 255 && Character.isUnicodeIdentifierStart(c))) {
	            madeMistake = false;
	
	            c = getChar();
	
	            while (((c >= 'a') && (c <= 'z'))
	                || ((c >= 'A') && (c <= 'Z'))
	                || ((c >= '0') && (c <= '9'))
	                || (c == '_')
	                || (c > 255 && Character.isUnicodeIdentifierPart(c))) {
	                c = getChar();
	            }
	
	            ungetChar(c);
	
	            String ident = fTokenBuffer.toString();
	
	            Object tokenTypeObject;
	
	            tokenTypeObject = fgKeywords.get(ident);
	
	            int tokenType = Token.tIDENTIFIER;
	            if (tokenTypeObject != null)
	                tokenType = ((Integer)tokenTypeObject).intValue();
	
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
	
                int digits= 0;
                
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
	            			if ((c= getChar()) == '.') {
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
	                    matchSinglelineComment();
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
	    		} while ((next >= '0' && next <= '9') || (next >= 'a' && next <= 'f')
	    			|| (next >= 'A' && next <= 'F'));
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
	    boolean escaped= false;
	    int c = getChar(true);
	
	    LOOP: for (;;) {
	        if (c == EOFCHAR)
	            break;
	        if (escaped) {
	            escaped= false;
	            int nc= getChar(true);
	            if (c=='\r' && nc=='\n') {
	                nc= getChar(true);
	            }
	            c= nc;
	        } else {
	            switch(c) {
	                case '\\':
	                    escaped= true;
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
	    LOOP:
	    for (;;) {
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
	    boolean done= false;
	    while (!done) {
	        switch(c) {
	            case '\'':
	                if (fTokenBuffer.length() > 1) {
	                    if (fPreprocessorToken == 0) {
	                        fPreprocessorToken= categorizePreprocessor(fTokenBuffer);
	                    }
	                    ungetChar(c);
	                    return newPreprocessorToken();
	                }
	                matchCharLiteral();
	                return newToken(Token.tCHAR);
	                    
	            case '"':
	                if (fTokenBuffer.length() > 1) {
	                    if (fPreprocessorToken==0) {
	                        fPreprocessorToken= categorizePreprocessor(fTokenBuffer);
	                    }
	                    if (fPreprocessorToken==Token.tPREPROCESSOR_INCLUDE) {
	                        matchStringLiteral();
	                        c= getChar();
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
	                    Token result= null;
	                    if (fTokenBuffer.length() > 2) {
	                        ungetChar(next);
	                        ungetChar(c);
	                        result= newPreprocessorToken();
	                    } else {
	                        matchSinglelineComment();
	                        result= newToken(Token.tLINECOMMENT);
	                    }
	                    fPreprocessorToken= 0;
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
	                        fPreprocessorToken= 0;
	                    }
	                    return newToken(Token.tBLOCKCOMMENT);
	                }
	                c = next;
	                break;
	            }
	            case '\n':
	            case '\r':
	            case EOFCHAR:
	                done= true;
	                break;
	            default:
	                c= getChar();
	                break;
	        }
	    }
	    ungetChar(c);
	    Token result= null;
	    if (fTokenBuffer.length() > 0) {
	        result= newPreprocessorToken();
	    }
	    fPreprocessorToken= 0;
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
	                matchSinglelineComment();
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

	private void matchSinglelineComment() {
	    int c = getChar();
	    while (c != '\n' && c != EOFCHAR) {
	        c = getChar();
	    }
	    if (c == EOFCHAR) {
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
	            case 0 :
	                if (c == '*')
	                    state = 1;
	                break;
	            case 1 :
	                if (c == '/')
	                    state = 2;
	                else if (c != '*')
	                    state = 0;
	                break;
	        }
	        c = getChar();
	    }
	    ungetChar(c);
	    return encounteredNewline;
	}

	static {
        fgKeywords.put("and", new Integer(Token.t_and)); //$NON-NLS-1$
        fgKeywords.put("and_eq", new Integer(Token.t_and_eq)); //$NON-NLS-1$
        fgKeywords.put("asm", new Integer(Token.t_asm)); //$NON-NLS-1$
        fgKeywords.put("auto", new Integer(Token.t_auto)); //$NON-NLS-1$
        fgKeywords.put("bitand", new Integer(Token.t_bitand)); //$NON-NLS-1$
        fgKeywords.put("bitor", new Integer(Token.t_bitor)); //$NON-NLS-1$
        fgKeywords.put("bool", new Integer(Token.t_bool)); //$NON-NLS-1$
        fgKeywords.put("break", new Integer(Token.t_break)); //$NON-NLS-1$
        fgKeywords.put("case", new Integer(Token.t_case)); //$NON-NLS-1$
        fgKeywords.put("catch", new Integer(Token.t_catch)); //$NON-NLS-1$
        fgKeywords.put("char", new Integer(Token.t_char)); //$NON-NLS-1$
        fgKeywords.put("class", new Integer(Token.t_class)); //$NON-NLS-1$
        fgKeywords.put("compl", new Integer(Token.t_compl)); //$NON-NLS-1$
        fgKeywords.put("const", new Integer(Token.t_const)); //$NON-NLS-1$
        fgKeywords.put("const_cast", new Integer(Token.t_const_cast)); //$NON-NLS-1$
        fgKeywords.put("continue", new Integer(Token.t_continue)); //$NON-NLS-1$
        fgKeywords.put("default", new Integer(Token.t_default)); //$NON-NLS-1$
        fgKeywords.put("delete", new Integer(Token.t_delete)); //$NON-NLS-1$
        fgKeywords.put("do", new Integer(Token.t_do)); //$NON-NLS-1$
        fgKeywords.put("double", new Integer(Token.t_double)); //$NON-NLS-1$
        fgKeywords.put("dynamic_cast", new Integer(Token.t_dynamic_cast)); //$NON-NLS-1$
        fgKeywords.put("else", new Integer(Token.t_else)); //$NON-NLS-1$
        fgKeywords.put("enum", new Integer(Token.t_enum)); //$NON-NLS-1$
        fgKeywords.put("explicit", new Integer(Token.t_explicit)); //$NON-NLS-1$
        fgKeywords.put("export", new Integer(Token.t_export)); //$NON-NLS-1$
        fgKeywords.put("extern", new Integer(Token.t_extern)); //$NON-NLS-1$
        fgKeywords.put("false", new Integer(Token.t_false)); //$NON-NLS-1$
        fgKeywords.put("float", new Integer(Token.t_float)); //$NON-NLS-1$
        fgKeywords.put("for", new Integer(Token.t_for)); //$NON-NLS-1$
        fgKeywords.put("friend", new Integer(Token.t_friend)); //$NON-NLS-1$
        fgKeywords.put("goto", new Integer(Token.t_goto)); //$NON-NLS-1$
        fgKeywords.put("if", new Integer(Token.t_if)); //$NON-NLS-1$
        fgKeywords.put("inline", new Integer(Token.t_inline)); //$NON-NLS-1$
        fgKeywords.put("int", new Integer(Token.t_int)); //$NON-NLS-1$
        fgKeywords.put("long", new Integer(Token.t_long)); //$NON-NLS-1$
        fgKeywords.put("mutable", new Integer(Token.t_mutable)); //$NON-NLS-1$
        fgKeywords.put("namespace", new Integer(Token.t_namespace)); //$NON-NLS-1$
        fgKeywords.put("new", new Integer(Token.t_new)); //$NON-NLS-1$
        fgKeywords.put("not", new Integer(Token.t_not)); //$NON-NLS-1$
        fgKeywords.put("not_eq", new Integer(Token.t_not_eq)); //$NON-NLS-1$
        fgKeywords.put("operator", new Integer(Token.t_operator)); //$NON-NLS-1$
        fgKeywords.put("or", new Integer(Token.t_or)); //$NON-NLS-1$
        fgKeywords.put("or_eq", new Integer(Token.t_or_eq)); //$NON-NLS-1$
        fgKeywords.put("private", new Integer(Token.t_private)); //$NON-NLS-1$
        fgKeywords.put("protected", new Integer(Token.t_protected)); //$NON-NLS-1$
        fgKeywords.put("public", new Integer(Token.t_public)); //$NON-NLS-1$
        fgKeywords.put("register", new Integer(Token.t_register)); //$NON-NLS-1$
        fgKeywords.put("reinterpret_cast", new Integer(Token.t_reinterpret_cast)); //$NON-NLS-1$
        fgKeywords.put("return", new Integer(Token.t_return)); //$NON-NLS-1$
        fgKeywords.put("short", new Integer(Token.t_short)); //$NON-NLS-1$
        fgKeywords.put("signed", new Integer(Token.t_signed)); //$NON-NLS-1$
        fgKeywords.put("sizeof", new Integer(Token.t_sizeof)); //$NON-NLS-1$
        fgKeywords.put("static", new Integer(Token.t_static)); //$NON-NLS-1$
        fgKeywords.put("static_cast", new Integer(Token.t_static_cast)); //$NON-NLS-1$
        fgKeywords.put("struct", new Integer(Token.t_struct)); //$NON-NLS-1$
        fgKeywords.put("switch", new Integer(Token.t_switch)); //$NON-NLS-1$
        fgKeywords.put("template", new Integer(Token.t_template)); //$NON-NLS-1$
        fgKeywords.put("this", new Integer(Token.t_this)); //$NON-NLS-1$
        fgKeywords.put("throw", new Integer(Token.t_throw)); //$NON-NLS-1$
        fgKeywords.put("true", new Integer(Token.t_true)); //$NON-NLS-1$
        fgKeywords.put("try", new Integer(Token.t_try)); //$NON-NLS-1$
        fgKeywords.put("typedef", new Integer(Token.t_typedef)); //$NON-NLS-1$
        fgKeywords.put("typeid", new Integer(Token.t_typeid)); //$NON-NLS-1$
        fgKeywords.put("typename", new Integer(Token.t_typename)); //$NON-NLS-1$
        fgKeywords.put("union", new Integer(Token.t_union)); //$NON-NLS-1$
        fgKeywords.put("unsigned", new Integer(Token.t_unsigned)); //$NON-NLS-1$
        fgKeywords.put("using", new Integer(Token.t_using)); //$NON-NLS-1$
        fgKeywords.put("virtual", new Integer(Token.t_virtual)); //$NON-NLS-1$
        fgKeywords.put("void", new Integer(Token.t_void)); //$NON-NLS-1$
        fgKeywords.put("volatile", new Integer(Token.t_volatile)); //$NON-NLS-1$
        fgKeywords.put("wchar_t", new Integer(Token.t_wchar_t)); //$NON-NLS-1$
        fgKeywords.put("while", new Integer(Token.t_while)); //$NON-NLS-1$
        fgKeywords.put("xor", new Integer(Token.t_xor)); //$NON-NLS-1$
        fgKeywords.put("xor_eq", new Integer(Token.t_xor_eq)); //$NON-NLS-1$

        // additional java keywords
        fgKeywords.put("abstract", new Integer(Token.t_abstract)); //$NON-NLS-1$
        fgKeywords.put("boolean", new Integer(Token.t_boolean)); //$NON-NLS-1$
        fgKeywords.put("byte", new Integer(Token.t_byte)); //$NON-NLS-1$
        fgKeywords.put("extends", new Integer(Token.t_extends)); //$NON-NLS-1$
        fgKeywords.put("final", new Integer(Token.t_final)); //$NON-NLS-1$
        fgKeywords.put("finally", new Integer(Token.t_finally)); //$NON-NLS-1$
        fgKeywords.put("implements", new Integer(Token.t_implements)); //$NON-NLS-1$
        fgKeywords.put("import", new Integer(Token.t_import)); //$NON-NLS-1$
        fgKeywords.put("interface", new Integer(Token.t_interface)); //$NON-NLS-1$
        fgKeywords.put("instanceof", new Integer(Token.t_instanceof)); //$NON-NLS-1$
        fgKeywords.put("native", new Integer(Token.t_native)); //$NON-NLS-1$
        fgKeywords.put("null", new Integer(Token.t_null)); //$NON-NLS-1$
        fgKeywords.put("package", new Integer(Token.t_package)); //$NON-NLS-1$
        fgKeywords.put("super", new Integer(Token.t_super)); //$NON-NLS-1$
        fgKeywords.put("synchronized", new Integer(Token.t_synchronized)); //$NON-NLS-1$
        fgKeywords.put("throws", new Integer(Token.t_throws)); //$NON-NLS-1$
        fgKeywords.put("transient", new Integer(Token.t_transient)); //$NON-NLS-1$
    }
}