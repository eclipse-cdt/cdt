/**********************************************************************
 * Copyright (c) 2002-2004 IBM Canada and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation */
package org.eclipse.cdt.core.dom.ast.gnu.c;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.c.ICASTDesignator;

/**
 * GCC-specific designator that allows for shorthand array range to be specified
 * in a designated initializer.
 * 
 * struct ABC { int def[10]; } abc = { def[4...10] = 3 };
 * 
 * @author jcamelon
 */
public interface IGCCASTArrayRangeDesignator extends ICASTDesignator {

	/**
	 * <code>SUSBCRIPT_FLOOR_EXPRESSION</code> represents the lower value in
	 * the range of expressions.
	 */
	public static final ASTNodeProperty SUBSCRIPT_FLOOR_EXPRESSION = new ASTNodeProperty(
			"Subscript Floor Expression"); //$NON-NLS-1$

	/**
	 * <code>SUSBCRIPT_CEILING_EXPRESSION</code> represents the higher value
	 * in the range of expressions.
	 */
	public static final ASTNodeProperty SUBSCRIPT_CEILING_EXPRESSION = new ASTNodeProperty(
			"Subscript Ceiling Expression"); //$NON-NLS-1$

	/**
	 * Get the floor expression of the range.
	 * 
	 * @return the floor expression <code>IASTExpression</code>
	 */
	public IASTExpression getRangeFloor();

	/**
	 * Set the floor expression of the range.
	 * 
	 * @param expression
	 *            <code>IASTExpression</code>
	 */
	public void setRangeFloor(IASTExpression expression);

	/**
	 * Get the range ceiling expression.
	 * 
	 * @return <code>IASTExpression</code>
	 */
	public IASTExpression getRangeCeiling();

	/**
	 * Set the ceiling expression of the range.
	 * 
	 * @param expression
	 *            <code>IASTExpression</code>
	 */
	public void setRangeCeiling(IASTExpression expression);
}
