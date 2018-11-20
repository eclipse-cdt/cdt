/*******************************************************************************
 * Copyright (c) 2004, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IScope;

/**
 * The 'if' statement including the optional else clause.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPASTIfStatement extends IASTIfStatement {
	/**
	 * {@code INIT_STATEMENT} represents the relationship between an
	 * {@code ICPPASTIfStatement} and its nested {@code IASTStatement}.
	 *
	 * @since 6.5
	 */
	public static final ASTNodeProperty INIT_STATEMENT = new ASTNodeProperty(
			"ICPPASTIfStatement.INIT_STATEMENT - IASTStatement init-statement for ICPPASTIfStatement"); //$NON-NLS-1$

	/**
	 * Returns the condition declaration. The condition declaration and the condition expression are
	 * mutually exclusive.
	 *
	 * @return the condition declaration, or <code>null</code> if the 'if' statement doesn't
	 *     have a condition declaration.
	 */
	public IASTDeclaration getConditionDeclaration();

	/**
	 * Sets the condition declaration.
	 */
	public void setConditionDeclaration(IASTDeclaration d);

	/**
	 * Sets the isConstxpr member variable.
	 *
	 * @since 6.5
	 */
	public void setIsConstexpr(boolean isConstexpr);

	/**
	 * Checks whether this if statement is a constexpr if statement.
	 *
	 * @return true iff this if statement is a constexpr if.
	 *
	 * @since 6.5
	 */
	public boolean isConstexpr();

	/**
	 * Returns the init-statement for an if.
	 *
	 * @return the init-statement, or <code>null</code> if the 'if' statement doesn't
	 *    have one.
	 *
	 * @since 6.5
	 */
	public IASTStatement getInitializerStatement();

	/**
	 * Sets the optional init-statement of an if.
	 *
	 * @param statement this statement should either be a <code>IASTSimpleDeclaration</code> or a
	 *    <code>IASTExpressionStatement</code>.
	 *
	 * @since 6.5
	 */
	public void setInitializerStatement(IASTStatement statement);

	/**
	 * Returns the implicit <code>IScope</code> represented by this if statement
	 *
	 * @return <code>IScope</code>
	 */
	public IScope getScope();

	/**
	 * @since 5.1
	 */
	@Override
	public ICPPASTIfStatement copy();

	/**
	 * @since 5.3
	 */
	@Override
	public ICPPASTIfStatement copy(CopyStyle style);
}
