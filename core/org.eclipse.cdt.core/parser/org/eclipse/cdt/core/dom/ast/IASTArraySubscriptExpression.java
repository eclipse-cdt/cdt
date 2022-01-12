/*******************************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation and others.
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
	 * @deprecated Replaced by {@link #getArgument()}
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Deprecated
	public IASTExpression getSubscriptExpression();

	/**
	 * @deprecated Replaced by {@link #setArgument(IASTInitializerClause)}
	 * @noreference This method is not intended to be referenced by clients.
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
