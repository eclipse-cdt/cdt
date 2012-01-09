/*******************************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Camelon (IBM Rational Software) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.gnu;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTExpression;

/**
 * There are GNU language extensions that apply to both GCC and G++. Compound
 * statement's as expressions.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IGNUASTCompoundStatementExpression extends IASTExpression {

	/**
	 * <code>STATEMENT</code> describes the relationship between
	 * <code>IGNUASTCompoundStatementExpression</code> and
	 * <code>IASTCompoundStatement</code>.
	 */
	public static final ASTNodeProperty STATEMENT = new ASTNodeProperty(
			"IGNUASTCompoundStatementExpression.STATEMENT - IASTCompoundStatement for IGNUASTCompoundStatementExpression"); //$NON-NLS-1$

	/**
	 * Get the compound statement.
	 * 
	 * @return <code>IASTCompoundStatement</code>
	 */
	public IASTCompoundStatement getCompoundStatement();

	/**
	 * Set the compound statement.
	 * 
	 * @param statement
	 *            <code>IASTCompoundStatement</code>
	 */
	public void setCompoundStatement(IASTCompoundStatement statement);

	/**
	 * @since 5.1
	 */
	@Override
	public IGNUASTCompoundStatementExpression copy();

	/**
	 * @since 5.3
	 */
	@Override
	public IGNUASTCompoundStatementExpression copy(CopyStyle style);
}
