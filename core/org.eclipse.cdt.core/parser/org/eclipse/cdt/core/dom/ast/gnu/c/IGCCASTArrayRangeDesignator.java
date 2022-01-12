/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     John Camelon (IBM Rational Software) - Initial API and implementation
 *******************************************************************************/
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
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IGCCASTArrayRangeDesignator extends ICASTDesignator {
	/**
	 * <code>SUSBCRIPT_FLOOR_EXPRESSION</code> represents the lower value in
	 * the range of expressions.
	 */
	public static final ASTNodeProperty SUBSCRIPT_FLOOR_EXPRESSION = new ASTNodeProperty(
			"IGCCASTArrayRangeDesignator.SUBSCRIPT_FLOOR_EXPRESSION - lower value in range"); //$NON-NLS-1$

	/**
	 * <code>SUSBCRIPT_CEILING_EXPRESSION</code> represents the higher value
	 * in the range of expressions.
	 */
	public static final ASTNodeProperty SUBSCRIPT_CEILING_EXPRESSION = new ASTNodeProperty(
			"IGCCASTArrayRangeDesignator.SUBSCRIPT_CEILING_EXPRESSION - higher value in range"); //$NON-NLS-1$

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

	/**
	 * @since 5.1
	 */
	@Override
	public IGCCASTArrayRangeDesignator copy();

	/**
	 * @since 5.3
	 */
	@Override
	public IGCCASTArrayRangeDesignator copy(CopyStyle style);
}
