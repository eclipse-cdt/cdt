/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.IASTName;

/**
 * This interface represents a C++ overloaded operator member function.
 * 
 * @author dsteffle
 */
public interface ICPPASTOperatorName extends IASTName {
	public static final String OPERATOR = "operator "; //$NON-NLS-1$
	
	public static final String OP_GT = ">"; //$NON-NLS-1$
	public static final char[] OPERATOR_GT = new String(OPERATOR + OP_GT).toCharArray();
	public static final String OP_LT = "<"; //$NON-NLS-1$
	public static final char[] OPERATOR_LT = new String(OPERATOR + OP_LT).toCharArray();
	public static final String OP_NOT = "!"; //$NON-NLS-1$
	public static final char[] OPERATOR_NOT = new String(OPERATOR + OP_NOT).toCharArray();
	public static final String OP_COMPL = "~"; //$NON-NLS-1$
	public static final char[] OPERATOR_COMPL = new String(OPERATOR + OP_COMPL).toCharArray();
	public static final String OP_BITOR = "|"; //$NON-NLS-1$
	public static final char[] OPERATOR_BITOR = new String(OPERATOR + OP_BITOR).toCharArray();
	public static final String OP_AMPER = "&"; //$NON-NLS-1$
	public static final char[] OPERATOR_AMPER = new String(OPERATOR + OP_AMPER).toCharArray();
	public static final String OP_XOR = "^"; //$NON-NLS-1$
	public static final char[] OPERATOR_XOR = new String(OPERATOR + OP_XOR).toCharArray();
	public static final String OP_MOD = "%"; //$NON-NLS-1$
	public static final char[] OPERATOR_MOD = new String(OPERATOR + OP_MOD).toCharArray();
	public static final String OP_DIV = "/"; //$NON-NLS-1$
	public static final char[] OPERATOR_DIV = new String(OPERATOR + OP_DIV).toCharArray();
	public static final String OP_STAR = "*"; //$NON-NLS-1$
	public static final char[] OPERATOR_STAR = new String(OPERATOR + OP_STAR).toCharArray();
	public static final String OP_PLUS = "+"; //$NON-NLS-1$
	public static final char[] OPERATOR_PLUS = new String(OPERATOR + OP_PLUS).toCharArray();
	public static final String OP_BRACKET = "[]"; //$NON-NLS-1$
	public static final char[] OPERATOR_BRACKET = new String(OPERATOR + OP_BRACKET).toCharArray();
	public static final String OP_PAREN = "()"; //$NON-NLS-1$
	public static final char[] OPERATOR_PAREN = new String(OPERATOR + OP_PAREN).toCharArray();
	public static final String OP_ARROW = "->"; //$NON-NLS-1$
	public static final char[] OPERATOR_ARROW = new String(OPERATOR + OP_ARROW).toCharArray();
	public static final String OP_ARROWSTAR = "->*"; //$NON-NLS-1$
	public static final char[] OPERATOR_ARROWSTAR = new String(OPERATOR + OP_ARROWSTAR).toCharArray();
	public static final String OP_COMMA = ","; //$NON-NLS-1$
	public static final char[] OPERATOR_COMMA = new String(OPERATOR + OP_COMMA).toCharArray();
	public static final String OP_MINUS = "-"; //$NON-NLS-1$
	public static final char[] OPERATOR_MINUS = new String(OPERATOR + OP_MINUS).toCharArray();
	public static final String OP_DECR = "--"; //$NON-NLS-1$
	public static final char[] OPERATOR_DECR = new String(OPERATOR + OP_DECR).toCharArray();
	public static final String OP_INCR = "++"; //$NON-NLS-1$
	public static final char[] OPERATOR_INCR = new String(OPERATOR + OP_INCR).toCharArray();
	public static final String OP_OR = "||"; //$NON-NLS-1$
	public static final char[] OPERATOR_OR = new String(OPERATOR + OP_OR).toCharArray();
	public static final String OP_AND = "&&"; //$NON-NLS-1$
	public static final char[] OPERATOR_AND = new String(OPERATOR + OP_AND).toCharArray();
	public static final String OP_ASSIGN = "="; //$NON-NLS-1$
	public static final char[] OPERATOR_ASSIGN = new String(OPERATOR + OP_ASSIGN).toCharArray();
	public static final String OP_GTEQUAL = ">="; //$NON-NLS-1$
	public static final char[] OPERATOR_GTEQUAL = new String(OPERATOR + OP_GTEQUAL).toCharArray();
	public static final String OP_LTEQUAL = "<="; //$NON-NLS-1$
	public static final char[] OPERATOR_LTEQUAL = new String(OPERATOR + OP_LTEQUAL).toCharArray();
	public static final String OP_NOTEQUAL = "!="; //$NON-NLS-1$
	public static final char[] OPERATOR_NOTEQUAL = new String(OPERATOR + OP_NOTEQUAL).toCharArray();
	public static final String OP_EQUAL = "=="; //$NON-NLS-1$
	public static final char[] OPERATOR_EQUAL = new String(OPERATOR + OP_EQUAL).toCharArray();
	public static final String OP_SHIFTR = ">>"; //$NON-NLS-1$
	public static final char[] OPERATOR_SHIFTR = new String(OPERATOR + OP_SHIFTR).toCharArray();
	public static final String OP_SHIFTL = "<<"; //$NON-NLS-1$
	public static final char[] OPERATOR_SHIFTL = new String(OPERATOR + OP_SHIFTL).toCharArray();
	public static final String OP_SHIFTL_ASSIGN = "<<="; //$NON-NLS-1$
	public static final char[] OPERATOR_SHIFTL_ASSIGN = new String(OPERATOR + OP_SHIFTL_ASSIGN).toCharArray();
	public static final String OP_SHIFTR_ASSIGN = ">>="; //$NON-NLS-1$
	public static final char[] OPERATOR_SHIFTR_ASSIGN = new String(OPERATOR + OP_SHIFTR_ASSIGN).toCharArray();
	public static final String OP_BITOR_ASSIGN = "|="; //$NON-NLS-1$
	public static final char[] OPERATOR_BITOR_ASSIGN = new String(OPERATOR + OP_BITOR_ASSIGN).toCharArray();
	public static final String OP_AMPER_ASSIGN = "&="; //$NON-NLS-1$
	public static final char[] OPERATOR_AMPER_ASSIGN = new String(OPERATOR + OP_AMPER_ASSIGN).toCharArray();
	public static final String OP_XOR_ASSIGN = "^="; //$NON-NLS-1$
	public static final char[] OPERATOR_XOR_ASSIGN = new String(OPERATOR + OP_XOR_ASSIGN).toCharArray();
	public static final String OP_MOD_ASSIGN = "%="; //$NON-NLS-1$
	public static final char[] OPERATOR_MOD_ASSIGN = new String(OPERATOR + OP_MOD_ASSIGN).toCharArray();
	public static final String OP_DIV_ASSIGN = "/="; //$NON-NLS-1$
	public static final char[] OPERATOR_DIV_ASSIGN = new String(OPERATOR + OP_DIV_ASSIGN).toCharArray();
	public static final String OP_STAR_ASSIGN = "*="; //$NON-NLS-1$
	public static final char[] OPERATOR_STAR_ASSIGN = new String(OPERATOR + OP_STAR_ASSIGN).toCharArray();
	public static final String OP_MINUS_ASSIGN = "-="; //$NON-NLS-1$
	public static final char[] OPERATOR_MINUS_ASSIGN = new String(OPERATOR + OP_MINUS_ASSIGN).toCharArray();
	public static final String OP_PLUS_ASSIGN = "+="; //$NON-NLS-1$
	public static final char[] OPERATOR_PLUS_ASSIGN = new String(OPERATOR + OP_PLUS_ASSIGN).toCharArray();
	public static final String OP_NEW = "new"; //$NON-NLS-1$
	public static final char[] OPERATOR_NEW = new String(OPERATOR + OP_NEW).toCharArray();
	public static final String OP_DELETE_ARRAY = "delete[]"; //$NON-NLS-1$
	public static final char[] OPERATOR_DELETE_ARRAY = new String(OPERATOR + OP_DELETE_ARRAY).toCharArray();
	public static final String OP_DELETE = "delete"; //$NON-NLS-1$
	public static final char[] OPERATOR_DELETE = new String(OPERATOR + OP_DELETE).toCharArray();
	public static final String OP_NEW_ARRAY = "new[]"; //$NON-NLS-1$
	public static final char[] OPERATOR_NEW_ARRAY = new String(OPERATOR + OP_NEW_ARRAY).toCharArray();
}
