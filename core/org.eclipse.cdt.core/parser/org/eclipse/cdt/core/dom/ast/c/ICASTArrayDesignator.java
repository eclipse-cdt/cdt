/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.c;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTExpression;

/**
 * C-style array designator. e.g. struct ABC { int def[10] }; struct ABC
 * instance = { def[0] = 9 };
 * 
 * @author jcamelon
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

}
