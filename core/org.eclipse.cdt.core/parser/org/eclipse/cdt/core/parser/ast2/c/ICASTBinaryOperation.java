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
package org.eclipse.cdt.core.parser.ast2.c;

/**
 * An expression with an operator and two operands.
 * 
 * @author Doug Schaefer
 */
public interface ICASTBinaryOperation extends ICASTExpression {

	/**
	 * @return a string representation of the operator.
	 */
	public String getOperator();

	/**
	 * @return the expression for the first or left hand side operand
	 */
	public ICASTExpression getOperand1();
	
	/**
	 * @return the expression for the second or right hand side operand
	 */
	public ICASTExpression getOperand2();
	
}
