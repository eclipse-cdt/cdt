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
 * This interface represents a binary expression.  
 * 
 * @author Doug Schaefer
 */
public interface IASTBinaryExpression extends IASTExpression {

    /**
     * Node property that describes the relationship between an <code>IASTBinaryExpression</code> and an <code>IASTExpression</code> representing the lhs.
     */
    public static final ASTNodeProperty OPERAND_ONE = new ASTNodeProperty( "Operand 1"); //$NON-NLS-1$
    /**
     * Node property that describes the relationship between an <code>IASTBinaryExpression</code> and an <code>IASTExpression</code> representing the rhs.
     */
    public static final ASTNodeProperty OPERAND_TWO = new ASTNodeProperty( "Operand 2"); //$NON-NLS-1$
    
    /**
     * Set the operator. 
     * @param op  Value to set.
     */
    public void setOperator( int op );
	/**
	 * Get the operator. 
	 * @return int value as operator
	 */
	public int getOperator();
	
	
	/**
	 * multiply *
	 */
	public static final int op_multiply = 1;
	/**
	 * divide /
	 */
	public static final int op_divide = 2;
	
	/**
	 * modulo %
	 */
	public static final int op_modulo = 3;
	
	/**
	 * plus +
	 */
	public static final int op_plus = 4;
	
	/**
	 * minus -
	 */
	public static final int op_minus = 5;
	
	/**
	 * shift left <<
	 */
	public static final int op_shiftLeft = 6;
	
	/**
	 * shift right >>
	 */
	public static final int op_shiftRight = 7;
	
	/**
	 * less than < 
	 */
	public static final int op_lessThan = 8;
	
	/**
	 * greater than >
	 */
	public static final int op_greaterThan = 9;
	
	/**
	 * less than or equals <=
	 */
	public static final int op_lessEqual = 10;
	
	/**
	 * greater than or equals >=
	 */
	public static final int op_greaterEqual = 11;
	
	/**
	 * binary and &
	 */
	public static final int op_binaryAnd = 12;
	
	/**
	 * binary Xor ^
	 */
	public static final int op_binaryXor = 13;
	
	/**
	 * binary Or |
	 */
	public static final int op_binaryOr = 14;
	
	/**
	 * logical and &&
	 */
	public static final int op_logicalAnd = 15;
	
	/**
	 * logical or || 
	 */
	public static final int op_logicalOr = 16;
	
	/**
	 * assignment = 
	 */
	public static final int op_assign = 17;
	
	/**
	 * multiply assignment *=
	 */
	public static final int op_multiplyAssign = 18;
	
	/**
	 * divide assignemnt /=
	 */
	public static final int op_divideAssign = 19;
	
	/**
	 * modulo assignment %=
	 */
	public static final int op_moduloAssign = 20;
	
	/**
	 * plus assignment +=
	 */
	public static final int op_plusAssign = 21;
	
	/**
	 * minus assignment -=
	 */
	public static final int op_minusAssign = 22;
	
	/**
	 * shift left assignment <<=
	 */
	public static final int op_shiftLeftAssign = 23;
	
	/**
	 * shift right assign >>=
	 */
	public static final int op_shiftRightAssign = 24;
	
	/**
	 * binary and assign &=
	 */
	public static final int op_binaryAndAssign = 25;
	
	/**
	 * binary Xor assign ^=
	 */
	public static final int op_binaryXorAssign = 26;
	
	/**
	 * binary Or assign |=
	 */
	public static final int op_binaryOrAssign = 27;
	
	/**
	 * equals == 
	 */
	public static final int op_equals = 28;
	
	/**
	 * not equals !=
	 */
	public static final int op_notequals = 29;
	
	/**
	 * op_last is the field used in subinterfaces to start their operators at
	 */
	public static final int op_last = op_notequals;
    
	/**
	 * Get the first operand.
	 * 
	 * @return <code>IASTExpression</code> representing operand 1.
	 */
	public IASTExpression getOperand1();
	/**
	 * Set the first operand. 
	 * 
	 * @param expression <code>IASTExpression</code> value.
	 */
	
	public void setOperand1( IASTExpression expression );
	/**
	 * Get the second operand.
	 * 
	 * @return <code>IASTExpression</code> representing operand 2.
	 */
	public IASTExpression getOperand2();
	/**
	 * @param expression <code>IASTExpression</code> value
	 */
	public void setOperand2( IASTExpression expression );
}
