/**********************************************************************
 * Copyright (c) 2002-2004 IBM Canada and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation */
package org.eclipse.cdt.core.dom.ast;

/**
 * This interface represents a cast expression of the form (TypeId)operand.
 * 
 * @author jcamelon
 */
public interface IASTCastExpression extends IASTExpression {

	/**
	 * <code>op_cast</code> represents a traditional cast.
	 */
	public static final int op_cast = 0;

	/**
	 * <code>op_last</code> for subinterfaces
	 */
	public static final int op_last = op_cast;

	/**
	 * Get the type of cast (as an operator).
	 * 
	 * @return operator
	 */
	public int getOperator();

	/**
	 * Set the operator (type of cast).
	 * 
	 * @param value
	 */
	public void setOperator(int value);

	/**
	 * <code>OPERAND</code> represents the relationship between a cast
	 * expression and the expression it is casting (operand).
	 */
	public static final ASTNodeProperty OPERAND = new ASTNodeProperty("Operand"); //$NON-NLS-1$

	/**
	 * Get expression being cast.
	 * 
	 * @return <code>IASTExpression</code> the expression being cast
	 */
	public IASTExpression getOperand();

	/**
	 * Set the expression being cast.
	 * 
	 * @param expression
	 *            <code>IASTExpression</code> the expression to be cast
	 */
	public void setOperand(IASTExpression expression);

	/**
	 * <code>TYPE_ID</code> represents the relationship between a cast
	 * expression and the type cast to.
	 */
	public static final ASTNodeProperty TYPE_ID = new ASTNodeProperty("Type Id"); //$NON-NLS-1$

	/**
	 * Set the typeId.
	 * 
	 * @param typeId
	 *            <code>IASTTypeId</code> to be set.
	 */
	public void setTypeId(IASTTypeId typeId);

	/**
	 * Get the typeId.
	 * 
	 * @return <code>IASTTypeId</code> representing type being casted to.
	 */
	public IASTTypeId getTypeId();

}
