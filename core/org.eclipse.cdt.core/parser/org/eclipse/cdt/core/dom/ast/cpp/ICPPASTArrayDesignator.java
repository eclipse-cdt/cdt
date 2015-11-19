/*******************************************************************************
 * Copyright (c) 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Sergey Prigogin (Google) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;

/**
 * Array designator, e.g. [4] in int a[6] = { [4] = 29, [2] = 15 };
 * @since 5.12
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPASTArrayDesignator extends ICPPASTDesignator {
	/**
	 * The relationship between the array designator and the subscript expression.
	 */
	public static final ASTNodeProperty SUBSCRIPT_EXPRESSION = new ASTNodeProperty(
			"ICPPASTArrayDesignator.SUBSCRIPT_EXPRESSION - expression inside square brackets"); //$NON-NLS-1$

	/**
	 * Returns the subscript expression.
	 */
	public ICPPASTExpression getSubscriptExpression();

	/**
	 * Sets the subscript expression.
	 * 
	 * @param expression the expression for the subscript
	 */
	public void setSubscriptExpression(ICPPASTExpression expression);

	@Override
	public ICPPASTArrayDesignator copy();

	@Override
	public ICPPASTArrayDesignator copy(CopyStyle style);
}
