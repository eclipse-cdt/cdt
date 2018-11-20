/**********************************************************************
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
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTSwitchStatement;
import org.eclipse.cdt.core.dom.ast.IScope;

/**
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPASTSwitchStatement extends IASTSwitchStatement {

	/**
	 * {@code INIT_STATEMENT} represents the relationship between an
	 * {@code ICPPASTSwitchStatement} and its nested {@code IASTStatement}.
	 *
	 * @since 6.5
	 */
	public static final ASTNodeProperty INIT_STATEMENT = new ASTNodeProperty(
			"ICPPASTSwitchStatement.INIT_STATEMENT - IASTStatement init-statement for ICPPASTSwitchStatement"); //$NON-NLS-1$

	/**
	 * <code>CONTROLLER_DECLARATION</code> represents the relationship between an
	 * <code>IASTSwitchStatement</code> and it's nested
	 * <code>IASTDeclaration</code>.
	 */
	public static final ASTNodeProperty CONTROLLER_DECLARATION = new ASTNodeProperty(
			"IASTSwitchStatement.CONTROLLER - IASTDeclaration (controller) for IASTSwitchExpression"); //$NON-NLS-1$

	/**
	 * In C++, a switch statement can be contorller by a declaration.
	 *
	 * @return <code>IASTDeclaration</code>
	 */
	public IASTDeclaration getControllerDeclaration();

	/**
	 * In C++, a switch statement can be contorller by a declaration.
	 *
	 * @param d <code>IASTDeclaration</code>
	 */
	public void setControllerDeclaration(IASTDeclaration d);

	/**
	 * Returns the init-statement for a switch.
	 *
	 * @return the init-statement, or <code>null</code> if the 'switch' statement doesn't
	 *    have one.
	 *
	 * @since 6.5
	 */
	public IASTStatement getInitializerStatement();

	/**
	 * Sets the optional init-statement of an switch.
	 *
	 * @param statement this statement should either be a <code>IASTSimpleDeclaration</code> or a
	 *    <code>IASTExpressionStatement</code>.
	 *
	 * @since 6.5
	 */
	public void setInitializerStatement(IASTStatement statement);

	/**
	 * Get the <code>IScope</code> represented by this switch.
	 *
	 * @return <code>IScope</code>
	 */
	public IScope getScope();

	/**
	 * @since 5.1
	 */
	@Override
	public ICPPASTSwitchStatement copy();

	/**
	 * @since 5.3
	 */
	@Override
	public ICPPASTSwitchStatement copy(CopyStyle style);
}
