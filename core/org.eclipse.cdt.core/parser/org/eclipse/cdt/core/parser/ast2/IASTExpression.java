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
package org.eclipse.cdt.core.parser.ast2;


/**
 * An expression is some computation that produces a value of a given
 * type. The type can be a void type which implies that the result can
 * not be used in further expressions.
 * 
 * @author Doug Schaefer
 */
public interface IASTExpression extends IASTNode {

	/**
	 * @return the type for the result value of the expression
	 */
	public IASTType getType();
	
	public void setType(IASTType type);

	/**
	 * @return and identifier that represent the operator for this
	 * expression
	 */
	public IASTIdentifier getOperator();
	
	public void setOperator(IASTIdentifier operator);
	
	/**
	 * @return the operands for this expression
	 */
	public int numOperands();
	public IASTExpression getOperands(int i);
	
}
