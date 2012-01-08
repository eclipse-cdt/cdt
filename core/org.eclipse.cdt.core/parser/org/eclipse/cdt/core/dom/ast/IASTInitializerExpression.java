/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Doug Schaefer (IBM) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * This is an initializer that is simply an expression.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 * @deprecated Replaced by {@link IASTEqualsInitializer}.
 */
@Deprecated
public interface IASTInitializerExpression extends IASTEqualsInitializer {

	/**
	 * <code>INITIALIZER_EXPRESSION</code> represents the relationship between
	 * an <code>IASTInitializerExpression</code>. and its <code></code>IASTExpression</code>.
	 */
	public static final ASTNodeProperty INITIALIZER_EXPRESSION = INITIALIZER;

	/**
	 * Get the expression for the initializer.
	 * 
	 * @return <code>IASTExpression</code>
	 */
	public IASTExpression getExpression();

	/**
	 * Set the initializer's expression.
	 * 
	 * @param expression
	 *            <code>IASTExpression</code>
	 */
	public void setExpression(IASTExpression expression);
	
	/**
	 * @since 5.1
	 */
	@Override
	public IASTInitializerExpression copy();

	/**
	 * @since 5.3
	 */
	@Override
	public IASTInitializerExpression copy(CopyStyle style);
}
