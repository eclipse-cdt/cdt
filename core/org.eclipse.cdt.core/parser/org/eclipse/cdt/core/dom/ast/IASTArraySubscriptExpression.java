/*******************************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Camelon (IBM Rational Software) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * This interface represents a postfix array subscript expression. x[10]
 * y.z()[t * t]
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IASTArraySubscriptExpression extends IASTExpression {
	public static final ASTNodeProperty ARRAY = new ASTNodeProperty(
			"IASTArraySubscriptExpression.ARRAY [IASTExpression]"); //$NON-NLS-1$
	public static final ASTNodeProperty SUBSCRIPT = new ASTNodeProperty(
			"IASTArraySubscriptExpression.SUBSCRIPT - [IASTFunctionArgument]"); //$NON-NLS-1$

	/**
	 * Get the expression that represents the array
	 * 
	 * @return <code>IASTExpression</code> that represents the array.
	 */
	public IASTExpression getArrayExpression();

	/**
	 * Set the expression that represents the array.
	 * 
	 * @param expression
	 *            <code>IASTExpression</code> to be set.
	 */
	public void setArrayExpression(IASTExpression expression);

	/**
	 * Returns the operand of this expression. In c++ the operand can be a braced initializer list.
	 * @since 5.2
	 */
	public IASTInitializerClause getArgument();

	/**
	 * Not allowed on frozen ast.
	 * @since 5.2
	 */
	public void setArgument(IASTInitializerClause expression);

	/**
	 * Returns the subscript expression, or <code>null</code>. Consider using {@link #getArgument()}.
	 * @deprecated Replaced by {@link #getArgument()}
	 */
	@Deprecated
	public IASTExpression getSubscriptExpression();

	/**
	 * Not allowed on frozen ast.
	 * @deprecated Replaced by {@link #setArgument(IASTInitializerClause)}
	 */
	@Deprecated
	public void setSubscriptExpression(IASTExpression expression);

	/**
	 * @since 5.1
	 */
	@Override
	IASTArraySubscriptExpression copy();
	
	/**
	 * @since 5.3
	 */
	@Override
	IASTArraySubscriptExpression copy(CopyStyle style);
}
