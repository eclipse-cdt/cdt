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
 *    John Camelon (IBM Rational Software) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.c;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTExpression;

/**
 * C-style array designator. e.g. struct ABC { int def[10] }; struct ABC
 * instance = { def[0] = 9 };
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICASTArrayDesignator extends ICASTDesignator {
	/**
	 * <code>SUBSCRIPT_EXPRESSION</code> represents the relationship between
	 * the designator and the subscript expression.
	 */
	public static final ASTNodeProperty SUBSCRIPT_EXPRESSION = new ASTNodeProperty(
			"ICASTArrayDesignator.SUBSCRIPT_EXPRESSION - relationship between designator and subscript expression"); //$NON-NLS-1$

	/**
	 * Get the subsript expression.
	 *
	 * @return value <code>IASTExpression</code>
	 */
	public IASTExpression getSubscriptExpression();

	/**
	 * Set the subscript expression.
	 *
	 * @param value
	 *            <code>IASTExpression</code>
	 */
	public void setSubscriptExpression(IASTExpression value);

	/**
	 * @since 5.1
	 */
	@Override
	public ICASTArrayDesignator copy();

	/**
	 * @since 5.3
	 */
	@Override
	public ICASTArrayDesignator copy(CopyStyle style);
}
