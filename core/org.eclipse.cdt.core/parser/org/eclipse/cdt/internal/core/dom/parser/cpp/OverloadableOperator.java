/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.parser.IToken;

/**
 * Enumeration of all the overloadable operators in C++.
 * 
 * Note: toString() has not been overridden, use toCharArray() to get
 * a character representation of the operator.
 * 
 * @author Mike Kucera
 */
@SuppressWarnings("nls")
public enum OverloadableOperator {
	
	GT(">"), 
	LT("<"), 
	NOT("!"), 
	BITCOMPLEMENT("~"), 
	BITOR("|"), 
	AMPER("&"), 
	XOR("^"), 
	MOD("%"), 
	DIV("/"), 
	STAR("*"), 
	PLUS("+"), 
	BRACKET("[]"), 
	PAREN("()"), 
	ARROW("->"), 
	ARROWSTAR("->*"), 
	COMMA(","), 
	MINUS("-"), 
	DECR("--"), 
	INCR("++"), 
	OR("||"), 
	AND("&&"), 
	ASSIGN("="), 
	GTEQUAL(">="), 
	LTEQUAL("<="), 
	NOTEQUAL("!="), 
	EQUAL("=="), 
	SHIFTR(">>"), 
	SHIFTL("<<"), 
	SHIFTLASSIGN("<<="), 
	SHIFTRASSIGN(">>="), 
	BITORASSIGN("|="), 
	AMPERASSIGN("&="), 
	XORASSIGN("^="), 
	MODASSIGN("%="), 
	DIVASSIGN("/="), 
	STARASSIGN("*="), 
	MINUSASSIGN("-="), 
	PLUSASSIGN("+="), 
	NEW("new"), 
	DELETE_ARRAY("delete[]"), 
	DELETE("delete"), 
	NEW_ARRAY("new[]"); 
	
	
	private final char[] rep;
	
	private OverloadableOperator(String rep) {
		this.rep = ("operator " + rep).toCharArray();
	}
	
	public char[] toCharArray() {
		return rep;
	}
	
	/**
	 * Returns the OverloadableOperator constant that corresponds to the
	 * given token. Only works for operators that consist of one token.
	 * 
	 * @throws NullPointerException if token is null
	 */
	public static OverloadableOperator valueOf(IToken token) {
		switch(token.getType()) {
			case IToken.t_delete:       return DELETE;
			case IToken.t_new:          return NEW;
			case IToken.tAMPER:         return AMPER;
			case IToken.tAMPERASSIGN:   return AMPERASSIGN;
			case IToken.tARROW:         return ARROW;
			case IToken.tARROWSTAR:     return ARROWSTAR;
			case IToken.tBITOR:         return BITOR;
			case IToken.tBITORASSIGN:   return BITORASSIGN;
			case IToken.tBITCOMPLEMENT: return BITCOMPLEMENT;
			case IToken.tSHIFTL:        return SHIFTL;
			case IToken.tSHIFTLASSIGN:  return SHIFTLASSIGN;
			case IToken.tSHIFTR:        return SHIFTR;
			case IToken.tSHIFTRASSIGN:  return SHIFTRASSIGN;
			case IToken.tXOR:           return XOR;
			case IToken.tXORASSIGN:     return XORASSIGN;
			
	        // logical operations
			case IToken.tNOT: return NOT;
			case IToken.tAND: return AND;
			case IToken.tOR:  return OR;

			// arithmetic
			case IToken.tDECR:        return DECR;
			case IToken.tINCR:        return INCR;
			case IToken.tDIV:         return DIV;
			case IToken.tDIVASSIGN:   return DIVASSIGN;
			case IToken.tMINUS:       return MINUS;
			case IToken.tMINUSASSIGN: return MINUSASSIGN;
			case IToken.tMOD:         return MOD;
			case IToken.tMODASSIGN:   return MODASSIGN;
			case IToken.tPLUS:        return PLUS;
			case IToken.tPLUSASSIGN:  return PLUSASSIGN;
			case IToken.tSTAR:        return STAR;
			case IToken.tSTARASSIGN:  return STARASSIGN;
				
			// comparison
			case IToken.tEQUAL:    return EQUAL; 
			case IToken.tNOTEQUAL: return NOTEQUAL;
			case IToken.tGT:       return GT;
			case IToken.tGTEQUAL:  return GTEQUAL;
			case IToken.tLT:       return LT;
			case IToken.tLTEQUAL:  return LTEQUAL;
				
			// other
			case IToken.tASSIGN: return ASSIGN; 
			case IToken.tCOMMA:  return COMMA;
		}
		
		return null;
	}
}
