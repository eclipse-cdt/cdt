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
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTOperatorName;

/**
 * The imlemented ICPPASTOperatorName.
 * 
 * @author dsteffle
 */
public class CPPASTOperatorName extends CPPASTName implements ICPPASTOperatorName {
	private static final int FIRST_CHAR_AFTER_OPERATOR = 8;

	private static final String LBRACKET = "["; //$NON-NLS-1$
	private static final String RBRACKET = "]"; //$NON-NLS-1$
	private static final String LPAREN = "("; //$NON-NLS-1$
	private static final String RPAREN = ")"; //$NON-NLS-1$
	
	private boolean wasSet=false;
	
	/**
	 * Default constructor.
	 *
	 */
	public CPPASTOperatorName() {
		super();
	}
	
	/**
	 * Primary consturctor that should be used to initialize the CPPASTOperatorName.
	 * @param name the char[] name to initialize to
	 */
	public CPPASTOperatorName(char[] name) {
		super(name);
		wasSet=true;
		super.setName(enforceOpStandard(super.toString()));
	}

	/**
	 * Returns the char[] of the name and ensures that the name will be standardized
	 * so that it will be of the form "operator op".
	 * @return 
	 */
	public char[] toCharArray() {
    	if (!wasSet) {
			super.setName(enforceOpStandard(super.toString()));
			wasSet=true;
    	}
		
		return super.toCharArray();
    }
	
	/**
	 * Returns the String of the name and ensures that the name will be standardized
	 * so that it will be of the form "operator op".
	 * @return
	 */
	public String toString() {
		if (!wasSet) {
			super.setName(enforceOpStandard(super.toString()));
			wasSet=true;
		}
		
		return super.toString();
	}
	
	/**
	 * Sets the name of this CPPASTOperatorName and ensures that the name follows
	 * the standard "operator op".
	 * @param name
	 */
	public void setName(char[] name) {
		super.setName(enforceOpStandard(new String(name)));
		wasSet=true;
	}
	
	private char[] enforceOpStandard(String name) {
		// need to use indexOf (could optimize by skipping the first 8 chars?)
		if (name.indexOf(OP_NEW, FIRST_CHAR_AFTER_OPERATOR) >=FIRST_CHAR_AFTER_OPERATOR && name.indexOf(LBRACKET, FIRST_CHAR_AFTER_OPERATOR) >=FIRST_CHAR_AFTER_OPERATOR && name.indexOf(RBRACKET, FIRST_CHAR_AFTER_OPERATOR) >=FIRST_CHAR_AFTER_OPERATOR) {
			return OPERATOR_NEW_ARRAY;
		} else if (name.indexOf(OP_DELETE, FIRST_CHAR_AFTER_OPERATOR) >=FIRST_CHAR_AFTER_OPERATOR && name.indexOf(LBRACKET, FIRST_CHAR_AFTER_OPERATOR) >=FIRST_CHAR_AFTER_OPERATOR && name.indexOf(RBRACKET, FIRST_CHAR_AFTER_OPERATOR) >=FIRST_CHAR_AFTER_OPERATOR) {
			return OPERATOR_DELETE_ARRAY;
		} else if (name.indexOf(OP_NEW, FIRST_CHAR_AFTER_OPERATOR) >= FIRST_CHAR_AFTER_OPERATOR) {
			return OPERATOR_NEW;
		} else if (name.indexOf(OP_DELETE, FIRST_CHAR_AFTER_OPERATOR) >= FIRST_CHAR_AFTER_OPERATOR) {
			return OPERATOR_DELETE;
		} else if (name.indexOf(OP_PLUS_ASSIGN, FIRST_CHAR_AFTER_OPERATOR) >= FIRST_CHAR_AFTER_OPERATOR) {
			return OPERATOR_PLUS_ASSIGN;
		} else if (name.indexOf(OP_MINUS_ASSIGN, FIRST_CHAR_AFTER_OPERATOR) >= FIRST_CHAR_AFTER_OPERATOR) {
			return OPERATOR_MINUS_ASSIGN;
		} else if (name.indexOf(OP_STAR_ASSIGN, FIRST_CHAR_AFTER_OPERATOR) >= FIRST_CHAR_AFTER_OPERATOR) {
			return OPERATOR_STAR_ASSIGN;
		} else if (name.indexOf(OP_DIV_ASSIGN, FIRST_CHAR_AFTER_OPERATOR) >= FIRST_CHAR_AFTER_OPERATOR) {
			return OPERATOR_DIV_ASSIGN;
		} else if (name.indexOf(OP_MOD_ASSIGN, FIRST_CHAR_AFTER_OPERATOR) >= FIRST_CHAR_AFTER_OPERATOR) {
			return OPERATOR_MOD_ASSIGN;
		} else if (name.indexOf(OP_XOR_ASSIGN, FIRST_CHAR_AFTER_OPERATOR) >= FIRST_CHAR_AFTER_OPERATOR) {
			return OPERATOR_XOR_ASSIGN;
		} else if (name.indexOf(OP_AMPER_ASSIGN, FIRST_CHAR_AFTER_OPERATOR) >= FIRST_CHAR_AFTER_OPERATOR) {
			return OPERATOR_AMPER_ASSIGN;
		} else if (name.indexOf(OP_BITOR_ASSIGN, FIRST_CHAR_AFTER_OPERATOR) >= FIRST_CHAR_AFTER_OPERATOR) {
			return OPERATOR_BITOR_ASSIGN;
		} else if (name.indexOf(OP_SHIFTR_ASSIGN, FIRST_CHAR_AFTER_OPERATOR) >= FIRST_CHAR_AFTER_OPERATOR) {
			return OPERATOR_SHIFTR_ASSIGN;
		} else if (name.indexOf(OP_SHIFTL_ASSIGN, FIRST_CHAR_AFTER_OPERATOR) >= FIRST_CHAR_AFTER_OPERATOR) {
			return OPERATOR_SHIFTL_ASSIGN;
		} else if (name.indexOf(OP_SHIFTL, FIRST_CHAR_AFTER_OPERATOR) >= FIRST_CHAR_AFTER_OPERATOR) {
			return OPERATOR_SHIFTL;
		} else if (name.indexOf(OP_SHIFTR, FIRST_CHAR_AFTER_OPERATOR) >= FIRST_CHAR_AFTER_OPERATOR) {
			return OPERATOR_SHIFTR;
		} else if (name.indexOf(OP_EQUAL, FIRST_CHAR_AFTER_OPERATOR) >= FIRST_CHAR_AFTER_OPERATOR) {
			return OPERATOR_EQUAL;
		} else if (name.indexOf(OP_NOTEQUAL, FIRST_CHAR_AFTER_OPERATOR) >= FIRST_CHAR_AFTER_OPERATOR) {
			return OPERATOR_NOTEQUAL;
		} else if (name.indexOf(OP_LTEQUAL, FIRST_CHAR_AFTER_OPERATOR) >= FIRST_CHAR_AFTER_OPERATOR) {
			return OPERATOR_LTEQUAL;
		} else if (name.indexOf(OP_GTEQUAL, FIRST_CHAR_AFTER_OPERATOR) >= FIRST_CHAR_AFTER_OPERATOR) {
			return OPERATOR_GTEQUAL;
		} else if (name.indexOf(OP_ASSIGN, FIRST_CHAR_AFTER_OPERATOR) >= FIRST_CHAR_AFTER_OPERATOR) {
			return OPERATOR_ASSIGN;
		} else if (name.indexOf(OP_AND, FIRST_CHAR_AFTER_OPERATOR) >= FIRST_CHAR_AFTER_OPERATOR) {
			return OPERATOR_AND;
		} else if (name.indexOf(OP_OR, FIRST_CHAR_AFTER_OPERATOR) >= FIRST_CHAR_AFTER_OPERATOR) {
			return OPERATOR_OR;
		} else if (name.indexOf(OP_INCR, FIRST_CHAR_AFTER_OPERATOR) >= FIRST_CHAR_AFTER_OPERATOR) {
			return OPERATOR_INCR;
		} else if (name.indexOf(OP_DECR, FIRST_CHAR_AFTER_OPERATOR) >= FIRST_CHAR_AFTER_OPERATOR) {
			return OPERATOR_DECR;
		} else if (name.indexOf(OP_COMMA, FIRST_CHAR_AFTER_OPERATOR) >= FIRST_CHAR_AFTER_OPERATOR) {
			return OPERATOR_COMMA;
		} else if (name.indexOf(OP_ARROWSTAR, FIRST_CHAR_AFTER_OPERATOR) >= FIRST_CHAR_AFTER_OPERATOR) {
			return OPERATOR_ARROWSTAR;
		} else if (name.indexOf(OP_ARROW, FIRST_CHAR_AFTER_OPERATOR) >= FIRST_CHAR_AFTER_OPERATOR) {
			return OPERATOR_ARROW;
		} else if (name.indexOf(LPAREN, FIRST_CHAR_AFTER_OPERATOR) >= FIRST_CHAR_AFTER_OPERATOR && name.indexOf(RPAREN, FIRST_CHAR_AFTER_OPERATOR) >= FIRST_CHAR_AFTER_OPERATOR) {
			return OPERATOR_PAREN;
		} else if (name.indexOf(LBRACKET, FIRST_CHAR_AFTER_OPERATOR) >=FIRST_CHAR_AFTER_OPERATOR && name.indexOf(RBRACKET, FIRST_CHAR_AFTER_OPERATOR) >=FIRST_CHAR_AFTER_OPERATOR) {
			return OPERATOR_BRACKET;
		} else if (name.indexOf(OP_PLUS, FIRST_CHAR_AFTER_OPERATOR) >= FIRST_CHAR_AFTER_OPERATOR) {
			return OPERATOR_PLUS;
		} else if (name.indexOf(OP_MINUS, FIRST_CHAR_AFTER_OPERATOR) >= FIRST_CHAR_AFTER_OPERATOR) {
			return OPERATOR_MINUS;
		} else if (name.indexOf(OP_STAR, FIRST_CHAR_AFTER_OPERATOR) >= FIRST_CHAR_AFTER_OPERATOR) {
			return OPERATOR_STAR;
		} else if (name.indexOf(OP_DIV, FIRST_CHAR_AFTER_OPERATOR) >= FIRST_CHAR_AFTER_OPERATOR) {
			return OPERATOR_DIV;
		} else if (name.indexOf(OP_MOD, FIRST_CHAR_AFTER_OPERATOR) >= FIRST_CHAR_AFTER_OPERATOR) {
			return OPERATOR_MOD;
		} else if (name.indexOf(OP_XOR, FIRST_CHAR_AFTER_OPERATOR) >= FIRST_CHAR_AFTER_OPERATOR) {
			return OPERATOR_XOR;
		} else if (name.indexOf(OP_AMPER, FIRST_CHAR_AFTER_OPERATOR) >= FIRST_CHAR_AFTER_OPERATOR) {
			return OPERATOR_AMPER;
		} else if (name.indexOf(OP_BITOR, FIRST_CHAR_AFTER_OPERATOR) >= FIRST_CHAR_AFTER_OPERATOR) {
			return OPERATOR_BITOR;
		} else if (name.indexOf(OP_COMPL, FIRST_CHAR_AFTER_OPERATOR) >= FIRST_CHAR_AFTER_OPERATOR) {
			return OPERATOR_COMPL;
		} else if (name.indexOf(OP_NOT, FIRST_CHAR_AFTER_OPERATOR) >= FIRST_CHAR_AFTER_OPERATOR) {
			return OPERATOR_NOT;
		} else if (name.indexOf(OP_LT, FIRST_CHAR_AFTER_OPERATOR) >= FIRST_CHAR_AFTER_OPERATOR) {
			return OPERATOR_LT;
		} else if (name.indexOf(OP_GT, FIRST_CHAR_AFTER_OPERATOR) >= FIRST_CHAR_AFTER_OPERATOR) {
			return OPERATOR_GT;
		}
		
		return name.toCharArray();
	}
}
