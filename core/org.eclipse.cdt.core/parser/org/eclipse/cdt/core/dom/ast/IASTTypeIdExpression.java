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
 * @author jcamelon
 */
public interface IASTTypeIdExpression extends IASTExpression {

	/**
	 * <code>op_sizeof</code> sizeof( typeId ) expression
	 */
	public static final int op_sizeof = 0;

	/**
	 * <code>op_last</code> defined for sub-interfaces to extend.
	 */
	public static final int op_last = op_sizeof;

	/**
	 * Get the operator for the expression.
	 * 
	 * @return int
	 */
	public int getOperator();

	/**
	 * Set the operator for the expression.
	 * @param value int
	 */
	public void setOperator(int value);

	/**
	 * <code>TYPEID</code> represents the relationship between an <code>IASTTypeIdExpression</code> and
	 * it's nested <code>IASTTypeId</code>.
	 */
	public static final ASTNodeProperty TYPE_ID = new ASTNodeProperty("IASTTypeIdExpression.TYPE_ID - IASTTypeId for IASTTypeIdExpression"); //$NON-NLS-1$

	/**
	 * Set the type Id.
	 * @param typeId
	 */
	public void setTypeId(IASTTypeId typeId);

	/**
	 * Get the type Id.
	 * 
	 * @return
	 */
	public IASTTypeId getTypeId();

}
