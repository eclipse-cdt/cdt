/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * @author Doug Schaefer
 */
public interface IASTBinaryExpression extends IASTExpression {

    public static final ASTNodeProperty OPERAND_ONE = new ASTNodeProperty( "Operand 1"); //$NON-NLS-1$
    public static final ASTNodeProperty OPERAND_TWO = new ASTNodeProperty( "Operand 2"); //$NON-NLS-1$
    
    public void setOperator( int op );
	public int getOperator();
	public static final int op_multiply = 1;
	public static final int op_divide = 2;
	public static final int op_modulo = 3;
	public static final int op_plus = 4;
	public static final int op_minus = 5;
	public static final int op_shiftLeft = 6;
	public static final int op_shiftRight = 7;
	public static final int op_lessThan = 8;
	public static final int op_greaterThan = 9;
	public static final int op_lessEqual = 10;
	public static final int op_greaterEqual = 11;
	public static final int op_binaryAnd = 12;
	public static final int op_binaryXor = 13;
	public static final int op_binaryOr = 14;
	public static final int op_logicalAnd = 15;
	public static final int op_logicalOr = 16;
	public static final int op_assign = 17;
	public static final int op_multiplyAssign = 18;
	public static final int op_divideAssign = 19;
	public static final int op_moduloAssign = 20;
	public static final int op_plusAssign = 21;
	public static final int op_minusAssign = 22;
	public static final int op_shiftLeftAssign = 23;
	public static final int op_shiftRightAssign = 24;
	public static final int op_binaryAndAssign = 25;
	public static final int op_binaryXorAssign = 26;
	public static final int op_binaryOrAssign = 27;
	public static final int op_equals = 28;
	public static final int op_notequals = 29;
	public static final int op_last = op_notequals;
    
	public IASTExpression getOperand1();
	public void setOperand1( IASTExpression expression );
	public IASTExpression getOperand2();
	public void setOperand2( IASTExpression expression );
}
