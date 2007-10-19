/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial implementation
 *     Markus Schorn (Wind River Systems)
 *     Bryan Wilkinson (QNX) - https://bugs.eclipse.org/bugs/show_bug.cgi?id=151207
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser.scanner;

import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.util.CharArrayObjectMap;

/**
 * Used to evaluate expressions in preprocessor directives.
 * @since 5.0
 */
class ExpressionEvaluator {
    static class EvalException extends Exception {
        private int fProblemID;
        private char[] fProblemArg;

		public EvalException(int problemID, char[] problemArg) {
        	fProblemID= problemID;
        }
		
		public int getProblemID() {
			return fProblemID;
		}

		public char[] getProblemArg() {
			return fProblemArg;
		}
    }

	private Token fTokens;
	private CharArrayObjectMap fDictionary;

	public boolean evaluate(Token condition, CharArrayObjectMap dictionary) throws EvalException {
		fTokens= condition;
		fDictionary= dictionary;
		return expression() != 0;
	}


    private long expression() throws EvalException {
        return conditionalExpression();
    }

    private long conditionalExpression() throws EvalException {
        long r1 = logicalOrExpression();
        if (LA() == IToken.tQUESTION) {
            consume();
            long r2 = expression();
            if (LA() == IToken.tCOLON)
                consume();
            else {
                throw new EvalException(IProblem.SCANNER_BAD_CONDITIONAL_EXPRESSION, null); 
            }
            long r3 = conditionalExpression();
            return r1 != 0 ? r2 : r3;
        }
        return r1;
    }

    private long logicalOrExpression() throws EvalException {
        long r1 = logicalAndExpression();
        while (LA() == IToken.tOR) {
            consume();
            long r2 = logicalAndExpression();
            r1 = ((r1 != 0) || (r2 != 0)) ? 1 : 0;
        }
        return r1;
    }

    private long logicalAndExpression() throws EvalException {
        long r1 = inclusiveOrExpression();
        while (LA() == IToken.tAND) {
            consume();
            long r2 = inclusiveOrExpression();
            r1 = ((r1 != 0) && (r2 != 0)) ? 1 : 0;
        }
        return r1;
    }

    private long inclusiveOrExpression() throws EvalException {
        long r1 = exclusiveOrExpression();
        while (LA() == IToken.tBITOR) {
            consume();
            long r2 = exclusiveOrExpression();
            r1 = r1 | r2;
        }
        return r1;
    }

    private long exclusiveOrExpression() throws EvalException {
        long r1 = andExpression();
        while (LA() == IToken.tXOR) {
            consume();
            long r2 = andExpression();
            r1 = r1 ^ r2;
        }
        return r1;
    }

    private long andExpression() throws EvalException {
        long r1 = equalityExpression();
        while (LA() == IToken.tAMPER) {
            consume();
            long r2 = equalityExpression();
            r1 = r1 & r2;
        }
        return r1;
    }

    private long equalityExpression() throws EvalException {
        long r1 = relationalExpression();
        for (int t = LA(); t == IToken.tEQUAL || t == IToken.tNOTEQUAL; t = LA()) {
            consume();
            long r2 = relationalExpression();
            if (t == IToken.tEQUAL)
                r1 = (r1 == r2) ? 1 : 0;
            else
                // t == tNOTEQUAL
                r1 = (r1 != r2) ? 1 : 0;
        }
        return r1;
    }

    private long relationalExpression() throws EvalException {
        long r1 = shiftExpression();
        for (int t = LA(); t == IToken.tLT || t == IToken.tLTEQUAL || t == IToken.tGT
                || t == IToken.tGTEQUAL; t = LA()) {
            consume();
            long r2 = shiftExpression();
            switch (t) {
            case IToken.tLT:
                r1 = (r1 < r2) ? 1 : 0;
                break;
            case IToken.tLTEQUAL:
                r1 = (r1 <= r2) ? 1 : 0;
                break;
            case IToken.tGT:
                r1 = (r1 > r2) ? 1 : 0;
                break;
            case IToken.tGTEQUAL:
                r1 = (r1 >= r2) ? 1 : 0;
                break;
            }
        }
        return r1;
    }

    private long shiftExpression() throws EvalException {
        long r1 = additiveExpression();
        for (int t = LA(); t == IToken.tSHIFTL || t == IToken.tSHIFTR; t = LA()) {
            consume();
            long r2 = additiveExpression();
            if (t == IToken.tSHIFTL)
                r1 = r1 << r2;
            else
                // t == tSHIFTR
                r1 = r1 >> r2;
        }
        return r1;
    }

    private long additiveExpression() throws EvalException {
        long r1 = multiplicativeExpression();
        for (int t = LA(); t == IToken.tPLUS || t == IToken.tMINUS; t = LA()) {
            consume();
            long r2 = multiplicativeExpression();
            if (t == IToken.tPLUS)
                r1 = r1 + r2;
            else
                // t == tMINUS
                r1 = r1 - r2;
        }
        return r1;
    }

    private long multiplicativeExpression() throws EvalException {
        long r1 = unaryExpression();
        for (int t = LA(); t == IToken.tSTAR || t == IToken.tDIV || t == IToken.tMOD; t = LA()) {
            consume();
            long r2 = unaryExpression();
            if (t == IToken.tSTAR)
                r1 = r1 * r2;
            else if (r2 != 0) {
            	if (t == IToken.tDIV)
            		r1 = r1 / r2;
            	else
            		r1 = r1 % r2;	//tMOD
            } else {
                throw new EvalException(IProblem.SCANNER_DIVIDE_BY_ZERO, null); 
            }
        }
        return r1;
    }

    private long unaryExpression() throws EvalException {
        switch (LA()) {
        case IToken.tPLUS:
            consume();
            return unaryExpression();
        case IToken.tMINUS:
            consume();
            return -unaryExpression();
        case IToken.tNOT:
            consume();
            return unaryExpression() == 0 ? 1 : 0;
        case IToken.tBITCOMPLEMENT:
            consume();
            return ~unaryExpression();
        case IToken.tCHAR:
        case IToken.tLCHAR:
        case IToken.tINTEGER:
        	long val= getValue(fTokens);
        	consume();
        	return val;
        case CPreprocessor.tDEFINED:
            return handleDefined();
        case IToken.tLPAREN:
            consume();
            long r1 = expression();
            if (LA() == IToken.tRPAREN) {
                consume();
                return r1;
            }
            throw new EvalException(IProblem.SCANNER_MISSING_R_PAREN, null); 
        case IToken.tIDENTIFIER:
        	return 1;
        default:
            throw new EvalException(IProblem.SCANNER_EXPRESSION_SYNTAX_ERROR, null); 
        }
    }

    private long handleDefined() throws EvalException {
    	boolean parenthesis= false;
    	consume();
    	if (LA() == IToken.tLPAREN) {
    		parenthesis= true;
    		consume();
    	}
    	if (LA() != IToken.tIDENTIFIER) {
    		throw new EvalException(IProblem.SCANNER_ILLEGAL_IDENTIFIER, null);
    	}
    	int result= fDictionary.containsKey(fTokens.getCharImage()) ? 1 : 0;
    	consume();
    	if (parenthesis) {
    		if (LA() != IToken.tRPAREN) {
    			throw new EvalException(IProblem.SCANNER_MISSING_R_PAREN, null);
    		}
    		consume();
    	}
    	return result;
    }

    private int LA() {
    	return fTokens.getType();
    }

    private void consume() {
    	fTokens= (Token) fTokens.getNext();
    	if (fTokens == null) {
    		fTokens= new SimpleToken(Lexer.tEND_OF_INPUT, 0, 0);
    	}
    }
    
    long getValue(Token t) throws EvalException {
    	switch(t.getType()) {
    	case IToken.tCHAR:
    		return getChar(t.getCharImage(), 1);
    	case IToken.tLCHAR:
    		return getChar(t.getCharImage(), 2);
    	case IToken.tINTEGER:
    		return getNumber(t.getCharImage());
    	}
    	assert false;
    	return 1;
    }

	private long getNumber(char[] image) throws EvalException {
        boolean isHex = false;
        boolean isOctal = false;

        int pos= 0;
        if (image.length > 1) {
        	if (image[0] == '0') {
        		switch (image[++pos]) {
        		case 'x':
        		case 'X':
        			isHex = true;
        			++pos;
        			break;
        		case '0': case '1': case '2': case '3': case '4': case '5': case '6': case '7': case '8': case '9':
        			isOctal = true;
        			++pos;
        			break;
        		}
        	}
        }
        if (isHex) {
        	return getNumber(image, 2, image.length, 16, IProblem.SCANNER_BAD_HEX_FORMAT);
        }
        if (isOctal) {
        	return getNumber(image, 1, image.length, 8, IProblem.SCANNER_BAD_OCTAL_FORMAT);
        }
    	return getNumber(image, 0, image.length, 10, IProblem.SCANNER_BAD_DECIMAL_FORMAT);
    }

	private long getChar(char[] tokenImage, int i) throws EvalException {
		if (i>=tokenImage.length) {
			throw new EvalException(IProblem.SCANNER_BAD_CHARACTER, tokenImage);
		}
		final char c= tokenImage[i];
		if (c != '\\') {
			return c;
		}

		if (++i>=tokenImage.length) {
			throw new EvalException(IProblem.SCANNER_BAD_CHARACTER, tokenImage);
		}
		final char d= tokenImage[i];
		switch(d) {
		case '\\': case '"': case '\'':
			return d;
		case 'a': return 7;
		case 'b': return '\b';
		case 'f': return '\f';
		case 'n': return '\n';
		case 'r': return '\r';
		case 't': return '\t';
		case 'v': return 0xb;
		
		case '0': case '1': case '2': case '3': case '4': case '5': case '6': case '7':
			return getNumber(tokenImage, i+1, tokenImage.length-1, 8, IProblem.SCANNER_BAD_OCTAL_FORMAT);
		
		case 'x': case 'u': case 'U':
			return getNumber(tokenImage, i+1, tokenImage.length-1, 16, IProblem.SCANNER_BAD_HEX_FORMAT);
		default:
			throw new EvalException(IProblem.SCANNER_BAD_CHARACTER, tokenImage);
		}
	}

	private long getNumber(char[] tokenImage, int from, int to, int base, int problemID) throws EvalException {
		long result= 0;
		for (int i = from; i < to; i++) {
			int digit= getDigit(tokenImage[i]);
			if (digit >= base) {
				throw new EvalException(problemID, tokenImage);
			}
			result= result*base + digit;
		}
		return result;
	}

	private int getDigit(char c) {
		switch(c) {
		case '0': case '1': case '2': case '3': case '4': case '5': case '6': case '7': case '8': case '9':
			return c-'0';
		case 'a': case 'b': case 'c': case 'd': case 'e': case 'f':
			return c-'a' + 10;
		case 'A': case 'B': case 'C': case 'D': case 'E': case 'F':
			return c-'A'+10;
		}
		return Integer.MAX_VALUE;
	}
}
