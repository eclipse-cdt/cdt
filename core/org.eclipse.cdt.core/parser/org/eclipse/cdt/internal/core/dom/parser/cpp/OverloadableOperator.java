/*******************************************************************************
 *  Copyright (c) 2008, 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *     Mike Kucera (IBM)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import java.util.Arrays;

import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeleteExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression;
import org.eclipse.cdt.core.parser.IToken;

/**
 * Enumeration of all the overloadable operators in C++.
 * 
 * Note: toString() has not been overridden, use toCharArray() to get
 * a character representation of the operator.
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
	NEW_ARRAY("new[]"),
	
	/**
	 * Cannot be overloaded by the user, however overload resolution needs to be performed.
	 */
	CONDITIONAL_OPERATOR("?");
	
	private final char[] rep;
	
	private OverloadableOperator(String rep) {
		this.rep = ("operator " + rep).toCharArray();
	}
	
	public char[] toCharArray() {
		return rep;
	}
	
	
	/**
	 * Returns true if the given name equals 'operator new'
	 * or 'operator new[]'.
	 */
	public static boolean isNew(char[] name) {
		return Arrays.equals(name, NEW.rep) || Arrays.equals(name, NEW_ARRAY.rep);
	}
	
	/**
	 * Returns true if the given name equals 'operator delete'
	 * or 'operator delete[]'.
	 */
	public static boolean isDelete(char[] name) {
		return Arrays.equals(name, DELETE.rep) || Arrays.equals(name, DELETE_ARRAY.rep);
	}
	
	/**
	 * Returns the OverloadableOperator constant that corresponds to
	 * the given token. Does not work for new[] and delete[] operators.
	 * 
	 * @throws NullPointerException if {@code token} is {@code null}.
	 */
	public static OverloadableOperator valueOf(IToken token) {
		switch (token.getType()) {
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
		
		case IToken.tLBRACKET: return BRACKET;
		case IToken.tRBRACKET: return BRACKET;
		case IToken.tLPAREN: return PAREN;
		case IToken.tRPAREN: return PAREN;
		}
		
		return null;
	}

	/**
	 * Returns the OverloadableOperator constant that corresponds to
	 * the given binary expression.
	 * 
	 * @throws NullPointerException if {@code expression} is {@code null}.
	 */
	public static OverloadableOperator fromBinaryExpression(IASTBinaryExpression expression) {
		switch (expression.getOperator()) {
		case IASTBinaryExpression.op_binaryAnd:     	return AMPER;
		case IASTBinaryExpression.op_binaryAndAssign:   return AMPERASSIGN;
		case IASTBinaryExpression.op_pmarrow:         	return ARROW;
		case IASTBinaryExpression.op_binaryOr:			return BITOR;
		case IASTBinaryExpression.op_binaryOrAssign:	return BITORASSIGN;
		case IASTBinaryExpression.op_shiftLeft:         return SHIFTL;
		case IASTBinaryExpression.op_shiftLeftAssign:  	return SHIFTLASSIGN;
		case IASTBinaryExpression.op_shiftRight:        return SHIFTR;
		case IASTBinaryExpression.op_shiftRightAssign:  return SHIFTRASSIGN;
		case IASTBinaryExpression.op_binaryXor:         return XOR;
		case IASTBinaryExpression.op_binaryXorAssign:   return XORASSIGN;
		
        // logical operations
		case IASTBinaryExpression.op_logicalAnd: 		return AND;
		case IASTBinaryExpression.op_logicalOr:  		return OR;

		// arithmetic
		case IASTBinaryExpression.op_divide:        	return DIV;
		case IASTBinaryExpression.op_divideAssign:  	return DIVASSIGN;
		case IASTBinaryExpression.op_minus:       		return MINUS;
		case IASTBinaryExpression.op_minusAssign: 		return MINUSASSIGN;
		case IASTBinaryExpression.op_modulo:         	return MOD;
		case IASTBinaryExpression.op_moduloAssign:   	return MODASSIGN;
		case IASTBinaryExpression.op_plus:        		return PLUS;
		case IASTBinaryExpression.op_plusAssign:  		return PLUSASSIGN;
		case IASTBinaryExpression.op_multiply:        	return STAR;
		case IASTBinaryExpression.op_multiplyAssign:	return STARASSIGN;
			
		// comparison
		case IASTBinaryExpression.op_equals:    		return EQUAL; 
		case IASTBinaryExpression.op_notequals: 		return NOTEQUAL;
		case IASTBinaryExpression.op_greaterThan:       return GT;
		case IASTBinaryExpression.op_greaterEqual:  	return GTEQUAL;
		case IASTBinaryExpression.op_lessThan:       	return LT;
		case IASTBinaryExpression.op_lessEqual:  		return LTEQUAL;
			
		// other
		case IASTBinaryExpression.op_assign: 			return ASSIGN; 
		}
		
		return null;
	}
	
	public static OverloadableOperator fromUnaryExpression(IASTUnaryExpression expression) {
		switch(expression.getOperator()) {
		case IASTUnaryExpression.op_prefixIncr:  return INCR;
		case IASTUnaryExpression.op_prefixDecr:  return DECR;
		case IASTUnaryExpression.op_plus:        return PLUS;
		case IASTUnaryExpression.op_minus:       return MINUS;
		case IASTUnaryExpression.op_star:        return STAR;
		case IASTUnaryExpression.op_amper:       return AMPER;
		case IASTUnaryExpression.op_tilde:       return BITCOMPLEMENT;
		case IASTUnaryExpression.op_not:         return NOT;
		case IASTUnaryExpression.op_postFixIncr: return INCR;
		case IASTUnaryExpression.op_postFixDecr: return DECR;
		}
		return null;
	}
	
	public static OverloadableOperator fromDeleteExpression(ICPPASTDeleteExpression expression) {
		return expression.isVectored() ? DELETE_ARRAY : DELETE;
	}
	
	public static OverloadableOperator fromNewExpression(ICPPASTNewExpression expression) {
		return expression.isArrayAllocation() ? NEW_ARRAY : NEW;
	}
}
