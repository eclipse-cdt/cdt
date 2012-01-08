/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn (Wind River Systems) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 5.3
 */
public interface IASTBinaryTypeIdExpression extends IASTExpression {
	public static final ASTNodeProperty OPERAND1 = new ASTNodeProperty("IASTBinaryTypeIdExpression.OPERAND1 [IASTTypeId]"); //$NON-NLS-1$
	public static final ASTNodeProperty OPERAND2 = new ASTNodeProperty("IASTBinaryTypeIdExpression.OPERAND2 [IASTTypeId]"); //$NON-NLS-1$

	/**
	 * Built-in type trait of g++. 
	 */
	public static enum Operator {__is_base_of}
	
	/**
	 * Get the operator for the expression.
	 */
	public Operator getOperator();

	/**
	 * Returns the first operand.
	 */
	public IASTTypeId getOperand1();
	
	/**
	 * Returns the second operand, or <code>null</code> if it was not provided (content assist).
	 */
	public IASTTypeId getOperand2();
	
	/**
	 * Set the operator for the expression.
	 */
	public void setOperator(Operator value);

	/**
	 * Set the first operand.
	 */
	public void setOperand1(IASTTypeId typeId);

	/**
	 * Set the second operand.
	 */
	public void setOperand2(IASTTypeId typeId);

	@Override
	public IASTBinaryTypeIdExpression copy();
	@Override
	public IASTBinaryTypeIdExpression copy(CopyStyle style);
}
